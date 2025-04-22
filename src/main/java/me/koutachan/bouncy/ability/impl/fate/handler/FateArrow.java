package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FateArrow implements FateHandler {
    public final static PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 20 * 20, 4, false, false);

    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        if (success) {
            gamePlayer.sendMessage("&a[能力発動] 敵を皆殺しにしろ！");
            gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() + 5);
            gamePlayer.getPlayer().addPotionEffect(SPEED);
        } else {
            gamePlayer.sendMessage("&4[能力発動失敗] 勘違いだったようだ...");
        }
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> 敵を皆殺しにする能力の予感");
    }
}