package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FateHealthBoost implements FateHandler {
    public PotionEffect HEALTH_BOOST = new PotionEffect(PotionEffectType.HEALTH_BOOST, -1, 0, false, false);

    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        if (success) {
            gamePlayer.sendMessage("&a[能力発動] 体力が増えちまった...");
            gamePlayer.getPlayer().addPotionEffect(HEALTH_BOOST);
        } else {
            gamePlayer.sendMessage("&4[能力発動失敗] 体力が増えるわけないｗｗ");
        }
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> 体力が増える予感！");
    }
}