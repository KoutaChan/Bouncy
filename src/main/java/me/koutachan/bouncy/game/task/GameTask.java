package me.koutachan.bouncy.game.task;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.logging.Level;

public abstract class GameTask extends BukkitRunnable {
    protected final GamePlayer gamePlayer;

    public GameTask(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public abstract void start();

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void onStop() {
    }

    public boolean forceRemove() {
        return false;
    }

    public boolean isDefinedTask() {
        try {
            Field task = BukkitRunnable.class.getDeclaredField("task");
            task.setAccessible(true);
            return task.get(this) != null;
        } catch (Exception ex) {
            Bouncy.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve BukkitTask. clazz=" + getClass().getName(), ex);
            return false;
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        try {
            Field task = BukkitRunnable.class.getDeclaredField("task");
            task.setAccessible(true);
            task.set(this, null);
        } catch (Exception ex) {
            Bouncy.INSTANCE.getLogger().log(Level.SEVERE, "Failed to recycle GameTask. clazz=" + getClass().getName(), ex);
        }
        gamePlayer.getTaskHandler().unregisterInternal(this);
    }
}