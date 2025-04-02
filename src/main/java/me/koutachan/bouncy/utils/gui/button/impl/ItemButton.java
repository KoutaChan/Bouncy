package me.koutachan.bouncy.utils.gui.button.impl;

import me.koutachan.bouncy.utils.gui.GuiBase;
import me.koutachan.bouncy.utils.gui.button.AbstractGuiButton;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemButton<T extends GuiBase<T>> extends AbstractGuiButton<T> {
    private ItemStack icon;

    public ItemButton(ItemStack icon) {
        this.icon = icon;
    }

    @Override
    public void onClick(InventoryClickEvent event, T holder) {

    }

    @Override
    public void onAdd() {

    }

    @Override
    public void onRemove() {

    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }
}