package me.koutachan.bouncy.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GuiHolder implements InventoryHolder {
    public Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void onGuiCreate(Inventory inventory) {
        this.inventory = inventory;
    }

    public void onGuiOpen(InventoryOpenEvent event) {
    }

    public void onGuiClose(InventoryCloseEvent event) {
    }

    public void onGuiClick(InventoryClickEvent event) {
    }

    public void onGuiDrag(InventoryDragEvent event) {
    }

    public InventoryType getType() {
        return inventory.getType();
    }

    public int getSize() {
        return inventory.getSize();
    }

    public int getDefaultSize() {
        return 27;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}