package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.attribute.Attribute;

public class HealSecret extends SkillSecret {
    public HealSecret(GamePlayer gamePlayer, TriggerType activeType) {
        super(gamePlayer, activeType);
    }

    @Override
    public void onActivated(TriggerMeta meta) {

    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {
        if (type == TriggerType.DRINK_POTION) {
            double max = gamePlayer.getPlayer().getAttribute(Attribute.MAX_HEALTH).getValue();
            gamePlayer.getPlayer().setHealth(Math.min(max, gamePlayer.getPlayer().getHealth() + 2));
        }
    }

    @Override
    public String asMessage() {
        return "ポーションを飲んだとき、更に回復する";
    }
}