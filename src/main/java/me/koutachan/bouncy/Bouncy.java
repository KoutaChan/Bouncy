package me.koutachan.bouncy;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.metaphoriker.pathetic.api.factory.PathfinderFactory;
import de.metaphoriker.pathetic.api.factory.PathfinderInitializer;
import de.metaphoriker.pathetic.api.pathing.Pathfinder;
import de.metaphoriker.pathetic.api.pathing.configuration.HeuristicWeights;
import de.metaphoriker.pathetic.api.pathing.configuration.PathfinderConfiguration;
import de.metaphoriker.pathetic.bukkit.PatheticBukkit;
import de.metaphoriker.pathetic.bukkit.initializer.BukkitPathfinderInitializer;
import de.metaphoriker.pathetic.bukkit.provider.LoadingNavigationPointProvider;
import de.metaphoriker.pathetic.engine.factory.AStarPathfinderFactory;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerRangeArgument;
import dev.jorel.commandapi.commandsenders.BukkitNativeProxyCommandSender;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.skytasul.glowingentities.GlowingEntities;
import me.koutachan.bouncy.commands.*;
import me.koutachan.bouncy.events.*;
import me.koutachan.bouncy.packetevents.PacketHandler;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.brigadier.BrigadierUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked"})
public final class Bouncy extends JavaPlugin {
    public static Bouncy INSTANCE;
    public static SecureRandom SECURE_RANDOM;
    public static GlowingEntities GLOW_API;

    public Pathfinder pathfinder;

    private PacketListenerCommon handler;

    static {
        try {
            SECURE_RANDOM = SecureRandom.getInstanceStrong();
        } catch (Exception ex) {
            SECURE_RANDOM = new SecureRandom();
        }
    }

    @Override
    public void onLoad() {
        INSTANCE = this;
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .initializeNBTAPI(NBTContainer.class, NBTContainer::new)
                .verboseOutput(true));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new ArrowListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new GambleListener(), this);
        getServer().getPluginManager().registerEvents(new TurretListener(), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(), this);
        CommandAPI.registerCommand(ArrowCommand.class);
        CommandAPI.registerCommand(PersistentCommand.class);
        CommandAPI.registerCommand(CustomSummonCommand.class);
        CommandAPI.registerCommand(CustomDamageCommand.class);
        CommandAPI.registerCommand(VelocityCommand.class);
        CommandAPI.registerCommand(CustomEffectCommand.class);
        CommandAPI.registerCommand(CustomTitleCommand.class);
        CommandAPI.registerCommand(GambleTestCommand.class);
        CommandAPI.registerCommand(ResetDataCommand.class);
        CommandAPI.registerCommand(GlowCommand.class);
        CommandAPI.registerCommand(PingCommand.class);

        registerProjectileRelation();
        registerShooterRelation();
        registerSneakCondition();
        registerUnlessSneakCondition();
        registerEnemyRelation();
        registerAllEnemyRelation();
        registerAsEnemyRelation();

        registerTeamRelation();
        registerAllTeamRelation();
        registerAsTeamRelation();

        registerSecureRandom();

        PacketEvents.getAPI().getEventManager().registerListeners(handler = new PacketHandler().asAbstract(PacketListenerPriority.NORMAL));

        PatheticBukkit.initialize(this);

        PathfinderFactory factory = new AStarPathfinderFactory();

        PathfinderInitializer initializer = new BukkitPathfinderInitializer();
        PathfinderConfiguration configuration = PathfinderConfiguration.builder()
                .provider(new LoadingNavigationPointProvider())
                .async(false)
                .heuristicWeights(HeuristicWeights.DIRECT_PATH_WEIGHTS)
                .fallback(true)
                .build();

        pathfinder = factory.createPathfinder(configuration, initializer);

        GLOW_API = new GlowingEntities(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        CommandAPI.onDisable();

        PacketEvents.getAPI().getEventManager().unregisterListener(handler);
    }

    private void registerProjectileRelation() {
        var projectileArgument = LiteralArgumentBuilder.literal("projectile")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player player)) {
                        return Collections.emptyList();
                    }
                    Projectile projectile = ArrowTracker.TRACKED_ARROW.get(player);
                    return projectile != null && !projectile.isDead()
                            ? List.of(Brigadier.getBrigadierSourceFromCommandSender(projectile))
                            : Collections.emptyList();
                });
        Brigadier.getRootNode().getChild("execute").getChild("on").addChild(projectileArgument.build());
    }

    private void registerSneakCondition() {
        var sneakArgument = LiteralArgumentBuilder.literal("sneak")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player player)) {
                        return Collections.emptyList();
                    }
                    return player.isSneaking()
                            ? Collections.singleton(Brigadier.getBrigadierSourceFromCommandSender(sender))
                            : Collections.emptyList();
                });
        Brigadier.getRootNode().getChild("execute").getChild("if").addChild(sneakArgument.build());
    }

    private void registerUnlessSneakCondition() {
        var sneakArgument = LiteralArgumentBuilder.literal("sneak")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player player)) {
                        return Collections.singleton(Brigadier.getBrigadierSourceFromCommandSender(sender));
                    }
                    return !player.isSneaking()
                            ? Collections.singleton(Brigadier.getBrigadierSourceFromCommandSender(sender))
                            : Collections.emptyList();
                });
        Brigadier.getRootNode().getChild("execute").getChild("unless").addChild(sneakArgument.build());
    }

    private void registerAsEnemyRelation() {
        var enemyArgument = LiteralArgumentBuilder.literal("asenemy").build();
        var multiSelector = new EntitySelectorArgument.ManyPlayers("selector");
        var selector = Brigadier.fromArgument(multiSelector)
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player owner)) {
                        return Collections.emptyList();
                    }
                    return ((List<Player>) Brigadier.parseArguments(context, List.of(multiSelector))[0])
                            .stream()
                            .filter(o -> o != owner && o.getGameMode() == GameMode.ADVENTURE && !DamageUtils.isSameTeam(owner, o))
                            .map(o -> BrigadierUtils.withEntity(context.getSource(), o))
                            .toList();
                });
        enemyArgument.addChild(selector.build());
        Brigadier.getRootNode().getChild("execute").addChild(enemyArgument);

    }

    private void registerAllEnemyRelation() {
        var enemyArgument = LiteralArgumentBuilder.literal("allenemy")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player owner)) {
                        return Collections.emptyList();
                    }
                    return Bukkit.getOnlinePlayers()
                            .stream()
                            .filter(o -> o != owner && o.getGameMode() == GameMode.ADVENTURE && !DamageUtils.isSameTeam(owner, o))
                            .sorted(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(owner.getLocation())))
                            .map(o -> BrigadierUtils.withEntity(context.getSource(), o))
                            .toList();
                });
        Brigadier.getRootNode().getChild("execute").addChild(enemyArgument.build());
    }

    private void registerAsTeamRelation() {
        var teamArgument = LiteralArgumentBuilder.literal("asteam").build();
        var multiSelector = new EntitySelectorArgument.ManyPlayers("selector");
        var selector = Brigadier.fromArgument(multiSelector)
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player owner)) {
                        return Collections.emptyList();
                    }
                    return ((List<Player>) Brigadier.parseArguments(context, List.of(multiSelector))[0])
                            .stream()
                            .filter(o -> o != owner && o.getGameMode() == GameMode.ADVENTURE && DamageUtils.isSameTeam(owner, o))
                            .map(o -> BrigadierUtils.withEntity(context.getSource(), o))
                            .toList();
                });
        teamArgument.addChild(selector.build());
        Brigadier.getRootNode().getChild("execute").addChild(teamArgument);
    }

    private void registerAllTeamRelation() {
        var teamArgument = LiteralArgumentBuilder.literal("allteam")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player owner)) {
                        return Collections.emptyList();
                    }
                    List<Player> sortedPlayers = Bukkit.getOnlinePlayers().stream()
                            .filter(o -> o != owner)
                            .filter(o -> o.getGameMode() == GameMode.ADVENTURE)
                            .sorted(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(owner.getLocation())))
                            .collect(Collectors.toList());
                    List<Object> result = new ArrayList<>();
                    for (Player player : sortedPlayers) {
                        if (DamageUtils.isSameTeam(owner, player)) {
                            result.add(Brigadier.getBrigadierSourceFromCommandSender(player));
                        }
                    }
                    return result;
                });
        Brigadier.getRootNode().getChild("execute").addChild(teamArgument.build());
    }

    private void registerTeamRelation() {
        var teamArgument = LiteralArgumentBuilder.literal("team")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player owner)) {
                        return Collections.emptyList();
                    }
                    List<Player> sortedPlayers = Bukkit.getOnlinePlayers().stream()
                            .filter(o -> o != owner)
                            .filter(o -> o.getGameMode() == GameMode.ADVENTURE)
                            .sorted(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(owner.getLocation())))
                            .collect(Collectors.toList());
                    for (Player player : sortedPlayers) {
                        if (DamageUtils.isSameTeam(owner, player)) {
                            return List.of(BrigadierUtils.withEntity(context.getSource(), player));
                        }
                    }
                    return Collections.emptyList();
                });
        Brigadier.getRootNode().getChild("execute").getChild("on").addChild(teamArgument.build());
    }

    private void registerSecureRandom() {
        var secureRandom = LiteralArgumentBuilder.literal("securerandom").build();
        var rangeArgument = new IntegerRangeArgument("range");
        var selector = Brigadier.fromArgument(rangeArgument)
                .executes(context -> {
                    IntegerRange range = (IntegerRange) Brigadier.parseArguments(context, List.of(rangeArgument))[0];
                    return SECURE_RANDOM.nextInt(range.getLowerBound(), range.getUpperBound() + 1);
                });
        secureRandom.addChild(selector.build());
        Brigadier.getRootNode().addChild(secureRandom);
    }

    private void registerEnemyRelation() {
        var enemyArgument = LiteralArgumentBuilder.literal("enemy")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Player owner)) {
                        return Collections.emptyList();
                    }
                    List<Player> sortedPlayers = Bukkit.getOnlinePlayers().stream()
                            .filter(o -> o != owner)
                            .filter(o -> o.getGameMode() == GameMode.ADVENTURE)
                            .sorted(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(owner.getLocation())))
                            .collect(Collectors.toList());
                    for (Player player : sortedPlayers) {
                        if (!DamageUtils.isSameTeam(owner, player)) {
                            return List.of(BrigadierUtils.withEntity(context.getSource(), player));
                        }
                    }
                    return Collections.emptyList();
                });
        Brigadier.getRootNode().getChild("execute").getChild("on").addChild(enemyArgument.build());
    }

    private void registerShooterRelation() {
        var shooterArgument = LiteralArgumentBuilder.literal("shooter")
                .fork(Brigadier.getRootNode().getChild("execute"), context -> {
                    CommandSender sender = getNativeSender(context);
                    if (!(sender instanceof Projectile projectile)) {
                        return Collections.emptyList();
                    }
                    return projectile.getShooter() != null
                            ? List.of(Brigadier.getBrigadierSourceFromCommandSender(projectile.getShooter()))
                            : Collections.emptyList();
                });
        Brigadier.getRootNode().getChild("execute").getChild("on").addChild(shooterArgument.build());
    }

    private static CommandSender getNativeSender(CommandContext<?> context) {
        CommandSender sender = Brigadier.getCommandSenderFromContext(context);
        if (sender instanceof BukkitNativeProxyCommandSender bukkitNativeProxyCommandSender) {
            return bukkitNativeProxyCommandSender.getSource().getCallee();
        } else if (sender instanceof NativeProxyCommandSender nativeProxyCommandSender) {
            return nativeProxyCommandSender.getCallee();
        }
        return sender;
    }
}