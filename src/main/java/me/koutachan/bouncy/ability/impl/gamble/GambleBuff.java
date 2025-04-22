package me.koutachan.bouncy.ability.impl.gamble;

import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.utils.AbilityUtils;
import me.koutachan.bouncy.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum GambleBuff implements GambleInfo {
    HEAL_ALL(
            ItemCreator.of(Material.ENCHANTED_GOLDEN_APPLE)
                    .setDisplayName("&a表")
                    .addLore("&cHPが全回復する")
                    .create(),
            player -> player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue())
    ),
    ALL_ARROW_SPREAD(
            ItemCreator.of(Material.CROSSBOW)
                    .setDisplayName("&a表")
                    .addLore("&a発射したすべての矢は拡散する")
                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .create(),
            player -> { /* Empty */ }
    ),
    ALL_ARROW_BOUNCE(
            ItemCreator.of(Material.SLIME_BALL)
                    .setDisplayName("&a表")
                    .addLore("&a発射したすべての矢は跳弾する")
                    .create(),
            player -> { /* Empty */ }
    ),
    GIVE_TOTEM(
            ItemCreator.of(Material.TOTEM_OF_UNDYING)
                    .setDisplayName("&a表")
                    .addLore("&6トーテムがもらえる")
                    .create(),
            player -> player.getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING))
    ),
    INCREASE_SPEED_AND_JUMP(
            ItemCreator.of(Material.SUGAR)
                    .setDisplayName("&a表")
                    .addLore("&b移動速度と跳躍力が上昇する")
                    .create(),
            player -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1, true, false));
            }
    ),
    GIVE_TWO_HEAL_POTION(
            ItemCreator.of(Material.DRAGON_BREATH)
                    .setDisplayName("&a表")
                    .addLore("&c回復ポーションが2個もらえる")
                    .create(),
            player -> AbilityUtils.giveHealPotion(player, 2)
    ),
    /*INCREASE_ARROW_AND_REGENERATION(
            ItemCreator.of(Material.ARROW)
                    .setDisplayName("&a表")
                    .addLore("&e矢のチャージが早くなり、矢の最大数が3個まで増える")
                    .create(),
            player -> GameManager.getGamePlayerOrCreate(player).runHealArrow()
    ),*/
    CHANGE_TO_YAMINABE(
            ItemCreator.of(Material.CAULDRON)
                    .setDisplayName("&a表")
                    .addLore("&b役職が闇鍋になる（賭博の場合能力維持）")
                    .create(),
            player -> AbilityUtils.changeAbility(player, 29)
    ),
    INVINCIBLE(
            ItemCreator.of(Material.NETHERITE_CHESTPLATE)
                    .setDisplayName("&a表")
                    .addLore("&e一時的に無敵になる（10秒）")
                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .create(),
            player -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 10, 22, true, false))
    ),
    INVISIBILITY(
            ItemCreator.of(Material.GHAST_TEAR)
                    .setDisplayName("&a表")
                    .addLore("&e一時的に透明になる（30秒）")
                    .create(),
            player -> player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 30, 0, true, false))
    ),
    INCREASE_DAMAGE(
            ItemCreator.of(Material.NETHERITE_SWORD)
                    .setDisplayName("&a表")
                    .addLore("&c矢のダメージを追加で0.5ダメージ増やす（重複はしない）")
                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .create(),
            player -> { /* Empty */ }
    ),
    HUNCH_ENEMY(
            ItemCreator.of(Material.GLOWSTONE_DUST)
                    .setDisplayName("&a表")
                    .addLore("&e最寄りの敵の位置がわかるようになる")
                    .create(),
            player -> GameManager.getGamePlayerOrCreate(player).runHunch()
    );

    public final static GambleBuff[] VALUES = values();

    private final ItemStack description;
    private final GambleConsumer gambleConsumer;

    GambleBuff(ItemStack description, GambleConsumer gambleConsumer) {
        this.description = description;
        this.gambleConsumer = gambleConsumer;
    }

    @Override
    public GambleConsumer getGambleConsumer() {
        return gambleConsumer;
    }

    @Override
    public ItemStack getDescription() {
        return description;
    }

    @Override
    public GambleBuffType getType() {
        return GambleBuffType.BUFF;
    }
}
