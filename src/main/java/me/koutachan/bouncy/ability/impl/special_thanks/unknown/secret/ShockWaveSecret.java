package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.HitMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.KillMeta;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ShockWaveSecret extends SkillSecret {
    public final static double MAX_RADIUS = 6;
    public final static int INTERVAL = 200;

    private int ticks;

    public ShockWaveSecret(GamePlayer gamePlayer, TriggerType type) {
        super(gamePlayer, type);
    }

    @Override
    public void onActivated(TriggerMeta meta) {
        if (type == TriggerType.TICK) {
            if (ticks++ >= INTERVAL) {
                ticks = 0;
            } else {
                return;
            }
        }
        Location pos = switch (type) {
            case HIT -> ((HitMeta) meta).victim().getLocation();
            case KILL -> ((KillMeta) meta).victim().getLocation();
            default -> gamePlayer.getLocation();
        };
        new ShockWaveTask(pos).start();
    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {

    }

    @Override
    public String asMessage() {
        return switch (type) {
            case HIT -> "ヒット時、ショックウェーブを発生させる";
            case KILL -> "敵を殺したとき、ショックウェーブを発生させる";
            case TICK -> (INTERVAL / 20) + "秒ごとに、ショックウェーブを発生させる";
            case DAMAGE -> "ダメージを受けたとき、ショックウェーブを発生させる";
            case SHOOT -> "矢を打ったとき、ショックウェーブを発生させる";
            case DRINK_POTION -> "ポーションを飲んだとき、ショックウェーブを発生させる";
            case DROP_1 -> "スキルを1回発動させたとき、ショックウェーブを発生させる";
            case DROP_2 -> "スキルを2回発動させたとき、ショックウェーブを発生させる";
            case JUMP_5 -> "ジャンプを5回したとき、ショックウェーブを発生させる";
            case JUMP_10 -> "ジャンプを10回したとき、ショックウェーブを発生させる";
        };
    }

    public class ShockWaveTask extends BukkitRunnable {
        private double currentRadius = 0.0;
        private final List<Entity> attacked = new ArrayList<>();
        private final Location center;

        public ShockWaveTask(Location center) {
            this.center = center;
        }

        public void start() {
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
            runTaskTimer(Bouncy.INSTANCE, 2, 0);
        }

        @Override
        public void run() {
            if (currentRadius >= MAX_RADIUS) {
                this.cancel();
                return;
            }
            createParticleRing(center, currentRadius);
            for (Entity entity : center.getWorld().getNearbyEntities(center, currentRadius + 1, 3, currentRadius + 1)) {
                if (DamageUtils.isSameTeam(gamePlayer.getPlayer(), entity) || attacked.contains(entity))
                    continue;
                if (!(entity instanceof Player player) || player.getGameMode() == GameMode.SPECTATOR)
                    continue;

                final double distance = entity.getLocation().distance(center);
                if (distance <= currentRadius + 0.5 && distance >= currentRadius - 0.5) {
                    DamageUtils.damage(gamePlayer.getPlayer(), entity, 1);

                    Vector direction = entity.getLocation().subtract(center).toVector();
                    entity.setVelocity(direction.setY(0.3).normalize().multiply(.5));
                    attacked.add(entity);

                    entity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, entity.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0);
                }
            }
            currentRadius += 0.2F;
        }

        private void createParticleRing(Location center, double radius) {
            World world = center.getWorld();
            if (world == null) return;

            int particles = Math.max(16, Math.min((int) (radius * 8), 64));
            for (int i = 0; i < particles; i++) {
                double angle = 2 * Math.PI * i / particles;
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);
                double y = center.getY();
                Location location = new Location(world, x, y, z);
                location.setY(location.getY() + 0.1);
                world.spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.fromRGB(255, 100, 0), 1.5f));
                if (Math.random() < 0.3) {
                    world.spawnParticle(Particle.SMALL_GUST, location, 1, 0, 0, 0, 0.1);
                }
            }
        }
    }


}