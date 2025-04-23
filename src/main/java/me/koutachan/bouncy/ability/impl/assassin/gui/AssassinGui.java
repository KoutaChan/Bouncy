package me.koutachan.bouncy.ability.impl.assassin.gui;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.gamble.GambleBuff;
import me.koutachan.bouncy.utils.gui.GuiBase;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.List;

public class AssassinGui extends GuiBase<AssassinGui> {
    public boolean selected;
    public Player player;

    private final List<AssassinIcon> assassinIcons = new ArrayList<>();

    public AssassinGui(Player player) {
        super(InventoryType.CHEST, "暗殺報酬");
        this.player = player;
        setLayout("""
                _ _ _ _ _ _ _ _ _
                _ x _ _ y _ _ z _
                _ _ _ _ _ _ _ _ _
                """)
                .set("_", createGlassButton())
                .set("x", createAssassinIcon())
                .set("y", createAssassinIcon())
                .set("z", createAssassinIcon())
                .update();
    }

    public AssassinIcon createAssassinIcon() {
        AssassinIcon icon = new AssassinIcon(this, GambleBuff.VALUES[Bouncy.SECURE_RANDOM.nextInt(GambleBuff.VALUES.length)]);
        assassinIcons.add(icon);
        return icon;
    }

    @Override
    public void onGuiClose(InventoryCloseEvent event) {
        if (!selected) {
            assassinIcons.get(Bouncy.SECURE_RANDOM.nextInt(assassinIcons.size())).addBuff();
        }
        super.onGuiClose(event);
    }

    public Player getPlayer() {
        return player;
    }

    public void open() {
        open(player);
    }
}