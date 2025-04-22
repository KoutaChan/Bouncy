package me.koutachan.bouncy.ability.impl.fate.handler;

import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class FateBattleAxe implements FateHandler {
    private static final ItemStack BATTLE_AXE = new ItemStack(Material.IRON_AXE) {{
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "戦闘斧");
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(1);
            damageable.setDamage(1);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(NamespacedKey.fromString("bouncy"), 999, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        setItemMeta(meta);
    }};

    @Override
    public void onFate(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&a[能力発動] 戦闘斧を獲得した..");
        gamePlayer.getInventory().addItem(BATTLE_AXE);
    }

    @Override
    public void onChosen(GamePlayer gamePlayer, int weight, boolean success) {
        gamePlayer.sendMessage("&e[!] >> 懐から...?");
    }

    @Override
    public int getWeight() {
        return 1;
    }
}
