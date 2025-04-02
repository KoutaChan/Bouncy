package me.koutachan.bouncy.game.task.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.gamble.GambleDeBuff;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.game.task.GameTask;

public class NoExpTask extends GameTask {
    private final static int NO_EXP_TIME = 20 * 30;

    private int time;

    public NoExpTask(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void run() {
        if (time++ > NO_EXP_TIME) {
            gamePlayer.getActiveDeBuffs().remove(GambleDeBuff.NO_ARROW_CHARGE);
            cancel();
        }
        gamePlayer.getPlayer().setExp(0);
    }

    @Override
    public void start() {
        runTaskTimer(Bouncy.INSTANCE, 0, 1);
    }
}