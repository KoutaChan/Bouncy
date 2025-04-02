package me.koutachan.bouncy.ability.gamble;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class Gambler {
    public final static int USE_DELAY_TICK = 20 * 15;
    public final static int USE_DELAY_MILLS = (USE_DELAY_TICK / 20) * 1000;

    public static ItemStack GAMBLE_ITEM;
    public static NamespacedKey GAMBLE_NAMESPACED_KEY = new NamespacedKey(Bouncy.INSTANCE, "gamble");
    static {
        var gambleItem = ItemCreator.of(Material.BOOK)
                .setDisplayName("&a賭博の本")
                .setGlow(true)
                .addLore("&bMade in South Korea (REPUBLIC OF KOREA)")
                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                .setUnbreakable(true)
                .create();
        var gambleMeta = gambleItem.getItemMeta();
        gambleMeta.getPersistentDataContainer().set(GAMBLE_NAMESPACED_KEY, PersistentDataType.BOOLEAN, true);
        gambleItem.setItemMeta(gambleMeta);
        GAMBLE_ITEM = gambleItem;
    }
}