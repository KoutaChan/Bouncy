package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.attribute.Attribute;

public class FateHeal implements FateHandler {
    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        if (success) {
            gamePlayer.sendMessage("&a[能力発動] 謎の力によって体力が回復した・・・！");
            gamePlayer.getPlayer().setHealth(Math.max(gamePlayer.getPlayer().getAttribute(Attribute.MAX_HEALTH).getValue(), gamePlayer.getPlayer().getHealth() + 2));
        } else {
            gamePlayer.sendMessage("&4[能力発動失敗] そんなあなたにはダメージをプレゼントーー！");
            gamePlayer.getPlayer().damage(2);
        }
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> 体力が回復する予感！");
    }
}