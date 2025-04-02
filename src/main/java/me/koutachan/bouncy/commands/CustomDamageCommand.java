package me.koutachan.bouncy.commands;

import de.tr7zw.changeme.nbtapi.NBT;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.arguments.ADoubleArgument;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import me.koutachan.bouncy.utils.DamageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

@Command("rdamage")
@Permission("rdamage")
public class CustomDamageCommand {
    private static final String INVALID_SENDER_MESSAGE = ChatColor.RED + "生きているエンティティーから実行する必要があります";
    private static final String INVALID_ATTACK_ENTITY_MESSAGE = ChatColor.RED + "不明な攻撃者です";


    @Default
    public static void damage(NativeProxyCommandSender sender, @AEntitySelectorArgument.ManyEntities Collection<Entity> entities, @ADoubleArgument double damage) {
        if (!(sender.getCallee() instanceof LivingEntity attackingEntity)) {
            sender.sendMessage(INVALID_SENDER_MESSAGE);
            return;
        }

        UUID uuid = NBT.getPersistentData(attackingEntity, nbt -> nbt.getUUID("attackBy"));
        if (uuid == null) {
            sender.sendMessage(INVALID_ATTACK_ENTITY_MESSAGE);
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        for (Entity targetEntity : entities) {
            DamageUtils.damage(player, targetEntity, damage);
        }
    }
}