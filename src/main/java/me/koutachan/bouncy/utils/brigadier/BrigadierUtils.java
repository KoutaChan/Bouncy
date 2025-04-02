package me.koutachan.bouncy.utils.brigadier;

import me.koutachan.bouncy.Bouncy;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class BrigadierUtils {
    public static Object withEntity(Object source, Entity entity) {
        try {
            var handleEntity = entity.getClass().getDeclaredMethod("getHandle").invoke(entity);
            for (Method method : source.getClass().getMethods()) {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0].getName().equals("net.minecraft.world.entity.Entity")) {
                    return method.invoke(source, handleEntity);
                }
            }
        } catch (Exception ex) {
            Bouncy.INSTANCE.getLogger().log(Level.SEVERE, "不明なバージョンのマッピング", ex);
        }
        return null;
    }
}