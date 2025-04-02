package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityAttack;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisibilityAbility extends Ability implements AbilityDrop, AbilityAttack {
    public static PotionEffect HIT_INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 3, 1, true, false);
    public static PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 10, 1, true, false);

    public final static int ID = 3;

    public InvisibilityAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onAttack(Player victim) {
        gamePlayer.addPotionEffect(HIT_INVISIBILITY);
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            gamePlayer.addPotionEffect(INVISIBILITY);
            gamePlayer.getWorld().spawnParticle(Particle.SMOKE, gamePlayer.getLocation().add(0, 1, 0), 25, 0.5, 1, 0.5, 0.05);
            gamePlayer.playSoundPublic(gamePlayer.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 0.6f);
        }
    }

    @Override
    public String getName() {
        return "隠密";
    }

    @Override
    public String getActionBar() {
        return "能力:隠密 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
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