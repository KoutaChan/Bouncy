package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RegenerationAbility extends Ability implements AbilityDrop {
    public static PotionEffect SLOW_EFFECT = new PotionEffect(PotionEffectType.SLOWNESS, 20, 1, true, false);
    public static PotionEffect REGENERATION_EFFECT = new PotionEffect(PotionEffectType.REGENERATION, 20, 2, true, false);


    public static int ID = 16;

    public RegenerationAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            gamePlayer.playSoundPublic(gamePlayer.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1, 2);
            gamePlayer.addPotionEffect(SLOW_EFFECT);
            gamePlayer.addPotionEffect(REGENERATION_EFFECT);
            gamePlayer.getWorld().spawnParticle(Particle.HEART, gamePlayer.getLocation().add(0, 2, 0), 5, 0.2, 0, 0.2, 0);
        }
    }

    @Override
    public String getName() {
        return "再生";
    }

    @Override
    public String getActionBar() {
        return "能力:再生 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 400;
    }

    @Override
    public int getId() {
        return ID;
    }
}
