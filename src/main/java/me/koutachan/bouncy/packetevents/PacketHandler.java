package me.koutachan.bouncy.packetevents;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.game.GamePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketHandler implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        GamePlayer gamePlayer = GameManager.getGamePlayer(event.getUser().getUUID());
        if (gamePlayer == null) return;

        if (packetType == PacketType.Play.Client.PONG) {
            handlePingPackets(event, gamePlayer);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (event.getPlayer() == null)
            return;

        GamePlayer gamePlayer = GameManager.getGamePlayerOrCreate(event.getPlayer());
        if (gamePlayer == null) return;

        if (packetType == PacketType.Play.Server.TEAMS) {
            handleTeamPackets(event, gamePlayer);
        }
    }

    private void handleTeamPackets(PacketSendEvent event, GamePlayer gamePlayer) {
        Map<String, String> teamTracker = gamePlayer.getTeamTracker();
        WrapperPlayServerTeams teams = new WrapperPlayServerTeams(event);
        switch (teams.getTeamMode()) {
            case CREATE, ADD_ENTITIES -> teams.getPlayers().forEach(player -> teamTracker.put(player, teams.getTeamName()));
            case REMOVE, REMOVE_ENTITIES -> handleRemoveTeams(event, teamTracker, teams);
        }
    }

    private void handleRemoveTeams(PacketSendEvent event, Map<String, String> teamTracker, WrapperPlayServerTeams teams) {
        List<String> players = new ArrayList<>();
        for (String player : teams.getPlayers()) {
            if (teamTracker.remove(player, teams.getTeamName())) {
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            event.setCancelled(true);
        } else {
            teams.setPlayers(players);
            event.markForReEncode(true);
        }
    }

    private void handlePingPackets(PacketReceiveEvent event, GamePlayer gamePlayer) {
        Runnable runnable = gamePlayer.getPingTask().remove(new WrapperPlayClientPong(event).getId());
        if (runnable != null) {
            runnable.run();
        }
    }
}
