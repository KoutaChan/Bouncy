package me.koutachan.bouncy.ability.impl.special_thanks;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.ability.AbilityShoot;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import me.koutachan.bouncy.utils.ThroughWallArrowUtils;
import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class TrueArrowAbility extends Ability implements AbilityShoot, AbilityDrop {
    public final static int TRUE_ARROW_RADIUS = 10;
    public final static int TRUE_ARROW_TICKS = 300;

    private final static Map<UUID, ArrowData> dataMap = new HashMap<>();
    private TrueArrowTask task;

    public final static int ID = 999;

    public TrueArrowAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public String getName() {
        return "透矢";
    }

    @Override
    public String getActionBar() {
        return "能力:透矢 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 600;
    }

    @Override
    public int getId() {
        return ID;
    }

    public static ArrowData getArrowData(UUID uuid) {
        return dataMap.get(uuid);
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        if (task != null && event.getEntity() instanceof AbstractArrow abstractArrow) {
            AbstractArrow arrow = ThroughWallArrowUtils.trySpawnThroughWallArrow(abstractArrow, ThroughWallArrowUtils.ThroughMode.TRUE_ARROW);
            if (arrow != null) {
                dataMap.put(arrow.getUniqueId(), new ArrowData(arrow, arrow.getVelocity(), this));
                event.setCancelled(true);
            }
        }
    }

    public void removeAllArrowData() {
        dataMap.values().removeIf(arrowData -> arrowData.ability == this);
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            if (task != null) {
                task.cancel();
            }
            task = new TrueArrowTask(gamePlayer.getLocation());
            task.start();
        }
    }

    public class ArrowData {
        private final AbstractArrow arrow;
        private final Vector velocity;
        private final TrueArrowAbility ability;

        public ArrowData(AbstractArrow arrow, Vector velocity, TrueArrowAbility ability) {
            this.arrow = arrow;
            this.velocity = velocity;
            this.ability = ability;
        }

        public AbstractArrow getArrow() {
            return arrow;
        }

        public boolean onTick() { // true の場合壁を貫通する
            boolean inArea = task.center.distanceSquared(arrow.getLocation()) <= TRUE_ARROW_RADIUS * TRUE_ARROW_RADIUS;
            if (inArea) {
                arrow.setVelocity(velocity);
            }
            return inArea;
        }

        public TrueArrowTask getTask() {
            return task;
        }
    }

    public class TrueArrowTask extends BukkitRunnable {
        private final Location center;
        private int ticks;

        public TrueArrowTask(Location center) {
            this.center = center.clone();
            this.ticks = TRUE_ARROW_TICKS;
        }

        public void start() {
            runTaskTimer(Bouncy.INSTANCE, 0L, 1L);
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            removeAllArrowData();
            task = null;
        }

        @Override
        public void run() {
            if (ticks <= 0) {
                cancel();
                return;
            }
            ticks--;
        }
    }
}