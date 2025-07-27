package me.koutachan.bouncy.utils;

import me.koutachan.bouncy.Bouncy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.logging.Level;

public class ThroughWallArrowUtils {
    public static boolean failed;

    public static boolean trySpawnThroughWallArrow(org.bukkit.entity.AbstractArrow bukkitAbstractArrow) {
        if (failed)
            return false;

        try {
            AbstractArrow abstractArrow = (AbstractArrow) NMSUtils.getHandle(bukkitAbstractArrow);
            AbstractArrow to = null;
            if (abstractArrow instanceof net.minecraft.world.entity.projectile.Arrow) {
                to = new ThroughWallArrow(abstractArrow.level(), abstractArrow.getX(), abstractArrow.getY(), abstractArrow.getZ(), abstractArrow.pickupItemStack, abstractArrow.firedFromWeapon);
            } else if (abstractArrow instanceof SpectralArrow) {
                to = new ThroughWallSpectralArrow(abstractArrow.level(), abstractArrow.getX(), abstractArrow.getY(), abstractArrow.getZ(), abstractArrow.pickupItemStack, abstractArrow.firedFromWeapon);
            }

            if (to != null) {
                to.setDeltaMovement(abstractArrow.getDeltaMovement());
                to.setXRot(abstractArrow.getXRot());
                to.setYRot(abstractArrow.getYRot());
                to.setYHeadRot(abstractArrow.getYHeadRot());
                abstractArrow.level().addFreshEntity(to);
                to.setOwner(abstractArrow.getOwner());
            }
            return true;
        } catch (Throwable throwable) {
            Bouncy.INSTANCE.getLogger().log(Level.WARNING, "Failed to spawn Through Wall Arrow!", throwable);
            failed = true;
            return false;
        }
    }

    public static class ThroughWallArrow extends net.minecraft.world.entity.projectile.Arrow {
        private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR;

        public ThroughWallArrow(net.minecraft.world.level.Level world, double d0, double d1, double d2, ItemStack itemstack, @Nullable ItemStack itemstack1) {
            super(world, d0, d1, d2, itemstack, itemstack1);
            this.updateColor();
        }

        public ThroughWallArrow(net.minecraft.world.level.Level world, LivingEntity entityliving, ItemStack itemstack, @Nullable ItemStack itemstack1) {
            super(world, entityliving, itemstack, itemstack1);
            this.updateColor();
        }

        @Override
        public PotionContents getPotionContents() {
            return this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        }

        @Override
        public void setPotionContents(PotionContents potioncontents) {
            this.getPickupItemStackOrigin().set(DataComponents.POTION_CONTENTS, potioncontents);
            this.updateColor();
        }

        @Override
        protected void setPickupItemStack(ItemStack itemstack) {
            super.setPickupItemStack(itemstack);
            this.updateColor();
        }

        @Override
        public void updateColor() {
            PotionContents potioncontents = this.getPotionContents();
            this.entityData.set(ID_EFFECT_COLOR, potioncontents.equals(PotionContents.EMPTY) ? -1 : potioncontents.getColor());
        }

        @Override
        public void addEffect(MobEffectInstance mobEffect) {
            this.setPotionContents(this.getPotionContents().withEffectAdded(mobEffect));
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder dataWatcher) {
            super.defineSynchedData(dataWatcher);
            dataWatcher.define(ID_EFFECT_COLOR, -1);
        }

        @Override
        public void tick() {
            boolean flag = !this.isNoPhysics();
            Vec3 deltaMovement = this.getDeltaMovement();

            BlockPos blockPosition = this.blockPosition();
            BlockState blockState = this.level().getBlockState(blockPosition);

            if (!blockState.isAir() && !isGlass(blockState) && flag) {
                VoxelShape voxelshape = blockState.getCollisionShape(this.level(), blockPosition);
                if (!voxelshape.isEmpty()) {
                    Vec3 vec3d1 = this.position();
                    for (AABB aabb : voxelshape.toAabbs()) {
                        if (aabb.move(blockPosition).contains(vec3d1)) {
                            this.setInGround(true);
                            break;
                        }
                    }
                }
            }

            if (this.shakeTime > 0) {
                --this.shakeTime;
            }

            if (this.isInWaterOrRain() || blockState.is(Blocks.POWDER_SNOW)) {
                this.clearFire();
            }

            if (!this.isInGround()) {
                Vec3 pos = this.position();
                if (this.isInWater()) {
                    this.applyInertia(this.getWaterInertia());
                    this.addBubbleParticles(pos);
                }

                if (this.isCritArrow()) {
                    for (int i = 0; i < 4; ++i) {
                        this.level().addParticle(ParticleTypes.CRIT, pos.x + deltaMovement.x * (double) i / 4.0, pos.y + deltaMovement.y * (double) i / 4.0, pos.z + deltaMovement.z * (double) i / 4.0, -deltaMovement.x, -deltaMovement.y + 0.2, -deltaMovement.z);
                    }
                }

                float f;
                if (!flag) {
                    f = (float) (Mth.atan2(-deltaMovement.x, -deltaMovement.z) * 57.2957763671875);
                } else {
                    f = (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 57.2957763671875);
                }

                float f1 = (float) (Mth.atan2(deltaMovement.y, deltaMovement.horizontalDistance()) * 57.2957763671875);
                this.setXRot(lerpRotation(this.getXRot(), f1));
                this.setYRot(lerpRotation(this.getYRot(), f));
                if (flag) {
                    this.stepMoveAndHit(deltaMovement);
                } else {
                    this.setPos(pos.add(deltaMovement));
                    this.applyEffectsFromBlocks();
                }

                if (!this.isInWater()) {
                    this.applyInertia(0.99F);
                }

                if (flag && !this.isInGround()) {
                    this.applyGravity();
                }
            }
            // マインクラフトのせいで、常にテレポートパケットを送らないと矢の位置がクライアントサイド側でおかしくなる
            // クライアントサイドの問題なので解決策がかなり無理やりだけど仕方ない
            ((ServerChunkCache) level().getChunkSource()).broadcast(this, new ClientboundEntityPositionSyncPacket(getId(), PositionMoveRotation.of(this), false));

            super.baseTick();
            if (this.isInGround() && this.inGroundTime != 0 && !this.getPotionContents().equals(PotionContents.EMPTY) && this.inGroundTime >= 600) {
                this.level().broadcastEntityEvent(this, (byte)0);
                this.setPickupItemStack(new ItemStack(Items.ARROW));
            }
        }

        private void addBubbleParticles(Vec3 vec3d) {
            Vec3 vec3d1 = this.getDeltaMovement();
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.BUBBLE, vec3d.x - vec3d1.x * 0.25, vec3d.y - vec3d1.y * 0.25, vec3d.z - vec3d1.z * 0.25, vec3d1.x, vec3d1.y, vec3d1.z);
            }
        }

        private void applyInertia(float f) {
            Vec3 deltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(deltaMovement.scale(f));
        }

        public boolean isGlass(BlockState blockState) {
            Block block = blockState.getBlock();
            return block instanceof StainedGlassPaneBlock || block instanceof StainedGlassBlock || block == Blocks.GLASS || block == Blocks.GLASS_PANE;
        }

        private void stepMoveAndHit(Vec3 deltaMovement) {
            while (true) {
                if (this.isAlive()) {
                    Vec3 pos = this.position();
                    Vec3 newPos = pos.add(deltaMovement);
                    BlockHitResult movingObject = this.level().clip(new ClipContext(pos,  newPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

                    boolean canPass = movingObject.getType() != HitResult.Type.MISS && !isGlass(level().getBlockState(movingObject.getBlockPos()));
                    if (canPass) {
                        newPos = movingObject.getLocation();
                    }

                    EntityHitResult entityResult = this.findHitEntity(pos, newPos);

                    this.setPos(newPos);
                    this.applyEffectsFromBlocks(pos, newPos);
                    if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
                        this.handlePortal();
                    }

                    if (entityResult != null) {
                        if (!this.isAlive() || this.noPhysics) {
                            continue;
                        }

                        ProjectileDeflection projectiledeflection = this.preHitTargetOrDeflectSelf(entityResult);
                        this.hasImpulse = true;
                        if (this.getPierceLevel() > 0 && projectiledeflection == ProjectileDeflection.NONE) {
                            continue;
                        }
                    } else if (canPass) {
                        this.hitTargetOrDeflectSelf(movingObject);
                    }
                }
                return;
            }
        }

        private void makeParticle(int i) {
            int j = this.getColor();
            if (j != -1 && i > 0) {
                for(int k = 0; k < i; ++k) {
                    this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, j), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
                }
            }
        }

        public int getColor() {
            return this.entityData.get(ID_EFFECT_COLOR);
        }

        protected void doPostHurtEffects(LivingEntity entityliving) {
            super.doPostHurtEffects(entityliving);
            Entity entity = this.getEffectSource();
            PotionContents potioncontents = this.getPotionContents();
            Iterator<MobEffectInstance> iterator;
            MobEffectInstance mobeffect;
            if (potioncontents.potion().isPresent()) {
                iterator = ((Potion) ((Holder<?>) potioncontents.potion().get()).value()).getEffects().iterator();

                while (iterator.hasNext()) {
                    mobeffect = iterator.next();
                    entityliving.addEffect(new MobEffectInstance(mobeffect.getEffect(), Math.max(mobeffect.mapDuration((i) -> i / 8), 1), mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isVisible()), entity, EntityPotionEffectEvent.Cause.ARROW);
                }
            }

            iterator = potioncontents.customEffects().iterator();

            while (iterator.hasNext()) {
                mobeffect = iterator.next();
                entityliving.addEffect(mobeffect, entity, EntityPotionEffectEvent.Cause.ARROW);
            }
        }

        protected ItemStack getDefaultPickupItem() {
            return new ItemStack(Items.ARROW);
        }

        public void handleEntityEvent(byte b0) {
            if (b0 == 0) {
                int i = this.getColor();
                if (i != -1) {
                    float f = (float)(i >> 16 & 255) / 255.0F;
                    float f1 = (float)(i >> 8 & 255) / 255.0F;
                    float f2 = (float)(i & 255) / 255.0F;

                    for(int j = 0; j < 20; ++j) {
                        this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, f, f1, f2), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 0.0, 0.0, 0.0);
                    }
                }
            } else {
                super.handleEntityEvent(b0);
            }
        }

        static {
            ID_EFFECT_COLOR = SynchedEntityData.defineId(ThroughWallArrow.class, EntityDataSerializers.INT);
        }
    }

    public static class ThroughWallSpectralArrow extends SpectralArrow {
        public int duration = 200;

        public ThroughWallSpectralArrow(net.minecraft.world.level.Level world, LivingEntity entityliving, ItemStack itemstack, @Nullable ItemStack itemstack1) {
            super(world, entityliving, itemstack, itemstack1);
        }

        public ThroughWallSpectralArrow(net.minecraft.world.level.Level world, double d0, double d1, double d2, ItemStack itemstack, @Nullable ItemStack itemstack1) {
            super(world, d0, d1, d2, itemstack, itemstack1);
        }

        @Override
        public void tick() {
            boolean flag = !this.isNoPhysics();
            Vec3 deltaMovement = this.getDeltaMovement();

            BlockPos blockPosition = this.blockPosition();
            BlockState blockState = this.level().getBlockState(blockPosition);

           if (!blockState.isAir() && !isGlass(blockState) && flag) {
                VoxelShape voxelshape = blockState.getCollisionShape(this.level(), blockPosition);
                if (!voxelshape.isEmpty()) {
                    Vec3 vec3d1 = this.position();
                    for (AABB aabb : voxelshape.toAabbs()) {
                        if (aabb.move(blockPosition).contains(vec3d1)) {
                            this.setInGround(true);
                            break;
                        }
                    }
                }
            }

            if (this.shakeTime > 0) {
                --this.shakeTime;
            }

            if (this.isInWaterOrRain() || blockState.is(Blocks.POWDER_SNOW)) {
                this.clearFire();
            }

            if (!this.isInGround()) {
                Vec3 pos = this.position();
                if (this.isInWater()) {
                    this.applyInertia(this.getWaterInertia());
                    this.addBubbleParticles(pos);
                }

                if (this.isCritArrow()) {
                    for (int i = 0; i < 4; ++i) {
                        this.level().addParticle(ParticleTypes.CRIT, pos.x + deltaMovement.x * (double) i / 4.0, pos.y + deltaMovement.y * (double) i / 4.0, pos.z + deltaMovement.z * (double) i / 4.0, -deltaMovement.x, -deltaMovement.y + 0.2, -deltaMovement.z);
                    }
                }

                float f;
                if (!flag) {
                    f = (float) (Mth.atan2(-deltaMovement.x, -deltaMovement.z) * 57.2957763671875);
                } else {
                    f = (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 57.2957763671875);
                }

                float f1 = (float) (Mth.atan2(deltaMovement.y, deltaMovement.horizontalDistance()) * 57.2957763671875);
                this.setXRot(lerpRotation(this.getXRot(), f1));
                this.setYRot(lerpRotation(this.getYRot(), f));
                if (flag) {
                    this.stepMoveAndHit(deltaMovement);
                } else {
                    this.setPos(pos.add(deltaMovement));
                    this.applyEffectsFromBlocks();
                }

                if (!this.isInWater()) {
                    this.applyInertia(0.99F);
                }

                if (flag && !this.isInGround()) {
                    this.applyGravity();
                }
            }
            // マインクラフトのせいで、常にテレポートパケットを送らないと矢の位置がクライアントサイド側でおかしくなる
            // クライアントサイドの問題なので解決策がかなり無理やりだけど仕方ない
            ((ServerChunkCache) level().getChunkSource()).broadcast(this, new ClientboundEntityPositionSyncPacket(getId(), PositionMoveRotation.of(this), false));

            super.baseTick();
        }

        private void addBubbleParticles(Vec3 vec3d) {
            Vec3 vec3d1 = this.getDeltaMovement();
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.BUBBLE, vec3d.x - vec3d1.x * 0.25, vec3d.y - vec3d1.y * 0.25, vec3d.z - vec3d1.z * 0.25, vec3d1.x, vec3d1.y, vec3d1.z);
            }
        }

        private void applyInertia(float f) {
            Vec3 deltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(deltaMovement.scale(f));
        }

        public boolean isGlass(BlockState blockState) {
            Block block = blockState.getBlock();
            return block instanceof StainedGlassPaneBlock || block instanceof StainedGlassBlock || block == Blocks.GLASS || block == Blocks.GLASS_PANE;
        }

        private void stepMoveAndHit(Vec3 deltaMovement) {
            while (true) {
                if (this.isAlive()) {
                    Vec3 pos = this.position();
                    Vec3 newPos = pos.add(deltaMovement);
                    BlockHitResult movingObject = this.level().clip(new ClipContext(pos,  newPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

                    boolean canPass = movingObject.getType() != HitResult.Type.MISS && !isGlass(level().getBlockState(movingObject.getBlockPos()));
                    if (canPass) {
                        newPos = movingObject.getLocation();
                    }

                    EntityHitResult entityResult = this.findHitEntity(pos, newPos);

                    this.setPos(newPos);
                    this.applyEffectsFromBlocks(pos, newPos);
                    if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
                        this.handlePortal();
                    }

                    if (entityResult != null) {
                        if (!this.isAlive() || this.noPhysics) {
                            continue;
                        }

                        ProjectileDeflection projectiledeflection = this.preHitTargetOrDeflectSelf(entityResult);
                        this.hasImpulse = true;
                        if (this.getPierceLevel() > 0 && projectiledeflection == ProjectileDeflection.NONE) {
                            continue;
                        }
                    } else if (canPass) {
                        this.hitTargetOrDeflectSelf(movingObject);
                    }
                }
                return;
            }
        }

        @Override
        protected void doPostHurtEffects(LivingEntity entityliving) {
            super.doPostHurtEffects(entityliving);
            MobEffectInstance mobEffect = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
            entityliving.addEffect(mobEffect, this.getEffectSource(), EntityPotionEffectEvent.Cause.ARROW);
        }
    }
}
