package me.koutachan.bouncy.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    public final static Map<UUID, GamePlayer> CURRENT_GAME_PLAYER = new HashMap<>();

    public static GamePlayer getGamePlayer(Player player) {
        return CURRENT_GAME_PLAYER.get(player.getUniqueId());
    }

    public static GamePlayer getGamePlayer(UUID uuid) {
        return CURRENT_GAME_PLAYER.get(uuid);
    }

    public static void removeGamePlayer(Player player) {
        GamePlayer gamePlayer = CURRENT_GAME_PLAYER.remove(player.getUniqueId());
        if (gamePlayer != null) {
            gamePlayer.dispose();
        }
    }

    public static void resetAll() {
        for (GamePlayer gamePlayer : CURRENT_GAME_PLAYER.values())  {
            gamePlayer.dispose();
        }
        CURRENT_GAME_PLAYER.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            createGamePlayer(player);
        }
    }

    public static void createGamePlayer(Player player) {
        CURRENT_GAME_PLAYER.put(player.getUniqueId(), new GamePlayer(player));
    }

    public static GamePlayer getGamePlayerOrCreate(Player player) {
        GamePlayer gamePlayer = CURRENT_GAME_PLAYER.get(player.getUniqueId());
        if (gamePlayer == null) {
            CURRENT_GAME_PLAYER.put(player.getUniqueId(), gamePlayer = new GamePlayer(player));
        }
        return gamePlayer;
    }
}