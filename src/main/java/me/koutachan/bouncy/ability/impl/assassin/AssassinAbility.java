package me.koutachan.bouncy.ability.impl.assassin;

import fr.skytasul.glowingentities.GlowingTeam;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityKill;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssassinAbility extends Ability implements AbilityKill {
    public static final int ID = 49;

    private Player target;

    public AssassinAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (target == null || gamePlayer.useAbility(getCt())) {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            Collections.shuffle(players);
            for (Player target : players) {
                if (target.getGameMode() == GameMode.ADVENTURE && !DamageUtils.isSameTeam(target, gamePlayer.getPlayer())) {
                    handleTargetChange(target);
                }
            }
        }
        gamePlayer.limitCt(getCt());
    }

    public void handleTargetChange(Player target) {
        unsetGlow();
        try {
            Bouncy.GLOW_API.setGlowing(target, gamePlayer.getPlayer(), new GlowingTeam(ChatColor.RED, Team.OptionStatus.NEVER, Team.OptionStatus.NEVER));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        this.target = target;
    }

    @Override
    public void onAbilityChange(@Nullable Ability to) {
        unsetGlow();
    }

    public void unsetGlow() {
        if (target == null)
            return;
        try {
            Bouncy.GLOW_API.unsetGlowing(target, gamePlayer.getPlayer());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onKill(Player victim) {
        if (target == victim) {
            gamePlayer.getInventory().addItem(AssassinItems.ASSASSIN_ITEMS);
        }
    }

    @Override
    public String getName() {
        return "暗殺";
    }

    @Override
    public String getActionBar() {
        String abilityText = "能力:暗殺 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒"
                + "（" + FormatUtils.formatTick(getCt()) + "秒で暗殺対象変更 | ";
        if (target != null) {
            if (target.getGameMode() != GameMode.ADVENTURE || target.isDead()) {
                return abilityText + target.getName() + " 距離: N/A）";
            } else {
                Location location = target.getLocation().subtract(gamePlayer.getLocation());

                double distance = Math.sqrt(location.getX() * location.getX() + location.getZ() * location.getZ());
                String direction = ChatColor.YELLOW.toString() + ChatColor.BOLD + getDirectionArrow(location.getX(), location.getZ(), gamePlayer.getPlayer().getLocation().getYaw()) + ChatColor.RESET;

                return abilityText + target.getName() + " 距離: " + String.format("%.1f", distance) + "m " + direction + "）";
            }
        } else {
            return abilityText + "対象なし）";
        }
    }

    private String getDirectionArrow(double dx, double dz, float playerYaw) {
        double worldAngle = Math.toDegrees(Math.atan2(dx, dz));

        worldAngle = -worldAngle;
        if (worldAngle < -180) worldAngle += 360;
        if (worldAngle > 180) worldAngle -= 360;

        if (playerYaw > 180) playerYaw -= 360;

        double relativeAngle = worldAngle - playerYaw;

        if (relativeAngle < -180) relativeAngle += 360;
        if (relativeAngle > 180) relativeAngle -= 360;

        if (relativeAngle >= -22.5 && relativeAngle < 22.5) return "↑";
        else if (relativeAngle >= 22.5 && relativeAngle < 67.5) return "⬈";
        else if (relativeAngle >= 67.5 && relativeAngle < 112.5) return "→";
        else if (relativeAngle >= 112.5 && relativeAngle < 157.5) return "⬊";
        else if ((relativeAngle >= 157.5 && relativeAngle <= 180) || (relativeAngle >= -180 && relativeAngle < -157.5)) return "↓";
        else if (relativeAngle >= -157.5 && relativeAngle < -112.5) return "⬋";
        else if (relativeAngle >= -112.5 && relativeAngle < -67.5) return "←";
        else return "⬉";
    }

    @Override
    public int getCt() {
        return 1200;
    }

    @Override
    public int getId() {
        return ID;
    }
}