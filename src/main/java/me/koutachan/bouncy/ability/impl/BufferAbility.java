package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BufferAbility extends Ability implements AbilityDrop {
    public final static PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.RESISTANCE, 1, 0, true, false);

    public final static int ID = 41;

    public BufferAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
        gamePlayer.addPotionEffect(RESISTANCE);
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            gamePlayer.getPlayer().getAttribute(Attribute.MAX_ABSORPTION).setBaseValue(2);
            gamePlayer.getPlayer().setAbsorptionAmount(2);
            gamePlayer.playSoundPublic(gamePlayer.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, 1, 2);
            gamePlayer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, gamePlayer.getLocation().add(0, 1.7, 0), 10, 0, 0, 0, 0.1);
            gamePlayer.setAbilityCt(0);
        }
    }

    @Override
    public String getName() {
        return "緩衝";
    }

    @Override
    public String getActionBar() {
        return "能力:緩衝 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 800;
    }

    @Override
    public int getId() {
        return ID;
    }
}
