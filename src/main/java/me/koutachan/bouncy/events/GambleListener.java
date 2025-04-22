package me.koutachan.bouncy.events;

import de.tr7zw.changeme.nbtapi.NBT;
import me.koutachan.bouncy.ability.impl.gamble.GambleBuff;
import me.koutachan.bouncy.ability.impl.gamble.GambleDeBuff;
import me.koutachan.bouncy.ability.impl.gamble.Gambler;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GambleListener implements Listener {
    @EventHandler
    public void onJump(PlayerMoveEvent event) {
        if (event.getTo() == null)
            return;
        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getPlayer());
        if (gamePlayer == null || !gamePlayer.hasDeBuff(GambleDeBuff.NO_JUMP))
            return;
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        if (0.05 > Math.abs(deltaY - getJumpPower(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() == null
                || !event.getItem().hasItemMeta()
                || !event.getItem().getItemMeta().getPersistentDataContainer().getOrDefault(Gambler.GAMBLE_NAMESPACED_KEY, PersistentDataType.BOOLEAN, false)) {
            return;
        }
        GameManager.getGamePlayerOrCreate(event.getPlayer())
                .tryUseGamble();
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        GamePlayer gamePlayer = GameManager.getGamePlayer(player);
        if (gamePlayer == null)
            return;
        if (gamePlayer.hasBuff(GambleBuff.ALL_ARROW_SPREAD)) {
            NBT.modifyPersistentData(event.getEntity(), persistent -> {
                persistent.setBoolean("Spread", true);
            });
        }
        if (gamePlayer.hasBuff(GambleBuff.ALL_ARROW_BOUNCE)) {
            NBT.modifyPersistentData(event.getEntity(), persistent -> {
                persistent.setInteger("BouncyCount", 1);
            });
            event.getEntity().addScoreboardTag("bouncy");
        }
    }

    @EventHandler
    public void onIncreaseDamage(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof Player victim) || !(event.getEntity().getShooter() instanceof Player shooter)) {
            return;
        }
        GamePlayer gamePlayer = GameManager.getGamePlayer(shooter);
        if (gamePlayer == null || !gamePlayer.hasBuff(GambleBuff.INCREASE_DAMAGE))
            return;
        victim.setHealth(Math.max(0, victim.getHealth() - 0.5));
    }

    @EventHandler
    public void onExtraDamage(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof Player player)) {
            return;
        }
        GamePlayer gamePlayer = GameManager.getGamePlayer(player);
        if (gamePlayer == null || !gamePlayer.hasDeBuff(GambleDeBuff.EXTRA_DAMAGE))
            return;
        player.setHealth(Math.max(0, player.getHealth() - 0.5));
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getEntity());
        if (gamePlayer != null) {
            gamePlayer.clearGamble();
        }
    }

    private static double getJumpPower(Player player) {
        double base = 0.42;
        PotionEffect jumpBoost = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jumpBoost != null) {
            base += (jumpBoost.getAmplifier() + 1) * 0.1;
        }
        return base;
    }
}
