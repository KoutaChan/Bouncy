package me.koutachan.bouncy.ability.impl;

import de.tr7zw.changeme.nbtapi.NBT;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityShoot;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class SpreadAbility extends Ability implements AbilityShoot {
    public final static int ID = 15;

    public SpreadAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        NBT.modifyPersistentData(event.getEntity(), nbt -> {
            nbt.setBoolean("Spread", true);
        });
    }

    @Override
    public String getName() {
        return "拡散";
    }

    @Override
    public String getActionBar() {
        return "能力:拡散 自動発動";
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
