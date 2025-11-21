package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import me.koutachan.bouncy.special.Reversi;
import org.bukkit.entity.Player;

@Command("reversi")
@Permission("reversi")
public class ReversiCommand {
    @Default
    public static void reversi(Player player) {
        new Reversi(player).open();
    }
}