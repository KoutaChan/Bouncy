package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import me.koutachan.bouncy.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvincibleAbility extends Ability implements AbilityDrop {
    private final static int RESISTANCE_TIME = 20 * 7;
    private final static PotionEffect RESISTANCE_EFFECT = new PotionEffect(PotionEffectType.RESISTANCE, RESISTANCE_TIME, 4, true, false);
    private final static ItemStack[] ITEMS = new ItemStack[]{
            ItemCreator.of(Material.NETHERITE_BOOTS).addEnchantments(Enchantment.BINDING_CURSE, 1).addItemFlags(ItemFlag.HIDE_ENCHANTS).create(),
            ItemCreator.of(Material.NETHERITE_LEGGINGS).addEnchantments(Enchantment.BINDING_CURSE, 1).addItemFlags(ItemFlag.HIDE_ENCHANTS).create(),
            ItemCreator.of(Material.NETHERITE_CHESTPLATE).addEnchantments(Enchantment.BINDING_CURSE, 1).addItemFlags(ItemFlag.HIDE_ENCHANTS).create(),
            ItemCreator.of(Material.NETHERITE_HELMET).addEnchantments(Enchantment.BINDING_CURSE, 1).addItemFlags(ItemFlag.HIDE_ENCHANTS).create()
    };

    public final static int ID = 8;

    public InvincibleAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            gamePlayer.addPotionEffect(RESISTANCE_EFFECT);
            gamePlayer.getInventory().setArmorContents(ITEMS);
            gamePlayer.playSoundPublic(gamePlayer.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 0.7f);
            Bukkit.getScheduler().runTaskLater(Bouncy.INSTANCE, () -> gamePlayer.getInventory().setArmorContents(null), RESISTANCE_TIME);
        }
    }

    @Override
    public String getName() {
        return "無敵";
    }

    @Override
    public String getActionBar() {
        return "能力:無敵 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 600;
    }

    @Override
    public int getId() {
        return ID;
    }
}