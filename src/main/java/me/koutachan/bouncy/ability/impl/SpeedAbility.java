package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class SpeedAbility extends Ability {
    public static final int ID = 1;

    private static final PotionEffect[] SPEED_EFFECTS = new PotionEffect[] {
            new PotionEffect(PotionEffectType.SPEED, 1, 1, true, false),
            new PotionEffect(PotionEffectType.SPEED, 1, 3, true, false),
            new PotionEffect(PotionEffectType.SPEED, 1, 5, true, false),
            new PotionEffect(PotionEffectType.SPEED, 1, 7, true, false),
            new PotionEffect(PotionEffectType.SPEED, 1, 9, true, false)
    };

    private static final int CHARGE_INTERVAL = 30;
    private static final int MAX_CHARGE_LEVEL = SPEED_EFFECTS.length;
    private static final int PARTICLE_SOUND_THRESHOLD = CHARGE_INTERVAL * 3;

    private static final double DEFAULT_STEP_HEIGHT = 0.6;
    private static final double ENHANCED_STEP_HEIGHT = 1.2;

    private static final char CHARGE_ICON = '■';

    private static final PotionEffect JUMP_BOOST_EFFECT = new PotionEffect(PotionEffectType.JUMP_BOOST, 5, 9, true, false);

    private int chargeTicks;
    private Location lastPosition;

    public SpeedAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (shouldSkipTick()) {
            return;
        }

        Location currentPosition = gamePlayer.getLocation();
        processMovement(currentPosition);
        applyEffects();

        lastPosition = currentPosition;
    }

    private boolean shouldSkipTick() {
        if (lastPosition == null || gamePlayer.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            lastPosition = gamePlayer.getLocation();
            return true;
        }
        return false;
    }

    private void processMovement(Location currentPosition) {
        Location deltaPosition = currentPosition.clone().subtract(lastPosition);
        if (isFullyChargedAndJumping(deltaPosition)) {
            resetCharge();
        } else if (gamePlayer.getPlayer().isHandRaised()) {
            incrementCharge();
        } else if (!gamePlayer.getPlayer().isSprinting()) {
            resetCharge(); // 1.21からは弓を構えると "Sprint"状態が解除される
        } else {
            incrementCharge();
        }
    }

    private boolean isFullyChargedAndJumping(Location deltaPosition) {
        return getChargeLevel() == MAX_CHARGE_LEVEL &&
                !gamePlayer.getPlayer().isOnGround() &&
                deltaPosition.getY() > 0;
    }

    private void incrementCharge() {
        chargeTicks++;
    }

    private void resetCharge() {
        chargeTicks = 0;
    }

    private void applyEffects() {
        resetStepHeight();

        int chargeLevel = getChargeLevel();
        if (chargeLevel > 0) {
            applySpeedEffect(chargeLevel);
            if (chargeTicks >= PARTICLE_SOUND_THRESHOLD) {
                showEffects();
            }
            if (chargeLevel == MAX_CHARGE_LEVEL) {
                applyMaxChargeEffects();
            }
        }
    }

    private void applySpeedEffect(int chargeLevel) {
        gamePlayer.addPotionEffect(SPEED_EFFECTS[chargeLevel - 1]);
    }

    private void showEffects() {
        Location playerPos = gamePlayer.getLocation();
        gamePlayer.getWorld().spawnParticle(Particle.SWEEP_ATTACK, playerPos.add(0, 0.4, 0), 1, 0.4, 0.2, 0.4, 0);
        gamePlayer.playSoundPublic(playerPos, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 0.2f);
    }

    private void applyMaxChargeEffects() {
        gamePlayer.addPotionEffect(JUMP_BOOST_EFFECT);
        gamePlayer.getWorld().spawnParticle(Particle.SWEEP_ATTACK, gamePlayer.getLocation().add(0, 1, 0), 1, 0.5, 0.5, 0.5, 0);
        gamePlayer.getPlayer().getAttribute(Attribute.STEP_HEIGHT).setBaseValue(ENHANCED_STEP_HEIGHT);
    }

    @Override
    public void onDispose() {
        resetStepHeight();
    }

    @Override
    public void onAbilityChange(@Nullable Ability to) {
        resetStepHeight();
    }

    public void resetStepHeight() {
        gamePlayer.getPlayer().getAttribute(Attribute.STEP_HEIGHT).setBaseValue(DEFAULT_STEP_HEIGHT);
    }

    public int getChargeLevel() {
        return Math.min(MAX_CHARGE_LEVEL, chargeTicks / CHARGE_INTERVAL);
    }

    @Override
    public String getName() {
        return "疾走";
    }

    @Override
    public String getActionBar() {
        int chargeLevel = getChargeLevel();
        String chargeStyle = ChatColor.AQUA + String.valueOf(CHARGE_ICON).repeat(Math.max(0, chargeLevel)) +
                ChatColor.GRAY + String.valueOf(CHARGE_ICON).repeat(Math.max(0, MAX_CHARGE_LEVEL - chargeLevel));
        return "能力:疾走 " + chargeStyle;
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