package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;

public interface FateHandler {
    void onFate(GamePlayer gamePlayer, int weight, boolean success);

    void onChosen(GamePlayer gamePlayer, int weight, boolean success);

    default int getSuccessfulWeight() {
        return 1;
    }

    default int getWeight() {
        return 2;
    }
}