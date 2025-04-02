package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class FlyAbility extends Ability implements AbilityDrop {
    private final static PotionEffect SLOW_FALLING_EFFECT = new PotionEffect(PotionEffectType.SLOW_FALLING, 2, 0);
    private int tick;

    public final static int FLY_TICK = 100;
    public final static int ID = 45;

    public FlyAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
        if (tick-- > 0) {
            gamePlayer.addPotionEffect(SLOW_FALLING_EFFECT);
            Vector velocity;
            if (gamePlayer.getPlayer().isSneaking()) {
                velocity = new Vector();
            } else {
                velocity = gamePlayer.getEyeLocation().getDirection().multiply(0.7);
            }
            gamePlayer.getPlayer().setVelocity(velocity);
            gamePlayer.getWorld().spawnParticle(Particle.INSTANT_EFFECT, gamePlayer.getLocation().add(0, 1, 0), 4, 0.5, 1, 0.5, 1);
        }
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            tick = FLY_TICK;
            gamePlayer.getPlayer().playSound(gamePlayer.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.3F, 2F);
        }
    }

    @Override
    public String getName() {
        return "飛行";
    }

    @Override
    public String getActionBar() {
        if (tick > 0) {
            return "能力:飛行 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（残り飛行時間: " + FormatUtils.formatTick(tick) + "秒）";
        } else {
            return "能力:飛行 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
        }
    }

    @Override
    public int getCt() {
        return 500;
    }

    @Override
    public int getId() {
        return ID;
    }
}