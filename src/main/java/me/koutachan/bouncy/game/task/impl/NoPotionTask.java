package me.koutachan.bouncy.game.task.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.gamble.GambleDeBuff;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.game.task.GameTask;
import org.bukkit.Material;

public class NoPotionTask extends GameTask {
    private final static int NO_POTION_TIME = 20 * 60;
    private int leftTime = NO_POTION_TIME;

    public NoPotionTask(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void run() {
        if (0 > leftTime--) {
            gamePlayer.getActiveDeBuffs().remove(GambleDeBuff.CANNOT_USE_HEAL_POTION);
            cancel();
        }
    }

    @Override
    public void start() {
        gamePlayer.getPlayer().setCooldown(Material.POTION, leftTime); // for rejoin or some kick.
        runTaskTimer(Bouncy.INSTANCE, 0, 1);
    }
}