package me.koutachan.bouncy;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class ArrowTracker {
    public static Map<Player, Projectile> TRACKED_ARROW = new HashMap<>();
}