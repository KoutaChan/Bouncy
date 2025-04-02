package me.koutachan.bouncy.utils;

import com.google.common.base.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemCreator {
    private Material material;
    private int amount = 1;
    private List<String> lore = new ArrayList<>();
    private Set<ItemFlag> itemFlags = new HashSet<>();
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private String displayName;
    private boolean unbreakable;
    private boolean glow;
    private int customModelData;

    public ItemCreator(Material material) {
        this.material = material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemCreator setDisplayName(String displayName) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        return this;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemCreator addLore(String... lores) {
        for (String lore : lores) {
            this.lore.add(ChatColor.translateAlternateColorCodes('&', lore));
        }
        return this;
    }

    public ItemCreator setLore(String... lores) {
        lore = new ArrayList<>();
        for (String lore : lores) {
            this.lore.add(ChatColor.translateAlternateColorCodes('&', lore));
        }
        return this;
    }

    public ItemCreator setLore(List<String> lores) {
        lore = new ArrayList<>();
        for (String lore : lores) {
            this.lore.add(ChatColor.translateAlternateColorCodes('&', lore));
        }
        return this;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public ItemCreator setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public Set<ItemFlag> getItemFlags() {
        return itemFlags;
    }

    public ItemCreator addItemFlags(ItemFlag... flags) {
        this.itemFlags.addAll(Arrays.asList(flags));
        return this;
    }

    public ItemCreator removeItemFlags(ItemFlag... flags) {
        Arrays.asList(flags).forEach(this.itemFlags::remove);
        return this;
    }

    public ItemCreator setItemFlags(Set<ItemFlag> flags) {
        this.itemFlags = flags;
        return this;
    }

    public ItemCreator addEnchantments(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
        return this;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public ItemCreator setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public boolean isGlow() {
        return glow;
    }

    public ItemCreator setGlow(boolean glow) {
        this.glow = glow;
        return this;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public ItemCreator setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }


    public ItemCreator setType(Material material) {
        this.material = material;
        return this;
    }

    public ItemStack create() {
        return new ItemStack(material, amount) {{
            ItemMeta meta = getItemMeta();
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (glow) {
                meta.addEnchant(Enchantment.LURE, 1, true);
            }
            enchantments.forEach(((enchantment, level) -> meta.addEnchant(enchantment, level, true)));

            meta.setLore(lore);
            meta.setUnbreakable(unbreakable);
            meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
            meta.setCustomModelData(customModelData);
            setItemMeta(meta);
        }};
    }

    public static ItemCreator of(Material material) {
        return new ItemCreator(material);
    }

    public static ItemCreator of(Material material, String displayName, int amount) {
        return new ItemCreator(material)
                .setDisplayName(displayName)
                .setAmount(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemCreator that)) return false;
        return unbreakable == that.unbreakable && material == that.material && Objects.equal(lore, that.lore) && Objects.equal(itemFlags, that.itemFlags) && Objects.equal(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(material, lore, itemFlags, displayName, unbreakable);
    }
}