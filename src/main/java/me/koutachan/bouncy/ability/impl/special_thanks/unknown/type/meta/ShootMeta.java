package me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public record ShootMeta(ProjectileLaunchEvent event) implements TriggerMeta {
}