package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FatePain implements FateHandler {
    public final static PotionEffect SLOWNESS = new PotionEffect(PotionEffectType.SLOWNESS, 15 * 20, 4, false, false);

    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        if (success) {
            gamePlayer.sendMessage("&4[能力発動] ギックリ腰になる運命だった！！");
            gamePlayer.addPotionEffect(SLOWNESS);
        } else {
            gamePlayer.sendMessage("&a[能力発動失敗] ならなくてよかったね；；");
        }
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> ギックリ腰の予感！");
    }
}