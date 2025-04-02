package me.koutachan.bouncy.ability.gamble;

import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.utils.AbilityUtils;
import me.koutachan.bouncy.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum GambleDeBuff implements GambleInfo {
    DECREASE_SPEED(ItemCreator.of(Material.SUGAR)
            .setDisplayName("&4裏")
            .addLore("&c移動速度が低下する")
            .create(),
            player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, true, false))
    ),
    CANNOT_USE_HEAL_POTION(ItemCreator.of(Material.BARRIER)
            .setDisplayName("&4裏")
            .addLore("&c一時的に回復ポーションが使えなくなる（60秒）")
            .create(),
            player -> GameManager.getGamePlayerOrCreate(player).runPotionDeBuff()
    ),
    DECREASE_HP(ItemCreator.of(Material.POISONOUS_POTATO)
            .setDisplayName("&4裏")
            .addLore("&cHPが減る（1♥）")
            .create(),
            player -> player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(Math.max(1, player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2))
    ),
    COMBUSTION(ItemCreator.of(Material.FLINT_AND_STEEL)
            .setDisplayName("&4裏")
            .addLore("&c炎上する")
            .create(),
            player -> player.setFireTicks(80)
    ),
    EXTRA_DAMAGE(ItemCreator.of(Material.FLINT_AND_STEEL)
            .setDisplayName("&4裏")
            .addLore("&cあなたがダメージを受けたら0.5ダメージ追加で食らうようになる（重複はしない）")
            .create(),
            player -> { /* Empty */ }
    ),
    DEATH(ItemCreator.of(Material.SKELETON_SKULL)
            .setDisplayName("&4裏")
            .addLore("&c死ぬ")
            .create(),
            player -> player.setHealth(0)
    ),
    NO_ARROW_CHARGE(ItemCreator.of(Material.EXPERIENCE_BOTTLE)
            .setDisplayName("&4裏")
            .addLore("&c矢のチャージが溜まらなくなる（30秒）")
            .create(),
            player -> GameManager.getGamePlayerOrCreate(player).runNoExpPoint()
    ),
    NO_JUMP(ItemCreator.of(Material.RABBIT_FOOT)
            .setDisplayName("&4裏")
            .addLore("&cジャンプできなくなる")
            .create(),
            player -> { /* Empty */ }
    ),
    DARKNESS(ItemCreator.of(Material.INK_SAC)
            .setDisplayName("&4裏")
            .addLore("&c暗闇になる")
            .create(),
            player -> player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 1, true, false))
    ),
    CLEAR_EFFECT(ItemCreator.of(Material.MILK_BUCKET)
            .setDisplayName("&4裏")
            .addLore("&eすべてのエフェクトを解除する")
            .create(),
            player -> {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
            }
    ),
    /*HORSE_MASTER(ItemCreator.of(Material.DIAMOND_HORSE_ARMOR)
            .setDisplayName("&4裏")
            .addLore("&c馬から降りれなくなる")
            .create(),
            AbilityUtils::randomAbility
    ),*/ // Todo: temporary removed
    RANDOM_ABILITY(ItemCreator.of(Material.ARMOR_STAND)
            .setDisplayName("&4裏")
            .addLore("&e能力が変わる（ギャンブルは発動できなくなる）")
            .create(),
            player -> {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().getOrDefault(Gambler.GAMBLE_NAMESPACED_KEY, PersistentDataType.BOOLEAN, false)) {
                        player.getInventory().remove(item);
                    }
                }
                AbilityUtils.randomAbility(player);
            }
    ),
    RANDOM_ROTATION(ItemCreator.of(Material.STRING)
            .setDisplayName("&4裏")
            .addLore("&c10秒ごとに視点がランダムな方向に変わる")
            .create(),
            player -> GameManager.getGamePlayerOrCreate(player).runRandomRotationDeBuff()
    ),
    NONE(ItemCreator.of(Material.NETHER_STAR)
            .setDisplayName("&4裏")
            .addLore("&a何も起きない")
            .create(),
            player -> { /* Empty */ }
    );

    public final static GambleDeBuff[] VALUES = values();

    private final ItemStack description;
    private final GambleConsumer gambleConsumer;

    GambleDeBuff(ItemStack description, GambleConsumer gambleConsumer) {
        this.description = description;
        this.gambleConsumer = gambleConsumer;
    }

    @Override
    public ItemStack getDescription() {
        return description;
    }

    @Override
    public GambleConsumer getGambleConsumer() {
        return gambleConsumer;
    }

    @Override
    public GambleBuffType getType() {
        return GambleBuffType.DEBUFF;
    }
}