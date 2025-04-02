package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AChatColorArgument;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.AMultiLiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.skytasul.glowingentities.GlowTeam;
import me.koutachan.bouncy.Bouncy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Collection;

@Command("glow")
@Permission("glow")
public class GlowCommand {
    @Default
    public static void glow(NativeProxyCommandSender sender, @AEntitySelectorArgument.ManyEntities Collection<Entity> entities, @AEntitySelectorArgument.OnePlayer Player player, @AChatColorArgument ChatColor color) {
        try {
            for (Entity entity : entities) {
                Bouncy.GLOW_API.setGlowing(entity, player, new GlowTeam(color));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Default
    public static void glow(NativeProxyCommandSender sender, @AEntitySelectorArgument.ManyEntities Collection<Entity> entities, @AEntitySelectorArgument.OnePlayer Player player, @AChatColorArgument ChatColor color, @AMultiLiteralArgument(value= {"ALWAYS", "NEVER", "FOR_OTHER_TEAMS", "FOR_OWN_TEAM"}) String nameTagVisibility, @AMultiLiteralArgument(value= {"ALWAYS", "NEVER", "FOR_OTHER_TEAMS", "FOR_OWN_TEAM"}) String collisionRule) {
        try {
            GlowTeam glowTeam = new GlowTeam(color, Team.OptionStatus.valueOf(nameTagVisibility), Team.OptionStatus.valueOf(collisionRule));
            for (Entity entity : entities) {
                Bouncy.GLOW_API.setGlowing(entity, player, glowTeam);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Subcommand("self")
    public static void glow(NativeProxyCommandSender sender, @AEntitySelectorArgument.ManyEntities Collection<Entity> entities, @AChatColorArgument ChatColor color) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        try {
            for (Entity entity : entities) {
                Bouncy.GLOW_API.setGlowing(entity, player, new GlowTeam(color));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Subcommand("reset")
    public static void resetGlow(NativeProxyCommandSender sender) {
        try { // 3 loops
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Bouncy.GLOW_API.unsetGlowing(entity, player);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Subcommand("unset")
    public static void unsetGlow(NativeProxyCommandSender sender, @AEntitySelectorArgument.ManyEntities Collection<Entity> entities, @AEntitySelectorArgument.OnePlayer Player player) {
        try {
            for (Entity entity : entities) {
                Bouncy.GLOW_API.unsetGlowing(entity, player);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Subcommand("unset")
    public static void unsetGlow(NativeProxyCommandSender sender, @AEntitySelectorArgument.OnePlayer Player player) {
        try { // two loops
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    Bouncy.GLOW_API.unsetGlowing(entity, player);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Subcommand(value = {"unset", "self"})
    public static void unsetGlow(NativeProxyCommandSender sender) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        try { // two loops
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    Bouncy.GLOW_API.unsetGlowing(entity, player);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Subcommand(value = {"unset", "self"})
    public static void unsetGlow(NativeProxyCommandSender sender, @AEntitySelectorArgument.ManyEntities Collection<Entity> entities) {
        if (!(sender.getCallee() instanceof Player player)) {
            return;
        }
        try {
            for (Entity entity : entities) {
                Bouncy.GLOW_API.unsetGlowing(entity, player);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}