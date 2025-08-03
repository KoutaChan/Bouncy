package me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import org.bukkit.entity.Player;

public record KillMeta(Player victim) implements TriggerMeta {
}