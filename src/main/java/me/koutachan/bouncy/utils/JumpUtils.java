package me.koutachan.bouncy.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JumpUtils {
    public static boolean isJump(double deltaY, double jumpPower) {
        return Math.abs(deltaY - jumpPower) < 0.05;
    }

    public static double getJumpPower(Player player) {
        double base = 0.42;
        PotionEffect jumpBoost = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jumpBoost != null) {
            base += (jumpBoost.getAmplifier() + 1) * 0.1;
        }
        return base;
    }
}
