package me.koutachan.bouncy.ability.impl;

import de.tr7zw.changeme.nbtapi.NBT;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityShoot;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class BouncyAbility extends Ability implements AbilityShoot {
    public final static int ID = 13;

    public BouncyAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        int bouncyCount = gamePlayer.getPlayer().isSneaking() ? 8 : 4;
        NBT.modifyPersistentData(event.getEntity(), nbt -> {
            nbt.setInteger("BouncyCount", bouncyCount);
        });
        event.getEntity().addScoreboardTag("bouncy");
    }

    
    @Overrid
    public String getName() {
        return "跳弾";
    }

    @Override
    public String getActionBar() {
        return "能力:跳弾 自動発動";
    }

    @Override
    public int getCt() {
        return 0;
    }

    @Override
    public int getId() {
        return ID;
    }
}