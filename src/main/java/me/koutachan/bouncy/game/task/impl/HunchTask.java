package me.koutachan.bouncy.game.task.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.game.task.GameTask;
import me.koutachan.bouncy.utils.EntityUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HunchTask extends GameTask {
    public static Particle.DustTransition HUNCH_DUST = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 0, 0), 1.5F);

    public HunchTask(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void run() {
        Player target = EntityUtils.getNearestEnemy(gamePlayer.getPlayer());
        if (target != null) {
            Location playerPos = gamePlayer.getLocation().add(0, 0.3, 0);
            Vector direction = target.getEyeLocation().subtract(playerPos).toVector().normalize();

            for (int i = 0; i < 6; i++) {
                gamePlayer.getPlayer().spawnParticle(Particle.DUST_COLOR_TRANSITION, playerPos.getX(), playerPos.getY(), playerPos.getZ(), 1, 0.05, 0.05, 0.05, 0, HUNCH_DUST);
                playerPos.add(direction);
            }
        }
    }

    @Override
    public void start() {
        runTaskTimer(Bouncy.INSTANCE, 0, 1);
    }
}
