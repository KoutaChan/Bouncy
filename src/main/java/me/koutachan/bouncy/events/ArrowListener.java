package me.koutachan.bouncy.events;

import de.tr7zw.changeme.nbtapi.NBT;
import me.koutachan.bouncy.ArrowTracker;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.NMSUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class ArrowListener implements Listener {
    @EventHandler
    public void onProjectileShootEvent(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            ArrowTracker.TRACKED_ARROW.put(player, event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        ArrowTracker.TRACKED_ARROW.remove(event.getPlayer());
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Projectile projectile && !lateTrack(projectile)) {
            Bukkit.getScheduler().runTaskLater(Bouncy.INSTANCE, () -> lateTrack(projectile), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBorderHitEvent(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }
        if (event.getHitBlock() != null && event.getHitBlock().getType() == Material.AIR) { // Bounced by Border
            Vector velocity = arrow.getVelocity();
            Location location = arrow.getLocation();
            NMSUtils.tryDisableReverse(arrow);
            Bukkit.getScheduler().runTask(Bouncy.INSTANCE, () -> {
                arrow.setVelocity(velocity);
                arrow.setRotation(location.getYaw(), location.getPitch());
            });
        }
    }

    private static boolean lateTrack(Projectile projectile) {
        if (projectile.getShooter() instanceof Player player) {
            ArrowTracker.TRACKED_ARROW.put(player, projectile);
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBouncyHitEvent(ProjectileHitEvent event) {
        Integer bouncyCount = NBT.getPersistentData(event.getEntity(), nbt -> nbt.getInteger("BouncyCount"));
        if (event.getHitBlock() == null || bouncyCount <= 0) {
            return;
        }
        Vector adjustedVelocity = calculateReboundVelocity(event.getEntity().getVelocity(), event.getHitBlockFace());
        if (event.getEntity().getShooter() instanceof Player player) {
            for (Entity target : event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), 5, 5, 5)) {
                if (target instanceof Player && !DamageUtils.isSameTeam(player, target)) {
                    adjustedVelocity = trackTowardsTarget(adjustedVelocity, event.getEntity().getLocation(), target.getLocation());
                    break;
                }
            }
        }
        decrementBouncyCountAndUpdateEntity(event.getEntity(), bouncyCount - 1, adjustedVelocity);
    }

    private Vector calculateReboundVelocity(Vector velocity, BlockFace hitBlockFace) {
        double bounceFactor = 0.8;

        Vector result = velocity.clone();
        if (hitBlockFace == BlockFace.UP || hitBlockFace == BlockFace.DOWN) {
            result.setY(-result.getY() * bounceFactor);
        } else if (hitBlockFace == BlockFace.NORTH || hitBlockFace == BlockFace.SOUTH) {
            result.setZ(-result.getZ() * bounceFactor);
        } else if (hitBlockFace == BlockFace.EAST || hitBlockFace == BlockFace.WEST) {
            result.setX(-result.getX() * bounceFactor);
        }
        return result;
    }

    private Vector trackTowardsTarget(Vector currentVelocity, Location source, Location target) {
        Vector bounceVector = currentVelocity.clone();

        double originalSpeed = bounceVector.length();
        Vector direction = target.toVector().subtract(source.toVector()).normalize();
        return bounceVector.normalize().multiply(0.85)
                .add(direction.multiply(0.15))
                .normalize()
                .multiply(originalSpeed);
    }

    private void decrementBouncyCountAndUpdateEntity(Projectile projectile, int newBouncyCount, Vector adjustedVelocity) {
        Location previousPos = projectile.getLocation()
                .subtract(projectile.getVelocity().multiply(.5));
        Arrow arrow = projectile.getWorld().spawnArrow(previousPos, adjustedVelocity, 1, 1);
        arrow.setShooter(projectile.getShooter());
        NBT.modifyPersistentData(arrow, nbt -> {
            nbt.setInteger("BouncyCount", newBouncyCount);
        });
        for (String tag : projectile.getScoreboardTags()) { // タグ維持用
            arrow.addScoreboardTag(tag);
        }
        projectile.remove();
    }

    public static Vector[] SPREAD_VECTORS = new Vector[] {
            new Vector(1.0, 0.5, 0.0),
            new Vector(0.0, 0.5, 1.0),
            new Vector(-1.0, 0.5, 0.0),
            new Vector(0.0, 0.5, -1.0),
            new Vector(0.8, 0.4, 0.8),
            new Vector(-0.8, 0.4, -0.8),
            new Vector(-0.8, 0.4, 0.8),
            new Vector(0.8, 0.4, -0.8)
    };

    @EventHandler
    public void onSpreadHitEvent(ProjectileHitEvent event) {
        boolean isSpread = NBT.getPersistentData(event.getEntity(), nbt -> nbt.getBoolean("Spread"));
        if (event.getHitBlock() == null || !isSpread) {
            return;
        }
        var location = event.getEntity().getLocation().subtract(event.getEntity().getVelocity());
        for (Vector direction : SPREAD_VECTORS) {
            Arrow arrow = event.getEntity().getWorld().spawnArrow(location, direction, 1f, 0f);
            arrow.setShooter(event.getEntity().getShooter());
        }
        event.getEntity().remove();
    }

    @EventHandler
    public void onExplosiveHitEvent(ProjectileHitEvent event) {
        boolean isExplosive = NBT.getPersistentData(event.getEntity(), nbt -> nbt.getBoolean("Explosive"));
        if (event.getHitBlock() == null || !isExplosive) {
            return;
        }
        event.getEntity().getNearbyEntities(3, 3, 3).stream()
                .filter(entity -> isValidNearbyEntity(entity, event.getEntity().getShooter()))
                .forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.setVelocity(livingEntity.getLocation().subtract(event.getEntity().getLocation()).toVector().normalize());
                        if (event.getEntity().getShooter() instanceof Entity shooter) {
                            livingEntity.damage(2, shooter);
                        } else {
                            livingEntity.damage(2);
                        }
                    }
                });
        handleExplosionEffect(event);
    }

    private boolean isValidNearbyEntity(Entity nearbyEntity, ProjectileSource shooter) {
        return nearbyEntity == shooter ||
                (nearbyEntity instanceof Player player
                && shooter instanceof Entity shooterEntity
                && !DamageUtils.isSameTeam(shooterEntity, player));
    }

    private void handleExplosionEffect(ProjectileHitEvent event) {
        event.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, event.getEntity().getLocation(), 1, 0.6, 0.6, 0.6, 0);
        event.getEntity().getWorld().spawnParticle(Particle.FLAME, event.getEntity().getLocation(), 20, 1, 1, 1, 0.3);
        event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5, 1);
    }
}