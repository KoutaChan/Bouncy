package me.koutachan.bouncy.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class GuiPlayer extends GuiBase<GuiPlayer> {
    private final Player player;

    public GuiPlayer(Player player, InventoryType type) {
        super(type);
        this.player = player;
    }

    public GuiPlayer(Player player, InventoryType type, String title) {
        super(type, title);
        this.player = player;
    }

    public GuiPlayer(Player player, int size) {
        super(size);
        this.player = player;
    }

    public GuiPlayer(Player player, int size, String title) {
        super(size, title);
        this.player = player;
    }

    @Override
    public void onGuiClose(InventoryCloseEvent event) {
        super.onGuiClose(event);
        buttons.clear();
    }

    public void open() {
        open(player);
    }

    public Player getPlayer() {
        return player;
    }
}