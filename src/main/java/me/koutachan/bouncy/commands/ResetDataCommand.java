package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import me.koutachan.bouncy.game.GameManager;
import org.bukkit.command.CommandSender;

@Command("resetData")
public class ResetDataCommand {
    @Default
    public static void reset(CommandSender sender) {
        GameManager.resetAll();
    }
}