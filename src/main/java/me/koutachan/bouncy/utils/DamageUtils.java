package me.koutachan.bouncy.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class DamageUtils {
    public static String PUBLIC_TEAM_NAME = "player";

    public static void damage(LivingEntity attacker, Entity victim, double damage) {
        if (attacker == victim || !(victim instanceof LivingEntity victimLiving)) {
            return;
        }
        Vector lastVelocity = victim.getVelocity();
        if (attacker instanceof Player attackingPlayer && victim instanceof Player victimPlayer) {
            damage(attackingPlayer, victimPlayer, damage);
        } else {
            victimLiving.damage(damage, attacker);
        }
        victimLiving.setVelocity(lastVelocity);
    }

    private static void damage(Player attacker, Player victim, double damage) {
        if (!isSameTeam(attacker, victim)) {
            victim.damage(damage, attacker);
        }
    }

    public static boolean isSameTeam(Entity attacker, Entity victim) {
        if (attacker == victim)
            return true;
        String team = getTeam(attacker);
        if (team == null || isPublicTeam(team)) {
            return false;
        }
        return team.equals(getTeam(victim));
    }

    public static boolean isPublicTeam(String name) {
        return name.equals(PUBLIC_TEAM_NAME);
    }

    public static String getTeam(Entity player) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : sb.getTeams()) {
            if (team.hasEntry(player.getName())) {
                return team.getName();
            }
        }
        return null;
    }
}
