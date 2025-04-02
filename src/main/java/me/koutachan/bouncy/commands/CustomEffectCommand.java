package me.koutachan.bouncy.commands;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.annotations.arguments.APotionEffectArgument;
import dev.jorel.commandapi.annotations.arguments.ATimeArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Command("reffect")
@Permission("reffect")
public class CustomEffectCommand {
    private static final String INVALID_SENDER_MESSAGE = ChatColor.RED + "生きているエンティティーから実行する必要があります";

    @Default
    public static void potion(NativeProxyCommandSender sender, @APotionEffectArgument PotionEffectType type, @ATimeArgument int duration, @AIntegerArgument int strength) {
        if (!(sender.getCallee() instanceof LivingEntity entity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        entity.addPotionEffect(new PotionEffect(type, duration, strength, true, false));
    }

    @Default
    public static void potion(CommandSender sender, @AEntitySelectorArgument.OneEntity Entity entity, @APotionEffectArgument PotionEffectType type, @ATimeArgument int duration, @AIntegerArgument int strength) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }
        livingEntity.addPotionEffect(new PotionEffect(type, duration, strength, true, false));
    }
}