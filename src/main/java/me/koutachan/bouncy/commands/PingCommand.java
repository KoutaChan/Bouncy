package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import me.koutachan.bouncy.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Command("fast-ping")
@Alias("ping")
public class PingCommand {
    @Default
    public static void ping(Player player) {
        long start = System.currentTimeMillis();
        GameManager.getGamePlayer(player).addPingTask(() -> {
            player.sendMessage(ChatColor.GREEN + "Pong! " + (System.currentTimeMillis() - start) + "ms");
        });
    }

    @Default
    public static void ping(Player player, @AEntitySelectorArgument.OnePlayer Player target) {
        long start = System.currentTimeMillis();
        GameManager.getGamePlayer(target).addPingTask(() -> {
            player.sendMessage(ChatColor.GREEN + target.getName() + "'s Ping: " + (System.currentTimeMillis() - start) + "ms");
        });
    }
}
