package me.koutachan.bouncy.game.task;

import me.koutachan.bouncy.game.GamePlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameTaskHandler {
    private final GamePlayer gamePlayer;

    private final Map<Class<? extends GameTask>, GameTask> tasks = new HashMap<>();
    private boolean paused;

    public GameTaskHandler(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    @SuppressWarnings("unchecked")
    public <T extends GameTask> T register(Class<T> clazz, boolean overwrite) {
        try {
            T task = clazz.getConstructor(GamePlayer.class).newInstance(gamePlayer);
            GameTask previousTask = overwrite ? tasks.put(clazz, task) : tasks.putIfAbsent(clazz, task);
            if (previousTask != null) {
                if (overwrite) {
                    previousTask.onStop();
                } else {
                    return (T) previousTask;
                }
            }
            task.start();
            if (!task.isDefinedTask()) {
                throw new IllegalStateException("Task is undefined! " + clazz.getName());
            }
            return task;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create task: " + clazz.getName(), e);
        }
    }

    public <T extends GameTask> T register(Class<T> clazz) {
        return register(clazz, false);
    }

    public <T extends GameTask> T registerOverwrite(Class<T> clazz) {
        return register(clazz, true);
    }

    public void pauseTasks() {
        if (!paused) {
            paused = true;
            tasks.forEach((clazz, task) -> task.cancel());
        }
    }

    public void resumeTasks() {
        if (paused) {
            paused = false;
            tasks.forEach((clazz, task) -> task.start());
        }
    }

    public void unregister(Class<? extends GameTask> clazz) {
        GameTask task = tasks.remove(clazz);
        if (task != null) {
            task.safeCancel();
        }
    }

    public void unregisterAll() {
        for (Iterator<GameTask> it = tasks.values().iterator(); it.hasNext();) {
            GameTask task = it.next();
            it.remove();
            task.safeCancel();
        }
    }

    @ApiStatus.Internal
    public void unregisterInternal(GameTask task) {
        if (!paused || task.forceRemove()) { // Freeze Task
            tasks.values().remove(task);
        }
        task.onStop();
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
}