package me.koutachan.bouncy.game.task.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.game.task.GameTask;
import org.bukkit.Location;

public class RandomRotationTask extends GameTask {
    public final static int RANDOM_ROTATION_DELAY = 10 * 20;

    public RandomRotationTask(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void run() {
        if (gamePlayer.isOpenInventory()) {
            return;
        }
        Location location = gamePlayer.getLocation();
        location.setYaw(Bouncy.SECURE_RANDOM.nextFloat(-180, 180));
        location.setPitch(Bouncy.SECURE_RANDOM.nextFloat(-90, 90));
        gamePlayer.getPlayer().teleport(location);
    }

    @Override
    public void start() {
        runTaskTimer(Bouncy.INSTANCE, RANDOM_ROTATION_DELAY, RANDOM_ROTATION_DELAY);
    }
}