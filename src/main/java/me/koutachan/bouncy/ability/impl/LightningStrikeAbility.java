package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.ability.AbilityShoot;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.FormatUtils;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import org.bukkit.*;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.net.JarURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LightningStrikeAbility extends Ability implements AbilityShoot, AbilityDrop {
    public final static int ID = 50;
    private final static int MAX_SUSPENDABLE_COUNT = 5;
    private final static double BUFF_RANGE = 20 * 20;
    private final static double AUTO_STRIKE_RANGE = 2 * 2;

    private int buffAmount;
    private final static PotionEffect SPEED = new PotionEffect(PotionEffectType.SPEED, 2, 1, true, false);
    private final static PotionEffect JUMP = new PotionEffect(PotionEffectType.JUMP_BOOST, 2, 0, true, false);

    private final List<AbstractArrow> suspendedArrows = new ArrayList<>();

    public LightningStrikeAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        buffAmount = 0;
        for (Iterator<AbstractArrow> it = suspendedArrows.iterator(); it.hasNext();) {
            AbstractArrow arrow = it.next();
            if (!arrow.isValid()) {
                it.remove();
                continue;
            }
            Location location = arrow.getLocation();
            /*
             * バフ付与
             */
            if (BUFF_RANGE >= location.distanceSquared(gamePlayer.getLocation())) {
                switch (++buffAmount) {
                    case 1 -> gamePlayer.addPotionEffect(SPEED);
                    case 2 -> gamePlayer.addPotionEffect(JUMP);
                    case 5 -> gamePlayer.getPlayer().setExp(Math.min(1, gamePlayer.getPlayer().getExp() + 0.01F));
                }
            }
            /*
             * 矢の移動阻止
             */
            if (arrow.getVelocity().getY() < 0) {
                arrow.setVelocity(new Vector(0, 0, 0));
                arrow.setRotation(0, -90);
            }
            /*
             * 矢の落下
             */
            for (Player player : arrow.getTrackedBy()) {
                if (player.getGameMode() != GameMode.ADVENTURE || DamageUtils.isSameTeam(player, gamePlayer.getPlayer())) {
                    continue;
                }
                Location squaredPos = player.getLocation().subtract(location);
                if (AUTO_STRIKE_RANGE >= squaredPos.getX() * squaredPos.getX() + squaredPos.getZ() * squaredPos.getZ()) {
                    handleLightningStrike(arrow);
                    it.remove();
                    break;
                }
            }
            /*
             * 矢のエフェクト
             */
            arrow.getWorld().spawnParticle(Particle.DUST, location, 10, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(Color.YELLOW, 1.0F));
            arrow.getWorld().spawnParticle(Particle.DUST, location, 5, 0.3, 0.3, 0.3, 0, new Particle.DustOptions(Color.WHITE, 0.8F));
        }
        gamePlayer.limitCt(getCt());
    }

    public void handleLightningStrike(AbstractArrow arrow) {
        if (arrow.isValid()) {
            arrow.setVelocity(new Vector(0, -2, 0));
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    Location location = arrow.getLocation();
                    if (!arrow.isValid() || arrow.isOnGround() || arrow.isInBlock()) {
                        arrow.getWorld().strikeLightningEffect(location);
                        for (Entity entity : arrow.getWorld().getNearbyEntities(location, 1.7, 2, 1.7)) {
                            if (entity instanceof Player player) {
                                DamageUtils.damage(gamePlayer.getPlayer(), player, 2);
                            }
                        }
                        arrow.remove();
                        cancel();
                    }
                    /*
                     * 矢のエフェクト
                     */
                    arrow.getWorld().spawnParticle(Particle.DUST, location, 10, 0.2, 0.2, 0.2, 0, new Particle.DustOptions(Color.YELLOW, 1.0F));
                    arrow.getWorld().spawnParticle(Particle.DUST, location, 5, 0.3, 0.3, 0.3, 0, new Particle.DustOptions(Color.WHITE, 0.8F));
                }
            };
            task.runTaskTimer(Bouncy.INSTANCE, 1L, 1L);
        }
    }

    @Override
    public void onDrop() {
        if (suspendedArrows.isEmpty() || !gamePlayer.useAbility(getCt())) {
            return;
        }
        for (AbstractArrow arrow : suspendedArrows) {
            handleLightningStrike(arrow);
        }
        suspendedArrows.clear();
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        if (gamePlayer.getPlayer().isSneaking() && event.getEntity() instanceof AbstractArrow abstractArrow) {
            if (suspendedArrows.size() >= MAX_SUSPENDABLE_COUNT) {
                suspendedArrows.remove(0);
            }
            event.getEntity().addScoreboardTag("lightning_strike");
            suspendedArrows.add(abstractArrow);
        }
    }

    @Override
    public String getName() {
        return "雷撃";
    }

    @Override
    public String getActionBar() {
        return "能力:雷撃 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能 | " + suspendedArrows.size() + "本 バフ: " + buffAmount + "本）";
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