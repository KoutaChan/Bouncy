package me.koutachan.bouncy.utils;

import me.koutachan.bouncy.Bouncy;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.bukkit.entity.Arrow;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class NMSUtils {
    private static boolean failed;

    /*public static void setDeltaMovement(Arrow bukkitArrow, VectorRot vectorRot) {
        if (failed)
            return;
        try {
            AbstractArrow arrow = ((CraftArrow) bukkitArrow).getHandle();
            arrow.setDeltaMovement(vectorRot.deltaMovement);
            arrow.setYRot(vectorRot.yRot);
            arrow.setXRot(vectorRot.xRot);
            arrow.setYHeadRot(vectorRot.yHeadRot);

        } catch (Throwable throwable) {
            Bouncy.INSTANCE.getLogger().log(Level.WARNING, "Failed to disable Border Reverse!", throwable);
            failed = true;
        }
    }*/

    public static void tryDisableReverse(Arrow bukkitArrow) {
        if (failed)
            return;
        try {
            AbstractArrow arrow = (AbstractArrow) getHandle(bukkitArrow);
            BlockHitResult result = arrow.level().clip(new ClipContext(
                    arrow.position(),
                    arrow.position().add(arrow.getDeltaMovement()),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    arrow
            ));
            Method stepAndMove = AbstractArrow.class.getDeclaredMethod("stepMoveAndHit", BlockHitResult.class);
            stepAndMove.setAccessible(true);
            stepAndMove.invoke(arrow, result);
        } catch (Throwable throwable) {
            Bouncy.INSTANCE.getLogger().log(Level.WARNING, "Failed to disable Border Reverse!", throwable);
            failed = true;
        }
    }

    public static Object getHandle(Object obj) throws Exception {
        Method method = obj.getClass().getMethod("getHandle");
        return method.invoke(obj);
    }
}