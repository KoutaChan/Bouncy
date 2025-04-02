package me.koutachan.bouncy.utils.gui.button.impl;

import me.koutachan.bouncy.utils.gui.GuiBase;
import me.koutachan.bouncy.utils.gui.GuiHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RedirectButton<T extends GuiBase<T>> extends ItemButton<T> {
    private final GuiHolder redirect;

    public RedirectButton(ItemStack icon, GuiHolder redirect) {
        super(icon);
        this.redirect = redirect;
    }

    @Override
    public void onClick(InventoryClickEvent event, T holder) {
        lateRun(() -> redirect.open((Player) event.getWhoClicked()));
    }
}