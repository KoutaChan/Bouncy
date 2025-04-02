package me.koutachan.bouncy.commands;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.ANBTCompoundArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import me.koutachan.bouncy.utils.EntityUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Command("persistent")
@Permission("persistent")
public class PersistentCommand {
    private static final String INVALID_SENDER_MESSAGE = ChatColor.RED + "エンティティーから実行する必要があります";
    private static final String PLAYER_ACTION_NOT_ALLOWED_MESSAGE = ChatColor.RED + "安全のため、プレイヤーには適応できません";

    @Default
    public static void persistent(NativeProxyCommandSender sender, @ANBTCompoundArgument NBTContainer container) {
        if (!(sender.getCallee() instanceof Entity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        if (entity instanceof Player) {
            sender.sendMessage(PLAYER_ACTION_NOT_ALLOWED_MESSAGE);
            return;
        }
        NBT.modifyPersistentData(entity, persistentNbt -> {
            persistentNbt.mergeCompound(container);
        });
    }

    @Subcommand(value = {"direct"})
    public static void direct(NativeProxyCommandSender sender, @ANBTCompoundArgument NBTContainer container) {
        if (!(sender.getCallee() instanceof Entity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        if (entity instanceof Player) {
            sender.sendMessage(PLAYER_ACTION_NOT_ALLOWED_MESSAGE);
            return;
        }
        EntityUtils.writeNbt(entity, container);
    }

    @Subcommand(value = {"unsafe"})
    public static void unsafe(NativeProxyCommandSender sender, @ANBTCompoundArgument NBTContainer container) {
        if (!(sender.getCallee() instanceof Entity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        EntityUtils.writeNbt(entity, container);
    }
}