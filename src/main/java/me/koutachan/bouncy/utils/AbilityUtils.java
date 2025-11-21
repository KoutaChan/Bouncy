package me.koutachan.bouncy.utils;

import me.koutachan.bouncy.ability.impl.GreedAbility;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.util.List;


public class AbilityUtils {
    public static ItemStack HEAL_POTION = new ItemStack(Material.POTION) {{
        var meta = (PotionMeta) getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "回復のポーション");
        meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 2, 29, true, false), true);
        meta.setColor(Color.fromARGB(16732788));
        meta.setLore(List.of(ChatColor.GREEN + "体力回復:♥"));
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

    public static ItemStack getBestHealPotion(GamePlayer gamePlayer) {
        ItemStack res = HEAL_POTION.clone();
        if (gamePlayer.getAbilityId() != GreedAbility.ID) {
            return res;
        }
        ItemMeta meta = res.getItemMeta();
        meta.setMaxStackSize(4);
        res.setItemMeta(meta);
        return res;
    }

    public static void giveHealPotion(Player player, int amount) {
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(HEAL_POTION);
        }
    }
}