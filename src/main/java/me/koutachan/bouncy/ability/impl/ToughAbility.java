package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityAttack;
import me.koutachan.bouncy.ability.AbilityDamage;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class ToughAbility extends Ability implements AbilityAttack, AbilityDamage {
    private final static int HIT_MULTIPLY = 5 * 20;
    private final static int MAX_HEAL_DELAY = 1000;

    private int localCount;

    public final static int ID = 22;

    public ToughAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (gamePlayer.getAbilityCt() >= getCt()) {
            gamePlayer.getPlayer().getAttribute(Attribute.MAX_ABSORPTION).setBaseValue(2);
            gamePlayer.getPlayer().setAbsorptionAmount(2);
            gamePlayer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, gamePlayer.getLocation().add(0, 1.7, 0), 10, 0, 0, 0, 0.1);
            gamePlayer.setAbilityCt(getDelay());
        }
    }

    @Override
    public void onAttack(Player victim) {
        if (!gamePlayer.isArrowHitPresent()) {
            localCount++;
        }
    }

    public int getCount() {
        return gamePlayer.isArrowHitPresent() ? gamePlayer.getArrowHit() : localCount;
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        gamePlayer.setAbilityCt(getDelay());
    }

    public int getDelay() {
        return Math.min(MAX_HEAL_DELAY, getCount() * HIT_MULTIPLY);
    }

    @Override
    public String getName() {
        return "強靭";
    }

    @Override
    public String getActionBar() {
        return "能力:強靭 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒（" + FormatUtils.formatTick(getCt()) + "秒で自動発動）";
    }

    @Override
    public int getCt() {
        return 1200;
    }

    @Override
    public int getId() {
        return ID;
    }
}
