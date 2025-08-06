package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.DamageMeta;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageEvent;

public class RespawnSecret extends SkillSecret {
    public RespawnSecret(GamePlayer gamePlayer, TriggerType activeType) {
        super(gamePlayer, activeType);
        this.active = true;
    }

    @Override
    public void onActivated(TriggerMeta meta) {

    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {
        if (!active || type != TriggerType.DAMAGE) return;

        EntityDamageEvent event = ((DamageMeta) meta).event();
        if (event.isCancelled()) return;

        double finalizedDamage = event.getFinalDamage();
        double projectedHealth = gamePlayer.getPlayer().getHealth() - finalizedDamage;
        if (projectedHealth <= 0.0) {
            event.setCancelled(true);
            gamePlayer.getPlayer().setHealth(gamePlayer.getPlayer().getAttribute(Attribute.MAX_HEALTH).getValue());
            gamePlayer.getPlayer().playEffect(EntityEffect.TOTEM_RESURRECT);
            gamePlayer.sendMessage(ChatColor.GOLD + "✦ 神の奇跡が君を蘇らせた！ まだ終わらんぞ！");
            active = false;
        }
    }

    @Override
    public String asMessage() {
        return "一度だけ致死ダメージを無効化し、全回復して復活する";
    }
}