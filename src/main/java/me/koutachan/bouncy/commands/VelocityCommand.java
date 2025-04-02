package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.ADoubleArgument;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

@Command("velocity")
@Permission("velocity")
public class VelocityCommand {
    private static final String INVALID_SENDER_MESSAGE = ChatColor.RED + "エンティティーから実行する必要があります";

    @Default
    public static void velocity(NativeProxyCommandSender sender, @ADoubleArgument double velocityX, @ADoubleArgument double velocityY, @ADoubleArgument double velocityZ) {
        if (!(sender.getCallee() instanceof Entity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        entity.setVelocity(new Vector(velocityX, velocityY, velocityZ));
    }

    @Default
    public static void velocity(CommandSender sender, @AEntitySelectorArgument.OneEntity Entity entity, @ADoubleArgument double velocityX, @ADoubleArgument double velocityY, @ADoubleArgument double velocityZ) {
        entity.setVelocity(new Vector(velocityX, velocityY, velocityZ));
    }

    @Subcommand(value = "direction")
    public static void velocityDirection(NativeProxyCommandSender sender, @ADoubleArgument double multiply) {
        if (!(sender.getCallee() instanceof Entity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        entity.setVelocity(entity.getLocation().getDirection().multiply(multiply));
    }

    @Subcommand(value = "direction")
    public static void velocityDirection(CommandSender sender, @AEntitySelectorArgument.OneEntity Entity entity, @ADoubleArgument double multiply) {
        entity.setVelocity(entity.getLocation().getDirection().multiply(multiply));
    }
}