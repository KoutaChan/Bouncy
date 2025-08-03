package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedSecret extends SkillSecret {
    public SpeedSecret(GamePlayer gamePlayer, TriggerType type) {
        super(gamePlayer, type);
    }

    @Override
    public void onActivated(TriggerMeta meta) {
        switch (type) {
            case HIT, DAMAGE, JUMP_5, DROP_1, SHOOT -> gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 2, true, false));
            case KILL, JUMP_10, DROP_2, DRINK_POTION -> gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 2, true, false));
            case TICK -> gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1, 1, true, false));
        }
    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {

    }

    @Override
    public String asMessage() {
        return switch (type) {
            case HIT -> "ヒット時、移動速度が一時的に上がる";
            case KILL -> "敵を殺したとき、移動速度が一時的に上がる";
            case TICK -> "常に、移動速度が上がる";
            case DAMAGE -> "ダメージを受けたとき、移動速度が一時的に上がる";
            case SHOOT -> "矢を打ったとき、移動速度が一時的に上がる";
            case DRINK_POTION -> "ポーションを飲んだとき、移動速度が一時的に上がる";
            case DROP_1 -> "スキルを1回発動させたとき、移動速度が一時的に上がる";
            case DROP_2 -> "スキルを2回発動させたとき、移動速度が一時的に上がる";
            case JUMP_5 -> "ジャンプを5回したとき、移動速度が一時的に上がる";
            case JUMP_10 -> "ジャンプを10回したとき、移動速度が一時的に上がる";
        };
    }
}
