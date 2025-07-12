package me.koutachan.bouncy.ability.impl.assassin.gui;

import me.koutachan.bouncy.ability.impl.gamble.GambleBuff;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.utils.gui.button.impl.ClickButton;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AssassinIcon extends ClickButton<AssassinGui> {
    private final AssassinGui gui;
    private final GambleBuff buff;

    public AssassinIcon(AssassinGui gui, GambleBuff buff) {
        this.gui = gui;
        this.buff = buff;
    }

    @Override
    public void onClick(InventoryClickEvent event, AssassinGui holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        addBuff();
        player.closeInventory();
    }

    public void addBuff() {
        GameManager.getGamePlayerOrCreate(gui.getPlayer()).addGambleBuff(buff);
        gui.getPlayer().sendMessage(String.join("\n", buff.getDescription().getItemMeta().getLore()) + "を選択しました");
        gui.selected = true;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack item = buff.getDescription().clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "暗殺報酬");
        item.setItemMeta(meta);
        return item;
    }
}