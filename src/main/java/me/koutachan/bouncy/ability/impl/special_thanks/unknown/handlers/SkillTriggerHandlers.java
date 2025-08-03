package me.koutachan.bouncy.ability.impl.special_thanks.unknown.handlers;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.impl.special_thanks.TrueArrowAbility;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret.*;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.game.GamePlayer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SkillTriggerHandlers {
    private final static List<Class<? extends SkillSecret>> secretsClasses = new ArrayList<>() {{
        add(ChargeSpeedSecret.class);
        add(GlowSecret.class);
        add(HealSecret.class);
        add(ProtectionSecret.class);
        add(RespawnSecret.class);
        add(ShockWaveSecret.class);
        add(SpeedSecret.class);
        add(TrueArrowSecret.class);
    }};

    private final GamePlayer gamePlayer;
    private final List<SkillSecret> secrets = new ArrayList<>();

    public SkillTriggerHandlers(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public void onTrigger(TriggerType type) {
        onTrigger(type, TriggerMeta.EMPTY);
    }

    public void onTrigger(TriggerType type, TriggerMeta meta) {
        for (SkillSecret secret : secrets) {
            if (secret.getType() == type) {
                secret.onActivated(meta);
            }
            secret.onGlobal(type, meta);
        }
    }

    public void newSecret() {
        Class<? extends SkillSecret> secretClass = secretsClasses.get(Bouncy.SECURE_RANDOM.nextInt(secretsClasses.size()));
        TriggerType type = TriggerType.VALUES[Bouncy.SECURE_RANDOM.nextInt(TriggerType.VALUES.length)];
        try {
            SkillSecret secret = secretClass.getConstructor(GamePlayer.class, TriggerType.class).newInstance(gamePlayer, type);
            secrets.add(secret);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public List<SkillSecret> getSecrets() {
        return secrets;
    }
}