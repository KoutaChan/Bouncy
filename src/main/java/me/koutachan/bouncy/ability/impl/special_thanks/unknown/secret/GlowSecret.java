package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import fr.skytasul.glowingentities.GlowingTeam;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GlowSecret extends SkillSecret {
    private int ticks;
    private boolean glow;

    public GlowSecret(GamePlayer gamePlayer, TriggerType type) {
        super(gamePlayer, type);
    }

    @Override
    public void onActivated(TriggerMeta meta) {
        switch (type) {
            case TICK -> {
                if (gamePlayer.getPlayer().isSneaking()) {
                    ticks = 1;
                }
            }
            case HIT, DAMAGE, JUMP_5, DROP_1, SHOOT -> ticks = 40;
            case KILL, JUMP_10, DROP_2, DRINK_POTION -> ticks = 80;
        }
    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {
        if (type == TriggerType.TICK) {
            if (ticks > 0) {
                handleGlow();
                ticks--;
            } else {
                unsetGlow();
            }
        }
    }

    public void handleGlow() {
        Player player = gamePlayer.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        glow = true;
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(player))
                continue;
            ChatColor glowColor = DamageUtils.isSameTeam(viewer, player)
                    ? ChatColor.GREEN
                    : ChatColor.RED;
            try {
                Bouncy.GLOW_API.setGlowing(viewer, player, new GlowingTeam(glowColor));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to set glowing effect", e);
            }
        }
    }

    public void unsetGlow() {
        if (!glow)
            return;
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                Bouncy.GLOW_API.unsetGlowing(viewer, gamePlayer.getPlayer());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to unset glowing effect", e);
            }
        }
    }

    @Override
    public String asMessage() {
        return switch (type) {
            case HIT -> "ヒット時、一時的に発光させる";
            case KILL -> "敵を殺したとき、一時的に発光させる";
            case TICK -> "スニーク時、発光させる";
            case DAMAGE -> "ダメージを受けたとき、一時的に発光させる";
            case SHOOT -> "矢を打ったとき、一時的に発光させる";
            case DRINK_POTION -> "ポーションを飲んだとき、一時的に発光させる";
            case DROP_1 -> "スキルを1回発動させたとき、一時的に発光させる";
            case DROP_2 -> "スキルを2回発動させたとき、一時的に発光させる";
            case JUMP_5 -> "ジャンプを5回したとき、一時的に発光させる";
            case JUMP_10 -> "ジャンプを10回したとき、一時的に発光させる";
        };
    }
}
