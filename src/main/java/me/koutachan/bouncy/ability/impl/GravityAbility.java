package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GravityAbility extends Ability implements AbilityDrop {
    public static PotionEffect GRAVITY_EFFECT = new PotionEffect(PotionEffectType.LEVITATION, 30, 21, true, false);
    public final static int ID = 2;

    public GravityAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            gamePlayer.addPotionEffect(GRAVITY_EFFECT);
            gamePlayer.playSoundPublic(gamePlayer.getLocation().add(0, 5, 0), Sound.ENTITY_BREEZE_IDLE_GROUND, 1, 0.7f);
        }
    }

    @Override
    public String getName() {
        return "浮遊";
    }

    @Override
    public String getActionBar() {
        return "能力:浮遊 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 200;
    }

    @Override
    public int getId() {
        return ID;
    }
}
