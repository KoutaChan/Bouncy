package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.boarder.WorldBorder;
import me.koutachan.bouncy.game.boarder.WorldBorderHandler;
import org.bukkit.ChatColor;

@Command("border")
@Permission("border")
public class BorderCommand {
    @Subcommand("cancel")
    public static void onCancel(NativeProxyCommandSender sender) {
        WorldBorderHandler handler = Bouncy.INSTANCE.worldBorderHandler;
        if (handler.getBorder() == null) {
            sender.sendMessage(ChatColor.RED + "現在進行中のボーダーはありません。");
            return;
        }

        handler.cancelBorder();
        sender.sendMessage(ChatColor.GREEN + "ボーダーを強制終了しました。");
    }

    @Subcommand("test")
    public static void onTest(NativeProxyCommandSender sender) {
        WorldBorderHandler handler = Bouncy.INSTANCE.worldBorderHandler;
        if (handler.getBorder() == null) {
            sender.sendMessage(ChatColor.RED + "現在進行中のボーダーはありません。");
            return;
        }

        handler.getBorder().setCount(0);
        sender.sendMessage(ChatColor.GREEN + "実行しました。");
    }

    @Subcommand("start")
    public static void onStart(NativeProxyCommandSender sender, @AStringArgument String settingName) {
        WorldBorderHandler handler = Bouncy.INSTANCE.worldBorderHandler;
        if (handler.getBorder() != null) {
            sender.sendMessage(ChatColor.RED + "既にボーダーが進行中です！先に /border cancel を実行してください。");
            return;
        }

        WorldBorder.Setting settings = handler.getSetting(settingName);
        if (settings == null) {
            sender.sendMessage(ChatColor.RED + "設定ファイルが見つかりませんでした: " + settingName);
            sender.sendMessage(ChatColor.GRAY + "bordersフォルダ内のymlにある 'name' を指定してください。");
            return;
        }

        handler.start(settings);
        sender.sendMessage(ChatColor.GREEN + "開始しました (設定: " + settingName + ")");
    }
}