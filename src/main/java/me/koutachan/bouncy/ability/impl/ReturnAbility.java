package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ReturnAbility extends Ability implements AbilityDrop {
    private final static PotionEffect RESISTANCE_EFFECT = new PotionEffect(PotionEffectType.RESISTANCE, 20 * 10, 1, true, false);
    private final static int MAX_RETURN_TIME = 20 * 10;
    private final static Particle.DustTransition RETURN_DUST = new Particle.DustTransition(Color.fromRGB(0, 255, 0), Color.fromRGB(0, 255, 0), 1F);

    private Location returnPos;
    private int tick;

    public final static int ID = 28;

    public ReturnAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onDrop() {
        if (returnPos != null) {
            gamePlayer.getPlayer().teleport(returnPos);
        } else if (gamePlayer.useAbility(getCt())) {
            returnPos = gamePlayer.getLocation();
            tick = 0;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (++tick > MAX_RETURN_TIME) {
                        returnPos = null;
                        cancel();
                    } else {
                        gamePlayer.getPlayer().spawnParticle(Particle.DUST_COLOR_TRANSITION, returnPos, 1, RETURN_DUST);
                    }
                }
            }.runTaskTimer(Bouncy.INSTANCE, 1, 1);
            gamePlayer.addPotionEffect(RESISTANCE_EFFECT);
        }
    }

    @Override
    public String getName() {
        return "帰還";
    }

    @Override
    public String getActionBar() {
        if (returnPos != null) {
            return "能力:帰還 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒（" + FormatUtils.formatTick(MAX_RETURN_TIME - tick) + "秒間テレポート可能）";
        } else {
            return "能力:帰還 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
        }
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
