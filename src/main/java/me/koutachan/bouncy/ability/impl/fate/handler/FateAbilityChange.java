package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.AbilityUtils;

public class FateAbilityChange implements FateHandler {
    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        if (success) {
            gamePlayer.sendMessage("&a[能力発動] 能力って本当に変わるんですね");
            AbilityUtils.randomAbility(gamePlayer.getPlayer());
        } else {
            gamePlayer.sendMessage("&4[能力発動失敗] 能力が変わったらチートだろ！！");
        }
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> 能力が変わる予感...");
    }

    @Override
    public int getSuccessfulWeight() {
        return 2;
    }

    @Override
    public int getWeight() {
        return 4;
    }
}
