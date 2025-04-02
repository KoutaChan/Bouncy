package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class LeapAbility extends Ability implements AbilityDrop {
    private int charge;

    private final static int MAX_CHARGE = 10;
    private final static char CHARGE_ICON = '■';

    public final static int ID = 12;

    public LeapAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (gamePlayer.useAbility(getCt())) {
            if (++charge > MAX_CHARGE) {
                charge = MAX_CHARGE;
            }
        }
    }

    @Override
    public void onDrop() {
        if (charge > 0) {
            gamePlayer.getPlayer().setVelocity(gamePlayer.getPlayer().getVelocity().add(gamePlayer.getLocation().getDirection().multiply(charge * 0.18D)));
            gamePlayer.playSoundPublic(gamePlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 2);
            charge = 0;
        }
    }

    @Override
    public String getName() {
        return "跳躍";
    }

    @Override
    public String getActionBar() {
        String chargeStyle = ChatColor.GREEN + String.valueOf(CHARGE_ICON).repeat(Math.max(0, charge)) +
                ChatColor.GRAY + String.valueOf(CHARGE_ICON).repeat(Math.max(0, MAX_CHARGE - charge));
        return "能力:跳躍 " + "[" + chargeStyle + ChatColor.WHITE + "]";
    }

    @Override
    public int getCt() {
        return 20;
    }

    @Override
    public int getId() {
        return ID;
    }
}