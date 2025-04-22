package me.koutachan.bouncy.game;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.google.common.cache.CacheBuilder;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityHandler;
import me.koutachan.bouncy.ability.impl.gamble.*;
import me.koutachan.bouncy.ability.impl.gamble.gui.GambleGui;
import me.koutachan.bouncy.ability.turret.Turret;
import me.koutachan.bouncy.game.task.GameTaskHandler;
import me.koutachan.bouncy.game.task.impl.HunchTask;
import me.koutachan.bouncy.game.task.impl.NoExpTask;
import me.koutachan.bouncy.game.task.impl.NoPotionTask;
import me.koutachan.bouncy.game.task.impl.RandomRotationTask;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class GamePlayer {
    public static int pingId = 0;

    private Player player;

    private final List<GambleBuff> activeBuffs = new ArrayList<>();
    private final List<GambleDeBuff> activeDeBuffs = new ArrayList<>();

    private final Map<String, String> teamTracker = new HashMap<>();

    private final List<Turret> turrets = new ArrayList<>();

    private final ConcurrentMap<Integer, Runnable> pingTask =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
                    .<Integer, Runnable>build()
                    .asMap();

    private long nextGamble;

    private final GameTaskHandler taskHandler = new GameTaskHandler(this);
    private final AbilityHandler abilityHandler;

    public GamePlayer(Player player) {
        this.player = player;
        this.abilityHandler = taskHandler.register(AbilityHandler.class);
    }

    public List<GambleBuff> getActiveBuffs() {
        return activeBuffs;
    }

    public boolean hasBuff(GambleBuff gambleBuff) {
        return activeBuffs.contains(gambleBuff);
    }

    public List<GambleDeBuff> getActiveDeBuffs() {
        return activeDeBuffs;
    }

    public boolean hasDeBuff(GambleDeBuff gambleDeBuff) {
        return activeDeBuffs.contains(gambleDeBuff);
    }

    public void addTurret(Location location) {
        turrets.add(new Turret(this, location));
    }

    public void addGambleBuff(GambleInfo gambleInfo) {
        if (gambleInfo.getType() == GambleBuffType.BUFF) {
            activeBuffs.add((GambleBuff) gambleInfo);
        } else {
            activeDeBuffs.add((GambleDeBuff) gambleInfo);
        }
        gambleInfo.getGambleConsumer().onGamble(player);
    }

    public Map<String, String> getTeamTracker() {
        return teamTracker;
    }

    public void addPingTask(Runnable runnable) {
        pingTask.put(++pingId, runnable);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerPing(pingId));
    }

    public ConcurrentMap<Integer, Runnable> getPingTask() {
        return pingTask;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerInventory getInventory() {
        return player.getInventory();
    }

    public boolean isOpenInventory() {
        return player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING;
    }

    public Location getLocation() {
        return player.getLocation();
    }

    public Location getEyeLocation() {
        return player.getEyeLocation();
    }

    public void addPotionEffect(PotionEffect effect) {
        player.addPotionEffect(effect);
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return player.hasPotionEffect(type);
    }

    public void sendMessage(String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public World getWorld() {
        return player.getWorld();
    }

    public void playSoundPublic(Location location, Sound sound, float volume, float pitch) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    public void tryUseGamble() {
        if (!canUseGamble())
            return;
        new GambleGui().open(player);
        player.setCooldown(Gambler.GAMBLE_ITEM.getType(), Gambler.USE_DELAY_TICK);
        nextGamble = System.currentTimeMillis() + (Gambler.USE_DELAY_MILLS);
    }

    public boolean canUseGamble() {
        return System.currentTimeMillis() >= nextGamble;
    }

    //TODO: Refactoring
    public int getAbilityId() {
        Objective abilityObjective = getObjective("ability");
        return abilityObjective == null ? 0 : getScoreValue(abilityObjective);
    }

    public void limitCt(int ct) {
        Objective objective = getObjective("ability_ct");
        if (objective == null)
            return;
        Score score = objective.getScore(player.getName());
        if (score.isScoreSet() && score.getScore() >= ct) {
            score.setScore(ct);
        }
    }

    public boolean isArrowHitPresent() {
        return getObjective("ArrowHit") != null;
    }

    public int getArrowHit() {
        Objective objective = getObjective("ArrowHit");
        return objective == null ? 0 : getScoreValue(objective);
    }

    public boolean useAbility(int ct) {
        Objective objective = getObjective("ability_ct");
        if (objective == null)
            return false;
        Score score = objective.getScore(player.getName());
        if (score.isScoreSet() && score.getScore() >= ct) {
            score.setScore(0);
            return true;
        }
        return false;
    }

    public int getAbilityCt() {
        Objective objective = getObjective("ability_ct");
        return objective == null ? 0 : getScoreValue(objective);
    }

    public void setAbilityCt(int abilityCt) {
        Objective objective = getObjective("ability_ct");
        if (objective != null) {
            objective.getScore(player.getName()).setScore(abilityCt);
        }
    }

    private Objective getObjective(String objectiveName) {
        return Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getObjective(objectiveName);
    }

    private int getScoreValue(Objective objective) {
        Score score = objective.getScore(player.getName());
        return score.isScoreSet() ? score.getScore() : 0;
    }

    public Ability getAbility() {
        return abilityHandler.getAbility();
    }

    public AbilityHandler getAbilityHandler() {
        return abilityHandler;
    }

    public List<Turret> getTurrets() {
        return turrets;
    }

    public void clearGamble() {
        activeDeBuffs.clear();
        activeBuffs.clear();
        cancelGamble();
    }

    public void cancelGamble() {
        taskHandler.unregister(HunchTask.class);
        taskHandler.unregister(NoExpTask.class);
        taskHandler.unregister(RandomRotationTask.class);
        taskHandler.unregister(NoPotionTask.class);
    }

    public void pauseTasks() {
        taskHandler.pauseTasks();
        pingTask.clear();
    }

    public void resumeTasks(Player player) {
        this.player = player;
        this.taskHandler.resumeTasks();
    }

    public void cancel() {
        taskHandler.unregisterAll();
        for (Turret turret : new ArrayList<>(turrets)) {
            turret.remove();
        }
        pingTask.clear();
    }

    public void runNoExpPoint() {
        taskHandler.registerOverwrite(NoExpTask.class);
    }

    public void runHunch() {
        taskHandler.register(HunchTask.class);
    }

    public void runRandomRotationDeBuff() {
        taskHandler.register(RandomRotationTask.class);
    }

    public void runPotionDeBuff() {
        taskHandler.register(NoPotionTask.class);
    }

    public GameTaskHandler getTaskHandler() {
        return taskHandler;
    }
}