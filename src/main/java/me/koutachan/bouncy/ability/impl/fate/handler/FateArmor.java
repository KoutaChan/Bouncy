package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class FateArmor implements FateHandler {
    public static ItemStack PROTECTION_ARMOR = new ItemStack(Material.GOLDEN_CHESTPLATE) {{
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "防護服");
        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(1);
            damageable.setDamage(1);
        }
        setItemMeta(meta);
    }};

    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        if (success) {
            gamePlayer.sendMessage("&a[能力発動] 防具がどこからか出現？！");
            gamePlayer.getInventory().addItem(PROTECTION_ARMOR);
        } else {
            gamePlayer.sendMessage("&4[能力発動失敗] 思い違いだったようだ...");
        }
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> 懐から...?");
    }
}