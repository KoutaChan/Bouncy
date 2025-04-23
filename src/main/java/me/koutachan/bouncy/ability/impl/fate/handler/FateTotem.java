package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FateTotem implements FateHandler {
    public static ItemStack TOTEM_OF_UNDYING = ItemCreator.of(Material.TOTEM_OF_UNDYING)
            .create();

    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        if (success) {
            gamePlayer.sendMessage("&a[能力発動] トーテムを獲得！");
            gamePlayer.getInventory().addItem(TOTEM_OF_UNDYING);
        } else {
            gamePlayer.sendMessage("&4[能力発動失敗] なにもなかった..");
        }
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> 懐から...?");
    }
}