package me.koutachan.bouncy.ability.turret;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Turret {
    public final static Map<UUID, Turret> BY_ARMOR_STAND = new HashMap<>();

    private final static int DEFAULT_STRENGTH = 2;
    private int strength = DEFAULT_STRENGTH;
    private final Player owner;
    private final GamePlayer gamePlayer;

    private final ArmorStand armorStand;

    private final BukkitTask task;

    public Turret(GamePlayer gamePlayer, Location pos) {
        this.owner = gamePlayer.getPlayer();
        this.gamePlayer = gamePlayer;
        this.armorStand = owner.getWorld().spawn(pos, ArmorStand.class);
        this.armorStand.getEquipment().setHelmet(new ItemStack(Material.DISPENSER));
        this.armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        this.armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        this.armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        this.armorStand.setInvulnerable(true);
        this.armorStand.setSmall(true);
        this.task = Bukkit.getScheduler().runTaskTimer(Bouncy.INSTANCE, this::tryShoot, 5, 20);
        BY_ARMOR_STAND.put(this.armorStand.getUniqueId(), this);
    }

    public void tryShoot() {
        if (armorStand.isDead()) {
            remove();
            return;
        }
        for (Entity entity : owner.getWorld().getNearbyEntities(armorStand.getLocation(), 10, 10, 10,
                e -> e instanceof Player player
                        && !DamageUtils.isSameTeam(owner, e)
                        && player.getGameMode() == GameMode.ADVENTURE)) {
            var startPos = armorStand.getEyeLocation();
            var endPos = EntityUtils.getBestLocation(entity);
            var direction = endPos.subtract(startPos).toVector().normalize();
            if (direction.isNormalized()) {
                armorStand.teleport(armorStand.getLocation().setDirection(direction));
                armorStand.getWorld().spawnArrow(armorStand.getEyeLocation(), direction, 1.5f, 0)
                        .setShooter(owner);
                break;
            }
        }
    }

    public boolean damageTaken() {
        strength -= 1;
        if (0 >= strength) {
            remove();
            return true;
        }
        return false;
    }

    public Player getOwner() {
        return owner;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void remove() {
        armorStand.remove();
        task.cancel();
        gamePlayer.getTurrets().remove(this);
        BY_ARMOR_STAND.remove(armorStand.getUniqueId());
    }
}