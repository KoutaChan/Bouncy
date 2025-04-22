package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FateCrossbow implements FateHandler {
    private final static ItemStack CROSSBOW = new ItemStack(Material.CROSSBOW);

    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&a[能力発動] クロスボウをゲット！");
        gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() + 5);
        gamePlayer.getPlayer().getInventory().addItem(CROSSBOW);
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> クロスボウ配布会");
    }

    @Override
    public int getWeight() {
        return 1;
    }
}