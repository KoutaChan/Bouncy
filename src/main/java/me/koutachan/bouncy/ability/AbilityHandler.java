package me.koutachan.bouncy.ability;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.*;
import me.koutachan.bouncy.ability.impl.assassin.AssassinAbility;
import me.koutachan.bouncy.ability.impl.fate.FateAbility;
import me.koutachan.bouncy.ability.impl.special_thanks.TrueArrowAbility;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.UnknownAbility;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.game.task.GameTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AbilityHandler extends GameTask {
    private final static Map<Integer, Class<? extends Ability>> ABILITY_MAPPINGS = new HashMap<>();
    static {
        try {
            ABILITY_MAPPINGS.put(GravityAbility.ID, GravityAbility.class);
            ABILITY_MAPPINGS.put(InvisibilityAbility.ID, InvisibilityAbility.class);
            ABILITY_MAPPINGS.put(ExplosiveAbility.ID, ExplosiveAbility.class);
            ABILITY_MAPPINGS.put(ConfusionAbility.ID, ConfusionAbility.class);
            ABILITY_MAPPINGS.put(LeapAbility.ID, LeapAbility.class);
            ABILITY_MAPPINGS.put(TeleportAbility.ID, TeleportAbility.class);
            ABILITY_MAPPINGS.put(RegenerationAbility.ID, RegenerationAbility.class);
            ABILITY_MAPPINGS.put(ReturnAbility.ID, ReturnAbility.class);
            ABILITY_MAPPINGS.put(InvincibleAbility.ID, InvincibleAbility.class);
            ABILITY_MAPPINGS.put(BouncyAbility.ID, BouncyAbility.class);
            ABILITY_MAPPINGS.put(SpreadAbility.ID, SpreadAbility.class);
            ABILITY_MAPPINGS.put(LightBeamAbility.ID, LightBeamAbility.class);
            ABILITY_MAPPINGS.put(ToughAbility.ID, ToughAbility.class);
            ABILITY_MAPPINGS.put(ExciteAbility.ID, ExciteAbility.class);
            ABILITY_MAPPINGS.put(CreateArrowAbility.ID, CreateArrowAbility.class);
            ABILITY_MAPPINGS.put(RestraintAbility.ID, RestraintAbility.class);
            ABILITY_MAPPINGS.put(TrackingArrowAbility.ID, TrackingArrowAbility.class);
            ABILITY_MAPPINGS.put(BufferAbility.ID, BufferAbility.class);
            ABILITY_MAPPINGS.put(FlyAbility.ID, FlyAbility.class);
            ABILITY_MAPPINGS.put(SpeedAbility.ID, SpeedAbility.class);
            ABILITY_MAPPINGS.put(WallAbility.ID, WallAbility.class);
            ABILITY_MAPPINGS.put(FateAbility.ID, FateAbility.class);
            ABILITY_MAPPINGS.put(AssassinAbility.ID, AssassinAbility.class);
            ABILITY_MAPPINGS.put(LightningStrikeAbility.ID, LightningStrikeAbility.class);

            /* SPECIAL THANKS */
            ABILITY_MAPPINGS.put(UnknownAbility.ID, UnknownAbility.class);
            ABILITY_MAPPINGS.put(TrueArrowAbility.ID, TrueArrowAbility.class);
        } catch (Throwable throwable) {
            Bouncy.INSTANCE.getLogger().log(Level.WARNING, "Failed to register Ability", throwable);
        }
    }

    private Ability lastAbility;
    private final Map<Integer, Ability> abilityCache = new HashMap<>();

    public AbilityHandler(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void run() {
        var ability = getAbility();
        if (ability != null) {
            ability.onTick();
            gamePlayer.getPlayer().spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(ability.getActionBar())
            );
        }

        if (lastAbility != null && lastAbility != ability) {
            lastAbility.onAbilityChange(ability);
        }

        lastAbility = ability;
    }

    @Nullable
    public Ability getAbility() {
        return getAbilityCacheOrCreate(gamePlayer.getAbilityId());
    }

    public void dispose() {
        abilityCache.forEach((id, ability) -> ability.onDispose());
    }

    @Override
    public void start() {
        runTaskTimer(Bouncy.INSTANCE, 0, 1);
    }

    @Override
    public void onStop() {
        dispose();
    }

    @Nullable
    public Ability getAbilityCacheOrCreate(int abilityId) {
        return abilityCache.computeIfAbsent(abilityId, id -> {
            Class<? extends Ability> abilityClass = ABILITY_MAPPINGS.get(id);
            if (abilityClass == null)
                return null;

            try {
                return abilityClass.getConstructor(GamePlayer.class).newInstance(gamePlayer);
            } catch (Throwable e) {
                Bouncy.INSTANCE.getLogger().log(Level.WARNING, "Failed to create Ability", e);
                throw new RuntimeException(e);
            }
        });
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
}