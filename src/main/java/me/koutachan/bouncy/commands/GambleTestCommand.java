package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import me.koutachan.bouncy.ability.impl.assassin.AssassinItems;
import me.koutachan.bouncy.ability.impl.gamble.GambleBuff;
import me.koutachan.bouncy.ability.impl.gamble.GambleDeBuff;
import me.koutachan.bouncy.ability.impl.gamble.Gambler;
import me.koutachan.bouncy.ability.impl.gamble.gui.GambleGui;
import me.koutachan.bouncy.game.GameManager;
import org.bukkit.entity.Player;

@Command("gamble")
@Permission("gamble")
public class GambleTestCommand {
    @Default
    public static void onCommand(Player player) {
        new GambleGui().open(player);
    }

    @Subcommand("give")
    public static void onGive(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        player.getInventory().addItem(Gambler.GAMBLE_ITEM);
    }

    @Subcommand("hunch")
    public static void onHunch(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        GameManager.getGamePlayer(player).addGambleBuff(GambleBuff.HUNCH_ENEMY);
    }

    @Subcommand("assassin")
    public static void onAssassin(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        player.getInventory().addItem(AssassinItems.ASSASSIN_ITEMS);
    }

    @Subcommand("random-ability")
    public static void onRandomAbility(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        GameManager.getGamePlayer(player).addGambleBuff(GambleDeBuff.RANDOM_ABILITY);
    }

    @Subcommand("turret")
    public static void onTurret(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        GameManager.getGamePlayer(player).addTurret(player.getLocation());
    }

    @Subcommand("random-rotation")
    public static void onRandomRotation(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        GameManager.getGamePlayer(player).addGambleBuff(GambleDeBuff.RANDOM_ROTATION);
    }
}