package me.koutachan.bouncy.ability.impl.special_thanks.unknown.secret;

import de.tr7zw.changeme.nbtapi.NBT;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.SkillSecret;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerMeta;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.TriggerType;
import me.koutachan.bouncy.ability.impl.special_thanks.unknown.type.meta.ShootMeta;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class RandomArrowSecret extends SkillSecret {
    public RandomArrowSecret(GamePlayer gamePlayer, TriggerType activeType) {
        super(gamePlayer, activeType);
        this.active = true;
    }

    @Override
    public void onActivated(TriggerMeta meta) {

    }

    @Override
    public void onGlobal(TriggerType type, TriggerMeta meta) {
        if (type != TriggerType.SHOOT) return;

        ProjectileLaunchEvent event = ((ShootMeta) meta).event();
        if (event.isCancelled()) return;

        ArrowType arrowType = getRandomArrowType();
        switch (arrowType) {
            case BOUNCE -> {
                int bouncyCount = gamePlayer.getPlayer().isSneaking() ? 8 : 4;
                NBT.modifyPersistentData(event.getEntity(), nbt -> {
                    nbt.setInteger("BouncyCount", bouncyCount);
                });
                event.getEntity().addScoreboardTag("bouncy");
            }
            case SPREAD -> NBT.modifyPersistentData(event.getEntity(), nbt -> {
                nbt.setBoolean("Spread", true);
            });
            case EXPLOSIVE -> NBT.modifyPersistentData(event.getEntity(), nbt -> {
                nbt.setBoolean("Explosive", true);
            });
        }
    }

    private ArrowType getRandomArrowType() {
        double random = Math.random() * ArrowType.totalWeight;
        double currentWeight = 0;

        for (ArrowType type : ArrowType.VALUES) {
            currentWeight += type.getWeight();
            if (random <= currentWeight) {
                return type;
            }
        }
        return ArrowType.NONE;
    }

    private enum ArrowType {
        NONE(20),
        BOUNCE(2),
        SPREAD(2),
        EXTRA_ARROW(2),
        EXPLOSIVE(1);

        private final double weight;
        public static final ArrowType[] VALUES = values();
        public static double totalWeight;

        static {
            for (ArrowType type : VALUES) {
                totalWeight += type.weight;
            }
        }

        ArrowType(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }
    }

    @Override
    public String asMessage() {
        return "確率で、打った矢はランダムな効果を持つ";
    }
}