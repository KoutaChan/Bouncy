package me.koutachan.bouncy.utils.gui.button.impl;

import me.koutachan.bouncy.utils.gui.GuiBase;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CloseButton<T extends GuiBase<T>> extends ItemButton<T> {
    public CloseButton(ItemStack icon) {
        super(icon);
    }

    @Override
    public void onClick(InventoryClickEvent event, T holder) {
        lateRun(() -> event.getWhoClicked().closeInventory());
    }
}