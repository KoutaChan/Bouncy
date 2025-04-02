package me.koutachan.bouncy.ability.impl;

import de.tr7zw.changeme.nbtapi.NBT;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.ability.AbilityShoot;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ExplosiveAbility extends Ability implements AbilityDrop, AbilityShoot {
    public static final int ID = 6;
    public boolean active;

    public ExplosiveAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        if (active) {
            NBT.modifyPersistentData(event.getEntity(), nbt -> {
                nbt.setBoolean("Explosive", true);
            });
            active = false;
        }
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            active = true;
        }
    }

    @Override
    public String getName() {
        return "爆弾";
    }

    @Override
    public String getActionBar() {
        return "能力:爆弾 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能）";
    }

    @Override
    public int getCt() {
        return 500;
    }

    @Override
    public int getId() {
        return ID;
    }
}