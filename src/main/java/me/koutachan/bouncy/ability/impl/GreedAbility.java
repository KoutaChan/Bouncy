package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityAttack;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GreedAbility extends Ability implements AbilityAttack {
    public final static int ID = 47;

    public GreedAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onAttack(Player victim) {
        PlayerInventory victimInventory = victim.getInventory();
        PotionEffect regenPotion = new PotionEffect(PotionEffectType.REGENERATION, 2, 29, true, false);
        for (ItemStack item : victimInventory.getContents()) {
            if (item != null && item.hasItemMeta() && item.getType() == Material.POTION) {
                if (item.getItemMeta() instanceof PotionMeta potionMeta && potionMeta.hasCustomEffects()) {
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        if (effect.equals(regenPotion)) {
                            if (Bouncy.SECURE_RANDOM.nextDouble() < 0.25) {
                                ItemStack stolenPotion = item.clone();
                                stolenPotion.setAmount(1);
                                victimInventory.removeItem(stolenPotion);
                                ItemMeta meta = stolenPotion.getItemMeta();
                                meta.setMaxStackSize(4);
                                stolenPotion.setItemMeta(meta);
                                gamePlayer.getPlayer().getInventory().addItem(stolenPotion);
                                gamePlayer.sendMessage("&a&l[!] 強欲 の効果で相手のポーションを盗んだ！(25%)");
                                victim.sendMessage("&c&l[!] 強欲 の効果でポーションを盗まれた！");
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getActionBar() {
        return "";
    }

    @Override
    public int getCt() {
        return 0;
    }

    @Override
    public int getId() {
        return ID;
    }
}
