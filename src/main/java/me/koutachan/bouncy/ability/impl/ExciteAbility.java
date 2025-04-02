package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityAttack;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ExciteAbility extends Ability implements AbilityAttack {
    private final static PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 20 * 3, 3);
    private final static double ARROW_CHANCE = 60;

    public final static int ID = 20;

    public ExciteAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onAttack(Player victim) {
        gamePlayer.addPotionEffect(SPEED);
        if (ARROW_CHANCE >= Bouncy.SECURE_RANDOM.nextDouble(0, 100)) {
            gamePlayer.getPlayer().setLevel(gamePlayer.getPlayer().getLevel() + 1);
            gamePlayer.playSoundPublic(gamePlayer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1, 1);
        }
        gamePlayer.getWorld().spawnParticle(Particle.WAX_OFF, gamePlayer.getLocation().add(0, 1, 0), 5, 0.3, 0.9, 0.3, 0);
    }

    @Override
    public String getName() {
        return "興奮";
    }

    @Override
    public String getActionBar() {
        return "能力:興奮 自動発動";
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