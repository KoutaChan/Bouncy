package me.koutachan.bouncy.ability.impl.special_thanks.unknown.type;

import me.koutachan.bouncy.game.GamePlayer;

public abstract class SkillSecret {
    protected final GamePlayer gamePlayer;
    protected final TriggerType type;

    protected boolean active;

    public SkillSecret(GamePlayer gamePlayer, TriggerType type) {
        this.gamePlayer = gamePlayer;
        this.type = type;
    }

    public void onActivated(TriggerMeta meta) {
        this.active = true;
    }

    public abstract void onGlobal(TriggerType type, TriggerMeta meta);

    public abstract String asMessage();

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public TriggerType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }
}