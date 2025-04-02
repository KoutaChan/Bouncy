package me.koutachan.bouncy.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;


public class AbilityUtils {
    public static ItemStack HEAL_POTION = new ItemStack(Material.POTION) {{
        var meta = (PotionMeta) getItemMeta();
        meta.setDisplayName(ChatColor.RED + "回復ポーション");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 2, 29, true, false), true);
        meta.setColor(Color.fromARGB(16732788));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        setItemMeta(meta);
    }};

    public static void changeAbility(Player player, int abilityId) {
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("ability");
        if (objective != null) {
            objective.getScore(player.getName()).setScore(abilityId);
        }
    }

    public static void randomAbility(Player player) {
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("yaminabe");
        if (objective != null) {
            objective.getScore(player.getName()).setScore(1200);
        }
    }

    public static void giveHealPotion(Player player, int amount) {
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(HEAL_POTION);
        }
    }
}