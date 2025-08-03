package me.koutachan.bouncy.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public interface AbilityPotion {
    void onPotion(PlayerItemConsumeEvent event);

}
