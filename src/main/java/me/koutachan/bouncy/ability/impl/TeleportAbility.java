package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ArrowTracker;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;

public class TeleportAbility extends Ability implements AbilityDrop {
    public final static int ID = 200;

    public TeleportAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onDrop() {
        if (gamePlayer.getAbilityCt() >= getCt()) {
            Projectile projectile = ArrowTracker.TRACKED_ARROW.get(gamePlayer.getPlayer());
            if (projectile != null && !projectile.isDead()) {
                Location pos = projectile.getLocation();
                pos.setYaw(gamePlayer.getLocation().getYaw());
                pos.setPitch(gamePlayer.getLocation().getPitch());
                gamePlayer.getPlayer().teleport(pos);
                gamePlayer.setAbilityCt(0);
            }
        }
    }

    @Override
    public String getName() {
        return "転移";
    }

    @Override
    public String getActionBar() {
        return "能力:転移 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 300;
    }

    @Override
    public int getId() {
        return ID;
    }
}
