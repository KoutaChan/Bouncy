package me.koutachan.bouncy.events;

import de.tr7zw.changeme.nbtapi.NBT;
import me.koutachan.bouncy.ability.impl.gamble.GambleBuff;
import me.koutachan.bouncy.ability.impl.gamble.GambleDeBuff;
import me.koutachan.bouncy.ability.impl.gamble.Gambler;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GambleListener implements Listener {
    @EventHandler
    public void onJumpEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null)
            return;

        GamePlayer gamePlayer = GameManager.getGamePlayer(player);
        if (gamePlayer == null || !gamePlayer.hasDeBuff(GambleDeBuff.NO_JUMP))
            return;

        double deltaY = to.getY() - from.getY();
        double jumpPower = getJumpPower(player);

        if (isJump(deltaY, jumpPower)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().getOrDefault(Gambler.GAMBLE_NAMESPACED_KEY, PersistentDataType.BOOLEAN, false))
            return;

        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getPlayer());
        if (gamePlayer != null) {
            gamePlayer.tryUseGamble();
        }
    }

    @EventHandler
    public void onArrowLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player))
            return;

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
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof Player victim) || !(event.getEntity().getShooter() instanceof Entity shooter))
            return;

        if (DamageUtils.isSameTeam(victim, shooter))
            return;

        if (shooter instanceof Player shooterPlayer) {
            GamePlayer shooterGamePlayer = GameManager.getGamePlayer(shooterPlayer);
            if (shooterGamePlayer != null && shooterGamePlayer.hasBuff(GambleBuff.INCREASE_DAMAGE)) {
                victim.setHealth(Math.max(0, victim.getHealth() - 0.5));
            }
        }

        GamePlayer victimGamePlayer = GameManager.getGamePlayer(victim);
        if (victimGamePlayer != null && victimGamePlayer.hasDeBuff(GambleDeBuff.EXTRA_DAMAGE)) {
            victim.setHealth(Math.max(0, victim.getHealth() - 0.5));
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getEntity());
        if (gamePlayer != null) {
            gamePlayer.clearGamble();
        }
    }

    private static boolean isJump(double deltaY, double jumpPower) {
        return Math.abs(deltaY - jumpPower) < 0.05;
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
