package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.game.GamePlayer;

public class ChargeSpeedSecret extends SkillSecret {
    public ChargeSpeedSecret(GamePlayer gamePlayer, TriggerType activeType) {
        super(gamePlayer, activeType);
    }

    @Override
    public void onActivated(TriggerMeta meta) {
        float progressLevel = switch (activeType) {
            case TICK -> 0.005f;
            case HIT -> 0.3f;
            case KILL, DRINK_POTION, DROP_2, DROP_1 -> 1f;
            case SHOOT, JUMP_5 -> 0.1f;
            case DAMAGE, JUMP_10 -> 0.2f;
        };
        gamePlayer.getPlayer().setExp(Math.min(0.99F, gamePlayer.getPlayer().getExp() + progressLevel));
    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {
    }

    @Override
    public String asMessage() {
        return switch (activeType) {
            case HIT -> "ヒット時、矢のチャージを加速させる";
            case KILL -> "敵を殺したとき、矢のチャージを加速させる";
            case TICK -> "常に、矢のチャージを加速させる";
            case DAMAGE -> "ダメージを受けたとき、矢のチャージを加速させる";
            case SHOOT -> "矢を打ったとき、矢のチャージを加速させる";
            case DRINK_POTION -> "ポーションを飲んだとき、矢のチャージを加速させる";
            case DROP_1 -> "スキルを1回発動させたとき、矢のチャージを加速させる";
            case DROP_2 -> "スキルを2回発動させたとき、矢のチャージを加速させる";
            case JUMP_5 -> "ジャンプを5回したとき、矢のチャージを加速させる";
            case JUMP_10 -> "ジャンプを10回したとき、矢のチャージを加速させる";
        };
    }
}
