package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityAttack;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.EntityUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class CreateArrowAbility extends Ability implements AbilityDrop, AbilityAttack {
    private int charge;

    public final static int ID = 39;

    public CreateArrowAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (gamePlayer.getPlayer().getLevel() > 0) {
            gamePlayer.getPlayer().setExp(0);
        }
    }

    @Override
    public void onDrop() {
        if (0 >= gamePlayer.getPlayer().getNoDamageTicks() && !gamePlayer.hasPotionEffect(PotionEffectType.REGENERATION)) {
            charge++;
            gamePlayer.getWorld().spawnParticle(Particle.EFFECT, gamePlayer.getLocation().add(0, 1, 0), 12, 0.3, 0.9, 0.3, 0);
            gamePlayer.playSoundPublic(gamePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
            gamePlayer.getPlayer().damage(1);
            gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() + 1);
        }
    }

    @Override
    public void onAttack(Player victim) {
        if (charge > 0) {
            EntityUtils.heal(gamePlayer.getPlayer(), 2);
            charge--;
        }
    }

    @Override
    public String getName() {
        return "創造";
    }

    @Override
    public String getActionBar() {
        return "能力:創造 （チャージ: " + charge + "）";
    }

    @Override
    public int getCt() {
        return 0;
    }

    @Override
    public int getId() {
        return ID;
    }
}