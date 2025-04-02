package me.koutachan.bouncy.ability.impl;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.EntityUtils;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Comparator;

public class ConfusionAbility extends Ability implements AbilityDrop {
    private int stock;

    private final static int MAX_STOCK = 5;
    private final static char CHARGE_ICON = '◎';

    public final static int ID = 24;

    public ConfusionAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (gamePlayer.useAbility(getCt())) {
            if (++stock > MAX_STOCK) {
                stock = MAX_STOCK;
            }
        }
    }

    @Override
    public void onDrop() {
        if (stock <= 0) {
            return;
        }
        Player player = EntityUtils.getNearestEnemy(gamePlayer.getPlayer());
        if (player != null) {
            Location location = player.getLocation();
            location.setYaw(Bouncy.SECURE_RANDOM.nextFloat(-180, 180));
            location.setPitch(Bouncy.SECURE_RANDOM.nextFloat(-90, 90));
            location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            player.teleport(location);
            stock--;
        }
    }

    @Override
    public String getName() {
        return "混乱";
    }

    @Override
    public String getActionBar() {
        String chargeStyle = ChatColor.YELLOW + String.valueOf(CHARGE_ICON).repeat(Math.max(0, stock)) +
                             ChatColor.GRAY + String.valueOf(CHARGE_ICON).repeat(Math.max(0, MAX_STOCK - stock));
        return "能力:混乱 " + FormatUtils.formatTick(Math.abs(getCt() - gamePlayer.getAbilityCt())) + "秒" + "（" + chargeStyle + ChatColor.WHITE + "）";
    }

    @Override
    public int getCt() {
        return 250;
    }

    @Override
    public int getId() {
        return ID;
    }
}