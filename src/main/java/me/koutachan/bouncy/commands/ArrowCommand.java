package me.koutachan.bouncy.commands;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.ANBTCompoundArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import me.koutachan.bouncy.utils.EntityUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

@Command("arrow")
@Permission("arrow")
public class ArrowCommand {
    private static final String INVALID_SENDER_MESSAGE = ChatColor.RED + "エンティティーから実行する必要があります";

    @Default
    public static void arrow(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Entity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        spawnArrowForEntity(entity, sender.getLocation(), determineProjectileShooter(entity));
    }

    @Default
    public static void arrow(NativeProxyCommandSender sender, @ANBTCompoundArgument NBTContainer container) {
        if (!(sender.getCallee() instanceof Entity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        EntityUtils.writeNbt(spawnArrowForEntity(entity, sender.getLocation(), determineProjectileShooter(entity)), container);
    }

    private static ProjectileSource determineProjectileShooter(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        } else if (entity instanceof Projectile projectile) {
            return projectile.getShooter();
        } else {
            return null;
        }
    }

    private static Arrow spawnArrowForEntity(Entity entity, Location location, ProjectileSource projectileShooter) {
        Arrow arrow = entity.getWorld().spawnArrow(modifyEyeHeightPos(entity, location), entity.getLocation().getDirection(), 1, 1);
        arrow.setShooter(projectileShooter);
        return arrow;
    }

    private static Location modifyEyeHeightPos(Entity entity, Location location) {
        if (entity instanceof LivingEntity livingEntity) {
            location.add(0, livingEntity.getEyeHeight(), 0);
        }
        return location;
    }
}