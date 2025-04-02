package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LightBeamAbility extends Ability implements AbilityDrop {
    public final static int ID = 31;

    public LightBeamAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            new LightBeam(gamePlayer.getPlayer(), gamePlayer.getEyeLocation()).start();
        }
    }

    public static class LightBeam extends BukkitRunnable {
        public final static int MAX_TICK = 70;
        public final static double ATTACK_RADIUS = 2.5;

        private int tick;
        private final Player attacker;
        private final Vector initialVector;
        private final Location currentPos;
        private final List<Player> attackedPlayers = new ArrayList<>();

        public LightBeam(Player attacker, Location location) {
            this.attacker = attacker;
            this.currentPos = location;
            this.initialVector = location.getDirection().multiply(2);
        }

        @Override
        public void run() {
            if (++tick > MAX_TICK) {
                cancel();
            } else {
                currentPos.add(initialVector);
                for (Player player : currentPos.getWorld().getPlayers()) {
                    if (!attackedPlayers.contains(player) && currentPos.distanceSquared(player.getEyeLocation()) <= ATTACK_RADIUS * ATTACK_RADIUS) {
                        if (!DamageUtils.isSameTeam(attacker, player)) {
                            player.damage(2, attacker);
                        }
                        attackedPlayers.add(player);
                    }
                }
                currentPos.getWorld().playSound(currentPos, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 1, 2);
                currentPos.getWorld().spawnParticle(Particle.END_ROD, currentPos, 6, 0.3, 0.3, 0.3, 0.15);
            }
        }

        public void start() {
            runTaskTimer(Bouncy.INSTANCE, 0, 1);
        }
    }

    @Override
    public String getName() {
        return "光線";
    }

    @Override
    public String getActionBar() {
        return "能力:光線 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
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