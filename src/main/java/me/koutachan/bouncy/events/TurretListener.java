package me.koutachan.bouncy.events;

import me.koutachan.bouncy.ability.turret.Turret;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class TurretListener implements Listener {
    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof ArmorStand armorStand)) {
            return;
        }

        Turret turret = Turret.BY_ARMOR_STAND.get(armorStand.getUniqueId());
        if (turret != null) {
            handleTurretInteraction(event, turret);
        }
    }

    private void handleTurretInteraction(ProjectileHitEvent event, Turret turret) {
        boolean isSameShooter = turret.getOwner() == event.getEntity().getShooter();
        if (!isSameShooter) {
            turret.damageTaken();
        }
        event.getEntity().remove();
    }
}