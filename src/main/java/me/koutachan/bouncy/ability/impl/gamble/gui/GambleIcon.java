package me.koutachan.bouncy.ability.impl.gamble.gui;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.gamble.GambleInfo;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.utils.gui.button.impl.ClickButton;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class GambleIcon extends ClickButton<GambleGui> {
    private final GambleGui gambleGui;

    private Status status = Status.NONE;
    private boolean lastStatus;

    private GambleInfo gambleInfo;
    public final static int PER_TICK_DELAY = 15;

    public GambleIcon(GambleGui gambleGui) {
        this.gambleGui = gambleGui;
    }

    @Override
    public void onClick(InventoryClickEvent event, GambleGui holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (status == Status.NONE) {
            gambleGui.task.cancel();
            status = Status.FLIPPING;
            new BukkitRunnable() {
                private int tickExists;

                @Override
                public void run() {
                    if (tickExists++ % PER_TICK_DELAY == 0) {
                        int dividedTickExists = (tickExists / PER_TICK_DELAY);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1 + (dividedTickExists * 0.5F));
                        if (dividedTickExists > 1) {
                            player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1f, 0);
                            status = Status.DONE;
                            GameManager.getGamePlayerOrCreate(player).addGambleBuff(gambleInfo = random());
                            var itemMeta = gambleInfo.getDescription().getItemMeta();
                            player.sendMessage(itemMeta.getDisplayName() + ": " + String.join("\n", itemMeta.getLore()));
                            cancel();
                        }
                    }
                    holder.updateIcon(GambleIcon.this);
                }
            }.runTaskTimer(Bouncy.INSTANCE, 0, 1);
        }
        holder.updateIcon(this);
    }

    public GambleInfo random() {
        return Bouncy.SECURE_RANDOM.nextBoolean() ? gambleGui.buff : gambleGui.deBuff;
    }

    @Override
    public ItemStack getIcon() {
        if (status == Status.DONE) {
            return gambleInfo.getDescription();
        } else if (status == Status.FLIPPING) {
            return random().getDescription();
        } else {
            var item = (lastStatus ? gambleGui.buff.getDescription() : gambleGui.deBuff.getDescription()).clone();
            var meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                var lore = new ArrayList<>(meta.getLore());
                lore.add(ChatColor.YELLOW + "クリックしてコイントスをする");
                meta.setLore(lore);
                meta.setDisplayName(ChatColor.GREEN + "表" + ChatColor.WHITE + "か" + ChatColor.DARK_RED + "裏" + ChatColor.WHITE + "か");
                item.setItemMeta(meta);
            }
            lastStatus = !lastStatus;
            return item;
        }
    }

    enum Status {
        FLIPPING,
        NONE,
        DONE
    }
}