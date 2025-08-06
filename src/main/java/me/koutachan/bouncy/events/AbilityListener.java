package me.koutachan.bouncy.events;

import me.koutachan.bouncy.ability.*;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AbilityListener implements Listener {
    @EventHandler
    public void onBowDropEvent(PlayerDropItemEvent event) {
        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getPlayer());
        if (event.getItemDrop().getItemStack().getType() == Material.BOW) {
            var ability = gamePlayer.getAbilityHandler().getAbility();
            if (ability instanceof AbilityDrop abilityDrop) {
                abilityDrop.onDrop();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (event.getEntity().getLastDamageCause() == null || !(event.getEntity().getLastDamageCause().getDamageSource().getCausingEntity() instanceof Player player)) {
            return;
        }
        GamePlayer gamePlayer = GameManager.getGamePlayer(player);
        var ability = gamePlayer.getAbilityHandler().getAbility();
        if (ability instanceof AbilityKill abilityKill) {
            abilityKill.onKill(event.getEntity());
        }
    }

    @EventHandler
    public void onEntityShootEvent(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof Player victim) || !(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        GamePlayer gamePlayer = GameManager.getGamePlayer(player);
        var ability = gamePlayer.getAbilityHandler().getAbility();
        if (ability instanceof AbilityAttack abilityAttack) {
            abilityAttack.onAttack(victim);
        }
    }

    @EventHandler
    public void onPotionDrinkEvent(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.POTION) {
            GamePlayer gamePlayer = GameManager.getGamePlayer(event.getPlayer());
            var ability = gamePlayer.getAbilityHandler().getAbility();
            if (ability instanceof AbilityPotion abilityPotion) {
                abilityPotion.onPotion(event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        GamePlayer gamePlayer = GameManager.getGamePlayer(player);
        var ability = gamePlayer.getAbilityHandler().getAbility();
        if (ability instanceof AbilityShoot abilityShoot) {
            abilityShoot.onShoot(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        GamePlayer gamePlayer = GameManager.getGamePlayer(player);
        var ability = gamePlayer.getAbilityHandler().getAbility();
        if (ability instanceof AbilityDamage abilityDamage) {
            abilityDamage.onDamage(event);
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getPlayer());
        if (gamePlayer != null) {
            gamePlayer.resumeTasks(event.getPlayer());
        } else {
            GameManager.createGamePlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getPlayer());
        if (gamePlayer != null) {
            gamePlayer.pauseTasks();
        }
    }
}