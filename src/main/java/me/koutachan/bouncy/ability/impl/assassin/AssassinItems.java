package me.koutachan.bouncy.ability.impl.assassin;

import me.koutachan.bouncy.Bouncy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class AssassinItems {
    public static NamespacedKey ASSASSIN_NAMESPACED_KEY = new NamespacedKey(Bouncy.INSTANCE, "assassin");

    public static ItemStack ASSASSIN_ITEMS = new ItemStack(Material.NETHER_STAR) {{
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "暗殺報酬");
        meta.setLore(List.of(ChatColor.YELLOW + "クリックで暗殺報酬を受け取る"));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(ASSASSIN_NAMESPACED_KEY, PersistentDataType.BOOLEAN, true);
        setItemMeta(meta);
    }};
}