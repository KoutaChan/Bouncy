package me.koutachan.bouncy.utils.gui.button;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.utils.gui.GuiBase;
import me.koutachan.bouncy.utils.gui.icon.GuiIcon;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class AbstractGuiButton<T extends GuiBase<T>> implements GuiIcon {
    public abstract void onClick(InventoryClickEvent event, T holder);

    public abstract void onAdd();

    public abstract void onRemove();

    public static void lateRun(Runnable runnable) {
        Bukkit.getScheduler().runTask(Bouncy.INSTANCE, runnable);
    }
}