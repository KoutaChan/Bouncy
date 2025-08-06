package me.koutachan.bouncy.ability.impl.special_thanks.unknown.type;

import me.koutachan.bouncy.game.GamePlayer;

public abstract class SkillSecret {
    protected final GamePlayer gamePlayer;
    protected final TriggerType activeType;

    protected boolean active;

    public SkillSecret(GamePlayer gamePlayer, TriggerType activeType) {
        this.gamePlayer = gamePlayer;
        this.activeType = activeType;
    }

    public void onActivated(TriggerMeta meta) {
        this.active = true;
    }

    public abstract void onGlobal(TriggerType type, TriggerMeta meta);

    public abstract String asMessage();

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public TriggerType getActiveType() {
        return activeType;
    }

    public boolean isActive() {
        return active;
    }
}