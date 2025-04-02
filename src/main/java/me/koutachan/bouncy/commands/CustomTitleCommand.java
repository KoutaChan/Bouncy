package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Command("rtitle")
@Permission("rtitle")
public class CustomTitleCommand {
    private static final String INVALID_SENDER_MESSAGE = ChatColor.RED + "プレイヤーから実行する必要があります";

    @Default
    public static void title(NativeProxyCommandSender sender, @AGreedyStringArgument String title) {
        if (!(sender.getCallee() instanceof Player player)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), null, 10, 70, 20);
    }

    @Default
    public static void titleWithSelector(NativeProxyCommandSender sender, @AEntitySelectorArgument.OnePlayer Player player, @AGreedyStringArgument String title) {
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), null, 10, 70, 20);
    }
}