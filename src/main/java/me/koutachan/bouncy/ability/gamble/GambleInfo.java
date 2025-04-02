package me.koutachan.bouncy.ability.gamble;

import org.bukkit.inventory.ItemStack;

public interface GambleInfo {
    GambleConsumer getGambleConsumer();

    ItemStack getDescription();

    GambleBuffType getType();
}
