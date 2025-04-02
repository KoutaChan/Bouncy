package me.koutachan.bouncy.ability;

import me.koutachan.bouncy.game.GamePlayer;
import org.jetbrains.annotations.Nullable;

public abstract class Ability {
    protected final GamePlayer gamePlayer;

    public Ability(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public abstract void onTick();

    public abstract String getName();

    public abstract String getActionBar();

    /**
     * プレイヤーのアビリティが変更されたときに呼び出されるメソッドです
     *
     * @param to 変更後のアビリティ、nullの場合は無効なアビリティ
     */
    public void onAbilityChange(@Nullable Ability to) {

    }

    public void onDispose() {

    }

    public abstract int getCt();

    public abstract int getId();

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
}