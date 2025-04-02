package me.koutachan.bouncy.commands;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AEntityTypeArgument;
import dev.jorel.commandapi.annotations.arguments.ALocationArgument;
import dev.jorel.commandapi.annotations.arguments.ANBTCompoundArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import me.koutachan.bouncy.utils.EntityUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Command("rsummon")
@Permission("rsummon")
public class CustomSummonCommand {
    @Default
    public static void summon(NativeProxyCommandSender sender, @AEntityTypeArgument EntityType type) {
        summon(sender, type, new NBTContainer());
    }

    @Default
    public static void summonWithNbt(NativeProxyCommandSender sender, @AEntityTypeArgument EntityType type, @ANBTCompoundArgument NBTContainer container) {
        summon(sender, type, container);
    }

    @Default
    public static void summonWithNbt(NativeProxyCommandSender sender, @ALocationArgument Location location, @AEntityTypeArgument EntityType type, @ANBTCompoundArgument NBTContainer container) {
        summon(sender, location, type, container);
    }

    private static void summon(NativeProxyCommandSender sender, EntityType type, NBTContainer container) {
        summon(sender, sender.getLocation(), type, container);
    }

    private static void summon(NativeProxyCommandSender sender, Location location, EntityType type, NBTContainer container) {
        if (sender.getCallee() instanceof Player player) {
            applyPlayerSpecificNbt(container, player);
        }
        Entity spawnedEntity = spawnEntityWithType(sender, location, type);
        applyNbtToEntity(spawnedEntity, container);
    }

    private static void applyPlayerSpecificNbt(NBTContainer container, Player player) {
        EntityUtils.writePluginPersistent(container, nbt -> nbt.setUUID("attackBy", player.getUniqueId()));
    }

    private static Entity spawnEntityWithType(NativeProxyCommandSender sender, Location location, EntityType type) {
        return sender.getWorld().spawnEntity(location, type);
    }

    private static void applyNbtToEntity(Entity entity, NBTContainer container) {
        EntityUtils.writeNbt(entity, container);
    }
}