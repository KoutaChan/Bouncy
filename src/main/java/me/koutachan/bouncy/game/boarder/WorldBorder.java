package me.koutachan.bouncy.game.boarder;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.EntityUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorldBorder extends BukkitRunnable {
    private final static Particle.DustOptions CENTER_DUST_OPTIONS = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.5f);
    private final static Particle.DustOptions OLDER_CENTER_DUST_OPTIONS = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 1.5f);
    public final static String CARE_PACKAGE_MESSAGE = "&c&l[!] ケアパッケージが投下されました";
    public final static String SURVIVE_MESSAGE = "&a&l[!] フェーズを生き延びたため、回復ポーションを入手しました";
    private final static double MOVING_BLOCKS_PER_TICK = 0.05;

    private final Setting setting;
    private final CarePackage carePackage;
    private Setting.PhaseSetting phaseSetting;
    private Controller controller;

    /**
     * 収縮計算用
     */
    private Location startPos;
    private Location currentPos;
    private Location nextPos;

    /**
     * 最終フェーズ用
     */
    private Vector directionVector;
    private int count;

    private final BossBar bossBar;

    public WorldBorder(Setting setting) {
        this.setting = setting;
        this.bossBar = Bukkit.createBossBar("INITIAL | =-_-=", BarColor.BLUE, BarStyle.SOLID);
        this.carePackage = new CarePackage(this);
    }

    @Override
    public void run() {
        if (phaseSetting == null) {
            Bouncy.INSTANCE.getLogger().warning("Invalid phase! controller=" + controller);
            return;
        }

        org.bukkit.WorldBorder border = currentPos.getWorld().getWorldBorder();
        count--;

        switch (controller) {
            case PHASE_WAIT -> onPhaseWait(border);
            case PHASE_MOVING -> onPhaseMoving(border);
            case PHASE_SHRINK -> onPhaseShrink(border);
        }

        onTick();
    }

    public void onTick() {
        handleBossBar();
        handleParticles();
        carePackage.onTick();
    }

    public void handleParticles() {
        if (!phaseSetting.enableEffects) {
            return;
        }

        if (controller == Controller.PHASE_MOVING) {
            spawnBorderParticle(currentPos, CENTER_DUST_OPTIONS);
        } else {
            spawnBorderParticle(nextPos, CENTER_DUST_OPTIONS);
            if (phaseSetting.showOldBorder) {
                spawnBorderParticle(currentPos, OLDER_CENTER_DUST_OPTIONS);
            }
        }
    }

    public void handleBossBar() {
        Setting.PhaseSetting.BossBarSetting bossBarSetting = phaseSetting.getBossBar(controller);
        if (bossBarSetting == null) {
            bossBar.setVisible(false);
            return;
        }

        bossBar.setColor(bossBarSetting.getColor());
        bossBar.setTitle(bossBarSetting.getTranslatedText().replaceAll("%time%", toSecondsText()));
        bossBar.setVisible(true);

        if (bossBarSetting.isFollowProgress()) {
            bossBar.setProgress(getProgress());
        } else {
            bossBar.setProgress(bossBarSetting.getValue());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public double getProgress() {
        return switch (controller) {
            case PHASE_WAIT -> count / (double) phaseSetting.getWaitFor();
            case PHASE_MOVING -> count / (double) phaseSetting.getMoveFor();
            case PHASE_SHRINK -> count / (double) phaseSetting.getShrinkFor();
        };
    }

    private String toSecondsText() {
        return String.format("%.01f", count / 20.0);
    }

    private void spawnBorderParticle(Location location, Particle.DustOptions options) {
        Location spawnPos = location.clone();
        spawnPos.setY(160);
        spawnPos.getWorld().spawnParticle(Particle.DUST, spawnPos, 40, 0.2, 45.0, 0.2, 0.0001, options, true);
    }

    public void onPhaseInitial(org.bukkit.WorldBorder border) {
        count = phaseSetting.getWaitFor();
        controller = Controller.PHASE_WAIT;

        if (phaseSetting.getPhaseController() == Controller.PHASE_SHRINK) {
            nextPos = getRandomPos(
                    phaseSetting.calculateUsingShrinkSize ? phaseSetting.shrinkSize : phaseSetting.size,
                    phaseSetting.lerp
            );
        }

        onPhaseWait(border);

        if (phaseSetting.isPhaseSurvivePotion()) {
            giveSurvivePotion();
        }

        carePackage.killAll();
        if (phaseSetting.isCarePackage()) {
            summonCarePackage();
        }
    }

    public void giveSurvivePotion() {
        for (GamePlayer gamePlayer : GameManager.CURRENT_GAME_PLAYER.values()) {
            if (gamePlayer.getTaskHandler().isPaused() || !gamePlayer.isAlive())
                continue;
            gamePlayer.addHealPotion();
            gamePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', SURVIVE_MESSAGE));
        }
    }

    /**
     * ケアパッケージを投下する
     */
    public void summonCarePackage() {
        for (int i = 0; i < phaseSetting.getPotionCount(); i++) {
            carePackage.summon();
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', CARE_PACKAGE_MESSAGE));
    }

    public void onPhaseWait(org.bukkit.WorldBorder border) {
        if (0 >= count) {
            onPhaseStatusChange();
            switch (phaseSetting.phaseController) {
                case PHASE_SHRINK -> toPhaseShrink(border);
                case PHASE_MOVING -> toPhaseMoving(border);
            }
            return;
        }
        border.setCenter(currentPos);
        border.setSize(calculateSafeBorderSize());
    }

    public void onPhaseStatusChange() {
        if (!phaseSetting.enableEffects) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(currentPos, Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 0.1f, 1.0f);
        }
    }

    private double calculateSafeBorderSize() {
        double configSize = phaseSetting.getSize();
        if (!phaseSetting.safeBorder) {
            return configSize;
        }

        Location defCenter = setting.getCenter();
        double deltaX = Math.abs(nextPos.getX() - defCenter.getX());
        double deltaZ = Math.abs(nextPos.getZ() - defCenter.getZ());
        double requiredSizeX = deltaX + configSize + phaseSetting.safeBorderSize;
        double requiredSizeZ = deltaZ + configSize + phaseSetting.safeBorderSize;
        double requiredSize = Math.max(requiredSizeX, requiredSizeZ);

        return Math.max(configSize, requiredSize);
    }

    public void toPhaseMoving(org.bukkit.WorldBorder border) {
        controller = Controller.PHASE_MOVING;
        count = phaseSetting.getMoveFor();

        Location defaultCenter = setting.getCenter();
        double distance = currentPos.distance(defaultCenter);
        double angle;

        if (distance > setting.moveLimitSize) {
            double deltaX = defaultCenter.getX() - currentPos.getX();
            double deltaZ = defaultCenter.getZ() - currentPos.getZ();
            double angleToCenter = Math.atan2(deltaZ, deltaX);
            double variance = (Math.random() * Math.PI) - (Math.PI / 2);
            angle = angleToCenter + variance;
            Bouncy.INSTANCE.getLogger().fine("Border is nearing limit (" + String.format("%.2f", distance) + " blocks). Steering back to center.");
        } else {
            angle = Math.random() * Math.PI * 2;
        }

        directionVector = new Vector(Math.cos(angle) * MOVING_BLOCKS_PER_TICK, 0, Math.sin(angle) * MOVING_BLOCKS_PER_TICK);
        Bouncy.INSTANCE.getLogger().fine("Moving start: Drifting randomly at " + MOVING_BLOCKS_PER_TICK * 20 + " block/s (" + directionVector + ")");
    }

    public void onPhaseMoving(org.bukkit.WorldBorder border) {
        if (0 >= count) {
            toPhaseMoving(border);
        }

        currentPos.setX(currentPos.getX() + directionVector.getX());
        currentPos.setZ(currentPos.getZ() + directionVector.getZ());

        border.setCenter(currentPos);
        border.setSize(phaseSetting.getSize() - 1e-6, TimeUnit.DAYS, 9999999);
    }

    public void toPhaseShrink(org.bukkit.WorldBorder border) {
        controller = Controller.PHASE_SHRINK;
        count = phaseSetting.getShrinkFor();
        startPos = currentPos.clone();

        border.setSize(phaseSetting.shrinkSize, TimeUnit.MILLISECONDS, count * 50L);
        Bouncy.INSTANCE.getLogger().info("Shrinking start: Center moves to " + nextPos);
    }

    public void onPhaseShrink(org.bukkit.WorldBorder border) {
        if (0 >= count) {
            currentPos = nextPos;
            increasePhase(border);
            return;
        }

        double progress = 1.0 - ((double) count / (double) phaseSetting.getShrinkFor());
        currentPos.setX(lerp(startPos.getX(), nextPos.getX(), progress));
        currentPos.setZ(lerp(startPos.getZ(), nextPos.getZ(), progress));
        border.setCenter(currentPos.getX(), currentPos.getZ());
    }

    private Location getSmoothPos(Location currentPos, Location nextPos, double weight) {
        double newX = lerp(currentPos.getX(), nextPos.getX(), weight);
        double newZ = lerp(currentPos.getZ(), nextPos.getZ(), weight);
        return new Location(currentPos.getWorld(), newX, currentPos.getY(), newZ);
    }

    public Location getRandomPosForCarePackage() {
        return getRandomPos(phaseSetting.shrinkSize, 0.9);
    }

    public Location getRandomPos(double size, double weight) {
        Location defaultCenter = setting.getCenter();
        double max = setting.getDefSize() / 2.0;
        for (int attempt = 0; attempt < 10; attempt++) {
            Location candidate = generateCandidatePos(size, weight);
            if (isWithinBounds(candidate, defaultCenter, max)) {
                return candidate;
            }
        }
        return clampToBounds(generateCandidatePos(size, weight), defaultCenter, max);
    }

    private Location generateCandidatePos(double size, double weight) {
        Location current = currentPos;
        double halfSize = size / 2.0;

        double offX = (Math.random() * 2 - 1) * halfSize;
        double offZ = (Math.random() * 2 - 1) * halfSize;

        double candidateX = current.getX() + offX;
        double candidateZ = current.getZ() + offZ;

        double newX = lerp(current.getX(), candidateX, weight);
        double newZ = lerp(current.getZ(), candidateZ, weight);

        return new Location(current.getWorld(), newX, current.getY(), newZ);
    }

    private boolean isWithinBounds(Location location, Location center, double max) {
        return Math.abs(location.getX() - center.getX()) <= max && Math.abs(location.getZ() - center.getZ()) <= max;
    }

    private Location clampToBounds(Location location, Location center, double max) {
        double clampedX = Math.max(center.getX() - max, Math.min(center.getX() + max, location.getX()));
        double clampedZ = Math.max(center.getZ() - max, Math.min(center.getZ() + max, location.getZ()));

        return new Location(location.getWorld(), clampedX, location.getY(), clampedZ);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public void increasePhase() {
        increasePhase(setting.center.getWorld().getWorldBorder());
    }

    public void increasePhase(org.bukkit.WorldBorder border) {
        if (phaseSetting == null) {
            Setting.PhaseSetting next = setting.getPhaseSettings().get(Phase.PHASE_0);
            if (next == null) {
                // How ???
                Bouncy.INSTANCE.getLogger().warning("Phase_0 is not configured!");
                return;
            }
            phaseSetting = next;
            onPhaseInitial(border);
            Bouncy.INSTANCE.getLogger().info("Phase increased: " + null + " -> " + next.phase);
            return;
        }

        Phase current = phaseSetting.phase;
        int nextOrdinal = current.ordinal() + 1;

        if (nextOrdinal >= Phase.getValues().length) {
            Bouncy.INSTANCE.getLogger().info("Already at the final phase: " + current);
            return;
        }

        Phase nextPhase = Phase.getValues()[nextOrdinal];
        Setting.PhaseSetting nextSetting = setting.getPhaseSettings().get(nextPhase);

        if (nextSetting == null) {
            Bouncy.INSTANCE.getLogger().warning("Phase " + nextPhase + " is not configured!");
            return;
        }

        phaseSetting = nextSetting;
        onPhaseInitial(border);
        Bouncy.INSTANCE.getLogger().info("Phase increased: " + current + " -> " + nextPhase);
    }

    public void start() {
        currentPos = setting.center;
        increasePhase();
        runTaskTimer(Bouncy.INSTANCE, 0, 1);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        bossBar.removeAll();
        carePackage.killAll();
        super.cancel();
    }

    public CarePackage getCarePackage() {
        return carePackage;
    }

    @ApiStatus.Internal
    public void setCount(int count) {
        this.count = count;
    }

    public enum Controller {
        PHASE_MOVING,
        PHASE_SHRINK,
        PHASE_WAIT
    }

    public enum Phase {
        PHASE_0,
        PHASE_1,
        PHASE_2,
        PHASE_3,
        PHASE_4,
        PHASE_5,
        PHASE_6;

        private static final Phase[] VALUES = values();

        public static Phase[] getValues() {
            return VALUES;
        }
    }

    public static class Setting {
        private String name;
        private Location center;
        private final Map<Phase, PhaseSetting> phaseSettings = new HashMap<>();
        private double defSize;
        private double moveLimitSize;

        public String getName() {
            return name;
        }

        public Location getCenter() {
            return center;
        }

        public Map<Phase, PhaseSetting> getPhaseSettings() {
            return phaseSettings;
        }

        public double getDefSize() {
            return defSize;
        }

        public void load(FileConfiguration config) {
            name = config.getString("name", "default");
            defSize = config.getDouble("def-size", 1000.0);
            moveLimitSize = config.getDouble("move-limit-size", 200.0);

            ConfigurationSection centerSection = config.getConfigurationSection("center");
            if (centerSection != null) {
                String worldName = centerSection.getString("world", "world");
                double x = centerSection.getDouble("x", 0);
                double y = centerSection.getDouble("y", 0);
                double z = centerSection.getDouble("z", 0);
                center = new Location(Bukkit.getWorld(worldName), x, y, z);
            } else {
                center = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
            }

            phaseSettings.clear();
            ConfigurationSection phasesSection = config.getConfigurationSection("phases");

            for (String key : phasesSection.getKeys(false)) {
                try {
                    Phase phase = Phase.valueOf(key);
                    ConfigurationSection phaseSection = phasesSection.getConfigurationSection(key);
                    PhaseSetting phaseSetting = new PhaseSetting();
                    phaseSetting.load(phaseSection, phase);
                    phaseSettings.put(phase, phaseSetting);
                } catch (IllegalArgumentException e) {
                    Bouncy.INSTANCE.getLogger().warning("Unknown phase found in config: " + key);
                }
            }
        }

        /**
         * 現在の設定を config.yml に保存する
         */
        public void save(FileConfiguration config) {
            config.set("name", name);
            config.set("def-size", defSize);

            if (center != null) {
                ConfigurationSection centerSection = config.createSection("center");
                centerSection.set("world", center.getWorld().getName());
                centerSection.set("x", center.getX());
                centerSection.set("y", center.getY());
                centerSection.set("z", center.getZ());
            }

            ConfigurationSection phasesSec = config.createSection("phases");
            for (Map.Entry<Phase, PhaseSetting> entry : phaseSettings.entrySet()) {
                ConfigurationSection phaseSection = phasesSec.createSection(entry.getKey().name());
                entry.getValue().save(phaseSection);
            }
        }

        public static class PhaseSetting {
            private Phase phase;
            private double size;
            private double shrinkSize;
            private int waitFor;
            private int shrinkFor;
            private int moveFor;
            private double lerp;
            private boolean borderShrink;
            private Controller phaseController;
            private boolean enableEffects;
            private boolean showOldBorder;
            private boolean safeBorder;
            private double safeBorderSize;
            private boolean calculateUsingShrinkSize;
            private boolean phaseSurvivePotion;
            private boolean carePackage;
            private boolean playerCountAffectsPotions;
            private double potionPosWeight;
            private int basePotionCount;
            private final Map<Controller, BossBarSetting> bossBarSettings = new HashMap<>();

            public PhaseSetting() {
            }

            public double getSize() {
                return size;
            }

            public Controller getPhaseController() {
                return phaseController;
            }

            public boolean isBorderShrink() {
                return borderShrink;
            }

            public int getWaitFor() {
                return waitFor;
            }

            public int getShrinkFor() {
                return shrinkFor;
            }

            public int getMoveFor() {
                return moveFor;
            }

            public boolean isPhaseSurvivePotion() {
                return phaseSurvivePotion;
            }

            public boolean isCarePackage() {
                return carePackage;
            }

            public long getPotionCount() {
                if (playerCountAffectsPotions) {
                    return basePotionCount + EntityUtils.getAlivePlayers();
                } else {
                    return basePotionCount;
                }
            }

            public int getBasePotionCount() {
                return basePotionCount;
            }

            public BossBarSetting getBossBar(Controller controller) {
                return bossBarSettings.get(controller);
            }

            public void load(ConfigurationSection section, Phase phase) {
                this.phase = phase;
                size = section.getDouble("size", 100.0);
                shrinkSize = section.getDouble("shrink-size", 100.0);
                waitFor = section.getInt("wait-for", 60);
                lerp = section.getDouble("lerp", 0.5);
                borderShrink = section.getBoolean("border-shrink", true);

                String controllerStr = section.getString("controller", "PHASE_WAIT");
                try {
                    phaseController = Controller.valueOf(controllerStr);
                } catch (IllegalArgumentException e) {
                    phaseController = Controller.PHASE_WAIT;
                }

                if (phaseController == Controller.PHASE_SHRINK) {
                    shrinkFor = section.getInt("shrink-for", 30);
                } else if (phaseController == Controller.PHASE_MOVING) {
                    moveFor = section.getInt("move-for", 30);
                }

                safeBorder = section.getBoolean("safe-border", false);
                if (safeBorder) {
                    safeBorderSize = section.getInt("safe-border-size");
                }

                calculateUsingShrinkSize = section.getBoolean("calculate-using-shrink-size", true);
                enableEffects = section.getBoolean("enable-effects", true);
                showOldBorder = section.getBoolean("show-old-border", true);
                phaseSurvivePotion = section.getBoolean("phase-survive-potion", true);
                carePackage = section.getBoolean("care-package", false);
                playerCountAffectsPotions = section.getBoolean("player-count-affects-potions", false);
                potionPosWeight = section.getDouble("potion-pos-weight", 1.0);
                basePotionCount = section.getInt("base-potion-count", 1);

                bossBarSettings.clear();
                ConfigurationSection bossBarSection = section.getConfigurationSection("boss-bar");
                for (String key : bossBarSection.getKeys(false)) {
                    try {
                        Controller controller = Controller.valueOf(key.toUpperCase());
                        ConfigurationSection $bossBarSection = bossBarSection.getConfigurationSection(key);
                        BossBarSetting bossBarSetting = new BossBarSetting();
                        bossBarSetting.load($bossBarSection);
                        bossBarSettings.put(controller, bossBarSetting);
                    } catch (IllegalArgumentException e) {
                        Bouncy.INSTANCE.getLogger().warning("Unknown bossBar found in config: " + key);
                    }
                }
            }

            /**
             * セクションへ個別のフェーズ設定を保存する
             */
            public void save(ConfigurationSection section) {
                section.set("radius", size);
                section.set("wait-for", waitFor);
                section.set("shrink-for", shrinkFor);
                section.set("lerp", lerp);
                section.set("border-shrink", borderShrink);

                if (phaseController != null) {
                    section.set("controller", phaseController.name());
                }

                section.set("potion-enabled", carePackage);
                section.set("player-count-affects-potions", playerCountAffectsPotions);
                section.set("potion-pos-weight", potionPosWeight);
                section.set("base-potion-count", basePotionCount);
            }

            public static class BossBarSetting {
                private String text;
                private BarColor color;
                private boolean followProgress;
                private double value;

                public void load(ConfigurationSection section) {
                    text = section.getString("text");
                    String colorStr = section.getString("color", "RED");
                    try {
                        color = BarColor.valueOf(colorStr);
                    } catch (IllegalArgumentException e) {
                        color = BarColor.RED;
                    }
                    followProgress = section.getBoolean("follow-progress", true);
                    if (!followProgress) {
                        value = section.getDouble("value", 0);
                    }
                }

                public String getTranslatedText() {
                    return ChatColor.translateAlternateColorCodes('&', text);
                }

                public String getText() {
                    return text;
                }

                public BarColor getColor() {
                    return color;
                }

                public boolean isFollowProgress() {
                    return followProgress;
                }

                public double getValue() {
                    return value;
                }
            }
        }
    }
}