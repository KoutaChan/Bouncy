package me.koutachan.bouncy.events;

import me.koutachan.bouncy.ability.impl.assassin.gui.AssassinGui;
import me.koutachan.bouncy.ability.impl.assassin.AssassinItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

public class AssassinListener implements Listener {
    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() == null
                || !event.getItem().hasItemMeta()
                || !event.getItem().getItemMeta().getPersistentDataContainer().getOrDefault(AssassinItems.ASSASSIN_NAMESPACED_KEY, PersistentDataType.BOOLEAN, false)) {
            return;
        }
        new AssassinGui(event.getPlayer()).open();
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
}
