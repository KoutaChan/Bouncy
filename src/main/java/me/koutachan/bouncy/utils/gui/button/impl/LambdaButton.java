package me.koutachan.bouncy.utils.gui.button.impl;

import me.koutachan.bouncy.utils.gui.GuiBase;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class LambdaButton<T extends GuiBase<T>> extends ItemButton<T> {
    private final Lambda<T> lambda;

    public LambdaButton(ItemStack icon, Lambda<T> lambda) {
        super(icon);
        this.lambda = lambda;
    }

    @Override
    public void onClick(InventoryClickEvent event, T holder) {
        lambda.onClick(event, holder);
    }

    public interface Lambda<T> {
        void onClick(InventoryClickEvent event, T holder);
    }
}