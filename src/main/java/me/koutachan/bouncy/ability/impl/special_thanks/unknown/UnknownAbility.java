package me.koutachan.bouncy.ability.impl.special_thanks.unknown;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.*;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.handlers.SkillTriggerHandlers;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.DamageMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.HitMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.KillMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.ShootMeta;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.DamageUtils;
import me.koutachan.bouncy.utils.FormatUtils;
import me.koutachan.bouncy.utils.JumpUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class UnknownAbility extends Ability implements AbilityDrop, AbilityKill, AbilityShoot, AbilityAttack, AbilityPotion, AbilityDamage {
    private final static int MAX_CT = 500;
    private final static int MIN_CT = 200;

    private int ct;

    public final static int ID = 998;

    private final SkillTriggerHandlers handlers;
    private int dropStock;
    private int jumpStock;
    private Double lastY;

    public UnknownAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
        this.handlers = new SkillTriggerHandlers(gamePlayer);
        for (int i = 0; i < 5; i++) {
            handlers.newSecret();
        }
        createCt();
    }

    @Override
    public void onTick() {
        handlers.onTrigger(TriggerType.TICK);

        double y = gamePlayer.getLocation().getY();
        if (lastY != null) {
            double deltaY = y - lastY;
            double jumpPower = JumpUtils.getJumpPower(gamePlayer.getPlayer());
            if (JumpUtils.isJump(deltaY, jumpPower)) {
                jumpStock++;
                if (jumpStock % 5 == 0)
                    handlers.onTrigger(TriggerType.JUMP_5);
                if (jumpStock % 10 == 0)
                    handlers.onTrigger(TriggerType.JUMP_10);
            }
        }
        lastY = y;

        if (gamePlayer.getAbilityCt() == Integer.MAX_VALUE) {
            gamePlayer.sendMessage(ChatColor.GREEN + "= = This is Secret Message = =");
            for (SkillSecret secret : handlers.getSecrets()) {
                gamePlayer.sendMessage(secret.asMessage());
            }
            gamePlayer.sendMessage(ChatColor.GREEN + "= = This is Secret Message = =");
        }

        gamePlayer.limitCt(getCt());
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        handlers.onTrigger(TriggerType.SHOOT, new ShootMeta(event));
    }

    @Override
    public void onAttack(Player victim) {
        if (!DamageUtils.isSameTeam(victim, gamePlayer.getPlayer())) {
            handlers.onTrigger(TriggerType.HIT, new HitMeta(victim));
        }
    }

    @Override
    public void onDamage(EntityDamageEvent event) {
        handlers.onTrigger(TriggerType.DAMAGE, new DamageMeta(event));
    }

    @Override
    public void onKill(Player victim) {
        handlers.onTrigger(TriggerType.KILL, new KillMeta(victim));
    }

    @Override
    public void onPotion(PlayerItemConsumeEvent event) {
        handlers.onTrigger(TriggerType.DRINK_POTION);
    }

    @Override
    public void onDrop() {
        if (gamePlayer.useAbility(getCt())) {
            handlers.onTrigger(TriggerType.DROP_1);
            if (dropStock++ % 2 == 0) {
                handlers.onTrigger(TriggerType.DROP_2);
            }
            createCt();
        }
    }

    public void createCt() {
        this.ct = Bouncy.SECURE_RANDOM.nextInt(MIN_CT, MAX_CT);
    }

    @Override
    public String getName() {
        return "不明";
    }

    @Override
    public String getActionBar() {
        return "能力:不明 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で使用可能） ";
    }

    @Override
    public int getCt() {
        return ct;
    }

    @Override
    public int getId() {
        return ID;
    }
}