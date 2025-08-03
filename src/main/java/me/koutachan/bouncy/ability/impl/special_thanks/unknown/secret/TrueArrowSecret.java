package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.ShootMeta;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.ThroughWallArrowUtils;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class TrueArrowSecret extends SkillSecret {
    public TrueArrowSecret(GamePlayer gamePlayer, TriggerType type) {
        super(gamePlayer, type);
    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {
        if (type == TriggerType.SHOOT && active) {
            ProjectileLaunchEvent event = ((ShootMeta) meta).event();
            if (event.getEntity() instanceof AbstractArrow abstractArrow) {
                boolean success = ThroughWallArrowUtils.trySpawnThroughWallArrow(abstractArrow, ThroughWallArrowUtils.ThroughMode.ALWAYS) != null;
                if (success) {
                    event.setCancelled(true);
                    active = false;
                }
            }
        }
    }

    @Override
    public String asMessage() {
        return switch (type) {
            case HIT -> "ヒット時、次の矢を貫通させる";
            case KILL -> "敵を殺したとき、次の矢を貫通させる";
            case TICK -> "常に、矢が貫通する";
            case DAMAGE -> "ダメージを受けたとき、次の矢を貫通させる";
            case SHOOT -> "矢を打ったとき、矢を貫通させる";
            case DRINK_POTION -> "ポーションを飲んだとき、次の矢を貫通させる";
            case DROP_1 -> "スキルを1回発動させたとき、次の矢を貫通させる";
            case DROP_2 -> "スキルを2回発動させたとき、次の矢を貫通させる";
            case JUMP_5 -> "ジャンプを5回したとき、次の矢を貫通させる";
            case JUMP_10 -> "ジャンプを10回したとき、次の矢を貫通させる";
        };
    }
}