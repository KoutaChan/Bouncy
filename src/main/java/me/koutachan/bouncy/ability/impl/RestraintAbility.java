package me.koutachan.bouncy.ability.impl;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.nimbusds.jose.crypto.bc.BouncyCastleFIPSProviderSingleton;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RestraintAbility extends Ability implements AbilityDrop {
    public final static int ID = 4;

    public RestraintAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onDrop() {
        if (gamePlayer.getAbilityCt() >= getCt()) {
            RestraintTask task = null;
            if (gamePlayer.getPlayer().isSneaking()) {
                task = new RestraintTask(gamePlayer.getPlayer(), gamePlayer.getLocation());
            } else {
                Block lookBlock = gamePlayer.getPlayer().getTargetBlockExact(100, FluidCollisionMode.NEVER);
                if (lookBlock != null) {
                    Location pos = lookBlock.getLocation().subtract(gamePlayer.getEyeLocation().getDirection().multiply(2));
                    task = new RestraintTask(gamePlayer.getPlayer(), pos);
                }
            }
            if (task != null && !task.isOutsideBorder()) {
                task.start();
                showParticleLine(Particle.FIREWORK, gamePlayer.getEyeLocation(), task.pos, 100);
                gamePlayer.setAbilityCt(0);
            }
        }
    }

    public static void showParticleLine(Particle particle, Location start, Location end, double max) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);

        double particleCount = Math.min(max, distance);
        for (int i = 0; i < particleCount; i++) {
            double progress = i / particleCount;
            Vector particlePos = start.toVector().add(direction.clone().multiply(distance * progress));
            start.getWorld().spawnParticle(particle, particlePos.getX(), particlePos.getY(), particlePos.getZ(), 1, 0, 0, 0, 0);
        }
    }

    public static class RestraintTask extends BukkitRunnable {
        private final Player attacker;
        private final Location pos;

        private int tick;

        private BlockDisplay restraintDisplay;

        public final static int MAX_TICK = 20 * 3;
        public final static double RESTRAINT_RADIUS = 10;
        public final static double PULL_STRENGTH = 0.5;

        public final static int PARTICLES_PER_CIRCLE = MAX_TICK / 2;
        public final static List<Vector> PARTICLE_LIST = new ArrayList<>();
        static {
            for (int i = 0; i < PARTICLES_PER_CIRCLE; i++) {
                double angle = 2 * Math.PI * i / PARTICLES_PER_CIRCLE;
                double x = RESTRAINT_RADIUS * Math.cos(angle);
                double z = RESTRAINT_RADIUS * Math.sin(angle);
                PARTICLE_LIST.add(new Vector(x, 0, z));
            }
        }

        public RestraintTask(Player attacker, Location pos) {
            this.attacker = attacker;
            this.pos = pos;
            this.pos.setYaw(0);
            this.pos.setPitch(0);
        }

        @Override
        public void run() {
            if (++tick > MAX_TICK || isOutsideBorder()) {
                cancel();
            } else {
                for (int i = 0; i < 5; i++) {
                    Location particlePos = pos.clone().add(PARTICLE_LIST.get((tick + i) % (PARTICLE_LIST.size() - 1)));
                    particlePos.getWorld().spawnParticle(Particle.WITCH, particlePos, 1);
                }
                for (Player player : pos.getWorld().getPlayers()) {
                    if (DamageUtils.isSameTeam(attacker, player)
                            || player.getGameMode() != GameMode.ADVENTURE
                            || player.getLocation().distanceSquared(pos) > RESTRAINT_RADIUS * RESTRAINT_RADIUS) {
                        continue;
                    }
                    Vector directionToPos = pos.toVector().subtract(player.getLocation().toVector()).normalize();
                    double distanceToPos = player.getLocation().distance(pos);
                    double pullStrengthScaled = Math.min(PULL_STRENGTH, distanceToPos / 2);

                    Vector pullVector = player.getVelocity().multiply(0.2);
                    if (distanceToPos > PULL_STRENGTH * PULL_STRENGTH) {
                        pullVector.add(directionToPos.multiply(pullStrengthScaled));
                    }

                    if (Double.isFinite(pullVector.getX()) && Double.isFinite(pullVector.getY()) && Double.isFinite(pullVector.getZ())) {
                        player.setVelocity(pullVector);
                    }
                    showParticleLine(Particle.DRAGON_BREATH, player.getEyeLocation(), pos, RESTRAINT_RADIUS);
                }
            }
        }

        public boolean isOutsideBorder() {
            return !pos.getWorld().getWorldBorder().isInside(pos);
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            restraintDisplay.remove();
            super.cancel();
        }

        public void spawnRestraint() {
            restraintDisplay = pos.getWorld().spawn(pos, BlockDisplay.class, blockDisplay -> {
                blockDisplay.setInvulnerable(true);
                blockDisplay.addScoreboardTag("restraint");
                blockDisplay.setBlock(Material.COAL_BLOCK.createBlockData());
                blockDisplay.setGlowing(true);
                blockDisplay.setGlowColorOverride(Color.WHITE);
            });
        }

        public void start() {
            spawnRestraint();
            runTaskTimer(Bouncy.INSTANCE, 0, 1);
        }
    }

    @Override
    public String getName() {
        return "拘束";
    }

    @Override
    public String getActionBar() {
        return "能力:拘束 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 600;
    }

    @Override
    public int getId() {
        return ID;
    }
}