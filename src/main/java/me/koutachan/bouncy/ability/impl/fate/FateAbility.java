package me.koutachan.bouncy.ability.impl.fate;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.impl.fate.handler.*;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

public class FateAbility extends Ability {
    public final static int ID = 36;

    private final static List<FateHandler> handlers = new ArrayList<>();
    private static int totalWeight;

    private final static FateAbilityChange ABILITY_CHANGE = register(new FateAbilityChange());
    private final static FateArmor ARMOR = register(new FateArmor());
    private final static FateArrow ARROW = register(new FateArrow());
    private final static FateBattleAxe BATTLE_AXE = register(new FateBattleAxe());
    private final static FateCrossbow CROSSBOW = register(new FateCrossbow());
    private final static FateHeal HEAL = register(new FateHeal());
    private final static FateHealthBoost HEALTH_BOOST = register(new FateHealthBoost());
    private final static FatePain PAIN = register(new FatePain());
    private final static FateTotem TOTEM = register(new FateTotem());

    private final static int FATE_SHOW_DELAY = 100;

    public FateAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (gamePlayer.useAbility(getCt())) {
            randomFate();
            gamePlayer.getWorld().playSound(gamePlayer.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        }
    }

    public void randomFate() {
        int random = Bouncy.SECURE_RANDOM.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (FateHandler handler : handlers) {
            int weight = handler.getWeight();
            cumulativeWeight += weight;

            if (random < cumulativeWeight) {
                int relativeWeight = random - (cumulativeWeight - weight) + 1;

                triggerFate(handler, relativeWeight);
                break;
            }
        }
    }

    public void triggerFate(FateHandler handler) {
        triggerFate(handler, handler.getSuccessfulWeight() == handler.getWeight() ? handler.getSuccessfulWeight() : Bouncy.SECURE_RANDOM.nextInt(1, handler.getWeight()));
    }

    public void triggerFate(FateHandler handler, int relativeWeight) {
        boolean success = relativeWeight <= handler.getSuccessfulWeight();

        handler.onChosen(gamePlayer, relativeWeight, success);
        Bukkit.getScheduler().runTaskLater(Bouncy.INSTANCE, () -> handler.onFate(gamePlayer, relativeWeight, success), FATE_SHOW_DELAY);
    }

    public static <T extends FateHandler> T register(T handler) {
        handlers.add(handler);
        totalWeight += handler.getWeight();
        return handler;
    }

    @Override
    public String getName() {
        return "運命";
    }

    @Override
    public String getActionBar() {
        return "能力:運命 " + FormatUtils.formatTick(gamePlayer.getAbilityCt()) + "秒" + "（" + FormatUtils.formatTick(getCt()) + "秒で自動発動）";
    }

    @Override
    public int getCt() {
        return 400;
    }

    @Override
    public int getId() {
        return ID;
    }
}