package me.koutachan.bouncy.utils;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Consumer;

public class EntityUtils {
    public static ReadWriteNBT getEntityNbt(Entity entity) {
        ReadWriteNBT entityNbt = NBT.createNBTObject();
        NBT.get(entity, entityNbt::mergeCompound);
        return entityNbt;
    }

    public static Location getBestLocation(Entity entity) {
        return entity instanceof LivingEntity livingEntity ? livingEntity.getEyeLocation() : entity.getLocation();
    }

    public static void writePluginPersistent(NBTContainer container, Consumer<ReadWriteNBT> con) {
        con.accept(container.getOrCreateCompound("persistent"));
    }

    public static void heal(LivingEntity livingEntity, double healValue) {
        var max = livingEntity.getAttribute(Attribute.MAX_HEALTH).getValue();
        livingEntity.setHealth(Math.min(max, livingEntity.getHealth() + healValue));
    }

    @Nullable
    public static Player getNearestEnemy(Player player) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(o -> o != player && o.getGameMode() == GameMode.ADVENTURE && !DamageUtils.isSameTeam(o, player))
                .min(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(player.getLocation())))
                .orElse(null);
    }

    public static void writeNbt(Entity entity, NBTContainer container) {
        ReadWriteNBT persistent = container.getCompound("persistent");
        if (persistent != null) {
            NBT.modifyPersistentData(entity, persistentNbt -> {
                persistentNbt.mergeCompound(persistent);
            });
        }
        NBT.modify(entity, nbt -> {
            nbt.mergeCompound(container);
        });
    }
}