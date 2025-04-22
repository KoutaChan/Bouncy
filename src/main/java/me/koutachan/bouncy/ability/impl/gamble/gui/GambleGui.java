package me.koutachan.bouncy.ability.impl.gamble.gui;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.gamble.GambleBuff;
import me.koutachan.bouncy.ability.impl.gamble.GambleDeBuff;
import me.koutachan.bouncy.utils.ItemCreator;
import me.koutachan.bouncy.utils.gui.GuiBase;
import me.koutachan.bouncy.utils.gui.button.impl.ItemButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitTask;

public class GambleGui extends GuiBase<GambleGui> {
    public final GambleBuff buff;
    public final GambleDeBuff deBuff;
    public final GambleIcon gambleIcon = new GambleIcon(this);

    private static final ItemButton<?> FRONT_ITEM = new ItemButton<>(ItemCreator.of(Material.LIME_STAINED_GLASS_PANE)
            .setDisplayName("&a表")
            .create());
    private static final ItemButton<?> BACK_ITEM = new ItemButton<>(ItemCreator.of(Material.RED_STAINED_GLASS_PANE)
            .setDisplayName("&4裏")
            .create());

    public BukkitTask task;

    public GambleGui() {
        super(InventoryType.CHEST, "コイントス");
        this.buff = GambleBuff.VALUES[Bouncy.SECURE_RANDOM.nextInt(GambleBuff.VALUES.length)];
        this.deBuff = GambleDeBuff.VALUES[Bouncy.SECURE_RANDOM.nextInt(GambleDeBuff.VALUES.length)];
        setLayout("""
                a a a _ _ _ b b b
                a x a _ z _ b y b
                a a a _ _ _ b b b
                """)
                .set("a", FRONT_ITEM)
                .set("x", new ItemButton<>(buff.getDescription()))
                .set("b", BACK_ITEM)
                .set("y", new ItemButton<>(deBuff.getDescription()))
                .set("z", gambleIcon)
                .update();
        task = Bukkit.getScheduler().runTaskTimer(Bouncy.INSTANCE, () -> updateIcon(gambleIcon), 0, 20);
    }

    @Override
    public void onGuiClose(InventoryCloseEvent event) {
        super.onGuiClose(event);
        if (task != null) {
            task.cancel();
            task = null; // Helps JVM
        }
    }

    public GambleBuff getBuff() {
        return buff;
    }

    public GambleDeBuff getDeBuff() {
        return deBuff;
    }
}