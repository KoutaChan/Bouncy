package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ProtectionSecret extends SkillSecret {
    public final static PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.RESISTANCE, 1, 1, true, false);

    public ProtectionSecret(GamePlayer gamePlayer, TriggerType activeType) {
        super(gamePlayer, activeType);
    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {
        switch (type) {
            case TICK -> {
                if (active) {
                    gamePlayer.addPotionEffect(RESISTANCE);
                }
            }
            case DAMAGE -> {
                if (this.activeType != TriggerType.DAMAGE) {
                    this.active = false;
                }
            }
        }
    }

    @Override
    public String asMessage() {
        return switch (activeType) {
            case HIT -> "ヒット時、次のダメージを半減させる";
            case KILL -> "敵を殺したとき、次のダメージを半減させる";
            case TICK -> "常に、ダメージを半減する";
            case DAMAGE -> "ダメージを受けたとき、次のダメージを半減させる";
            case SHOOT -> "矢を打ったとき、次のダメージを半減させる";
            case DRINK_POTION -> "ポーションを飲んだとき、次のダメージを半減させる";
            case DROP_1 -> "スキルを1回発動させたとき、次のダメージを半減させる";
            case DROP_2 -> "スキルを2回発動させたとき、次のダメージを半減させる";
            case JUMP_5 -> "ジャンプを5回したとき、次のダメージを半減させる";
            case JUMP_10 -> "ジャンプを10回したとき、次のダメージを半減させる";
        };
    }
}