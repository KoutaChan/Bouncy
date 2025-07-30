package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import me.koutachan.bouncy.game.GameManager;
import org.bukkit.command.CommandSender;

@Command("resetData")
@Permission("resetData")
public class ResetDataCommand {
    @Default
    public static void reset(CommandSender sender) {
        GameManager.resetAll();
    }
}