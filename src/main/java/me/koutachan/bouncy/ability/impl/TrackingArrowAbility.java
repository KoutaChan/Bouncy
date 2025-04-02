package me.koutachan.bouncy.ability.impl;

import de.metaphoriker.pathetic.api.pathing.filter.PathFilter;
import de.metaphoriker.pathetic.api.pathing.filter.PathValidationContext;
import de.metaphoriker.pathetic.api.provider.NavigationPointProvider;
import de.metaphoriker.pathetic.api.wrapper.PathPosition;
import de.metaphoriker.pathetic.bukkit.mapper.BukkitMapper;
import de.metaphoriker.pathetic.bukkit.provider.BukkitNavigationPoint;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.*;
import java.util.stream.Collectors;

public class TrackingArrowAbility extends Ability implements AbilityDrop {
    private Player target;

    private Player targeting;
    private long lookTick;

    public final static int ID = 34;

    public final static double STEP_SIZE = 0.5d;
    public final static double TARGET_SIZE = 3.5;

    public final static int TARGET_TICK = 60;
    public final static int TITLE_SHOW_COUNT = 10;

    private final static Particle.DustTransition TRACKING_DUST = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 0, 0), 1F);

    private final List<Location> footprints = new ArrayList<>();
    private final static int FOOT_PRINT_REMOVE_TICK = 60;
    private final static int FOOT_PRINT_COLLECT_TICK = 2;
    private int tick;

    private final static int MAX_TARGET = 3;
    private int count = 0;

    public TrackingArrowAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());

        /*
         * 対象のプレイヤー
         */
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(o -> !DamageUtils.isSameTeam(o, gamePlayer.getPlayer()) && o.getGameMode() == GameMode.ADVENTURE)
                .collect(Collectors.toList());

        /*
         * 足跡処理
         */
        if (++tick >= FOOT_PRINT_COLLECT_TICK) {
            List<Location> newFootprints = new ArrayList<>();
            for (Player player : players) {
                if (player.isOnGround()) {
                    newFootprints.add(player.getLocation());
                }
            }
            footprints.addAll(newFootprints);
            Bukkit.getScheduler().runTaskLater(Bouncy.INSTANCE, () -> footprints.removeAll(newFootprints), FOOT_PRINT_REMOVE_TICK);
            tick = 0;
        }

        for (Location footprint : footprints) {
            gamePlayer.getPlayer().spawnParticle(Particle.DUST_COLOR_TRANSITION, footprint, 1, 0, 0, 0, TRACKING_DUST);
        }

        if (!gamePlayer.getPlayer().isSneaking() || gamePlayer.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        /*
         * 追尾判定付与
         */
        Vector direction = gamePlayer.getEyeLocation().getDirection();
        Location loopPos = gamePlayer.getEyeLocation().clone();

        if (target != null) { // Optimize performance;
            players.remove(target);
        }

        for (double traveled = 0; traveled < 100; traveled += STEP_SIZE) {
            Location newPos = loopPos.add(direction.clone().multiply(STEP_SIZE));

            if (newPos.getWorld().getBlockAt(newPos).getType().isSolid()) {
                return;
            }

            for (Player player : players) {
                if (newPos.distanceSquared(player.getEyeLocation()) <= TARGET_SIZE) {
                    if (targeting != null && targeting != player) {
                        targeting = null;
                        lookTick = 0;
                    } else {
                        targeting = player;
                        if (++lookTick >= TARGET_TICK) {
                            if (lookTick == TARGET_TICK) {
                                gamePlayer.getPlayer().sendTitle("", ChatColor.GREEN + "追尾完了", 10, 70, 20);
                                gamePlayer.getPlayer().playSound(gamePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            }
                            lookTick = 0;
                            target = targeting;
                            count = MAX_TARGET;
                        } else if (lookTick % 5 == 0) {
                            gamePlayer.getPlayer().sendTitle("", "追尾中 [" + getChargeStyle() + "]", 10, 70, 20);
                            gamePlayer.getPlayer().playSound(gamePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
                        }
                    }
                    return;
                }
            }
        }
    }

    private String getChargeStyle() {
        int progress = (int) ((double) lookTick / TARGET_TICK * TITLE_SHOW_COUNT);
        return (ChatColor.GREEN + "|").repeat(progress) + (ChatColor.GRAY + "|").repeat(TITLE_SHOW_COUNT - progress);
    }

    @Override
    public void onDrop() {
        if (target != null && gamePlayer.useAbility(getCt())) {
            new TrackingArrowTask(gamePlayer.getEyeLocation().getBlock().getLocation().add(0.5, 0.5, 0.5), gamePlayer.getPlayer(), target).runTaskTimer(Bouncy.INSTANCE, 0, 1);
            if (0 >= --count) {
                target = null;
            }
        }
    }

    private static class TrackingArrowTask extends BukkitRunnable {
        private final Arrow arrow;
        private final Player target;

        private final static int ARROW_TARGET_WARNING = 10 * 10;
        private final static int WARNING_TICK = 20 * 5;

        public final static double OFFSET_VECTOR = 1;
        public final static double OFFSET_CHECK_SIZE = 1;

        public final static int REMOVE_TICK = 400;
        private int tick;
        private int warningTick;

        public TrackingArrowTask(Location location, Player shooter, Player target) {
            this.arrow = createArrow(location, shooter);
            this.target = target;
        }

        @Override
        public void run() {
            if (++tick >= REMOVE_TICK || arrow.isDead() || target.getGameMode() != GameMode.ADVENTURE || arrow.isOnGround() || !target.getWorld().equals(arrow.getWorld())) {
                cancel();
            } else {
                if (arrow.getLocation().distanceSquared(target.getLocation()) <= ARROW_TARGET_WARNING) {
                    if (0 > --warningTick) {
                        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
                        target.sendMessage(ChatColor.RED + "[警告] 追尾されています！");
                        warningTick = WARNING_TICK;
                    }
                }
                Location arrowPos = arrow.getLocation();
                Location targetPos = target.getEyeLocation();

                Vector direction = targetPos.toVector().subtract(arrowPos.toVector()).normalize();
                if (arrowPos.getWorld().rayTraceBlocks(arrowPos, direction, OFFSET_CHECK_SIZE) != null) {
                    direction = avoidObstacle(arrow, direction);
                }
                arrow.setVelocity(direction.multiply(0.5));
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            arrow.remove();
            super.cancel();
        }

        public static Arrow createArrow(Location location, Player shooter) {
            Arrow arrow = location.getWorld().spawn(location, Arrow.class, bukkitArrow -> {
                bukkitArrow.setGravity(false);
                bukkitArrow.setGlowing(true);
            });
            arrow.setShooter(shooter); // Patch: use arrow bug
            return arrow;
        }

        private Vector avoidObstacle(Arrow arrow, Vector direction) {
            List<Vector> possibleDirections = new ArrayList<>() {{
                    add(new Vector(OFFSET_VECTOR, 0, 0));
                    add(new Vector(-OFFSET_VECTOR, 0, 0));
                    add(new Vector(0, OFFSET_VECTOR, 0));
                    add(new Vector(0, -OFFSET_VECTOR, 0));
                    add(new Vector(0, 0, OFFSET_VECTOR));
                    add(new Vector(0, 0, -OFFSET_VECTOR));
            }};
            Collections.shuffle(possibleDirections);
            for (Vector possibleDirection : possibleDirections) {
                Vector newDirection = direction.clone().add(possibleDirection).normalize();
                if (arrow.getWorld().rayTraceBlocks(arrow.getLocation(), newDirection, OFFSET_CHECK_SIZE) == null) {
                    return newDirection;
                }
            }
            return direction;
        }
    }

    @Override
    public String getName() {
        return "追尾";
    }

    @Override
    public String getActionBar() {
        return "能力: 追尾 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒（" + FormatUtils.formatTick(getCt()) + "秒で使用可能 | 追尾中" + getCount() + ": " + getTarget() + "）";
    }

    public String getCount() {
        return switch (count) {
            case 3 -> ChatColor.GREEN + "③";
            case 2 -> ChatColor.YELLOW + "②";
            case 1 -> ChatColor.RED + "①";
            default -> "";
        } + ChatColor.WHITE;
    }

    public String getTarget() {
        return target != null ? target.getName() : "不明";
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