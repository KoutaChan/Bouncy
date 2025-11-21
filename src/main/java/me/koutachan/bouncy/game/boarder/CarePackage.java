package me.koutachan.bouncy.game.boarder;

import fr.skytasul.glowingentities.GlowingTeam;
import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.game.GameManager;
import me.koutachan.bouncy.game.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Interaction;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CarePackage {
    public final static double HEAD_OFFSET = 1.43;
    public final static double VEC_OFFSET = 1;

    private final WorldBorder worldBorder;
    public Map<UUID, CarePackageData> carePackages = new HashMap<>();

    public CarePackage(WorldBorder worldBorder) {
        this.worldBorder = worldBorder;
    }

    public void onTick() {
        for (CarePackageData carePackageData : carePackages.values()) {
            carePackageData.onTick();
        }
    }

    public void summon() {
        Location spawnPos = this.worldBorder.getRandomPosForCarePackage();
        spawnPos.setY(230);
        CarePackageData carePackageData = CarePackageData.create(spawnPos);
        carePackages.put(carePackageData.getInteraction().getUniqueId(), carePackageData);
    }

    public void killAll() {
        for (CarePackageData carePackageData : carePackages.values()) {
            carePackageData.killSelf();
        }
        carePackages.clear();
    }

    public CarePackageData getCarePackage(UUID uuid) {
        return carePackages.get(uuid);
    }

    public Map<UUID, CarePackageData> getCarePackages() {
        return carePackages;
    }

    public static class CarePackageData {
        private int left;
        private final ArmorStand armorStand;
        private final Interaction interaction;
        private final List<UUID> collectedUsers = new ArrayList<>();

        public CarePackageData(int left, ArmorStand armorStand, Interaction interaction) {
            this.left = left;
            this.armorStand = armorStand;
            this.interaction = interaction;
        }

        public int getLeft() {
            return left;
        }

        public Interaction getInteraction() {
            return interaction;
        }

        public ArmorStand getArmorStand() {
            return armorStand;
        }

        public void onTick() {
            if (!armorStand.getLocation().add(0, VEC_OFFSET, 0).getBlock().getType().isSolid()) {
                armorStand.teleport(armorStand.getLocation().subtract(0, 0.12, 0));
                interaction.teleport(interaction.getLocation().subtract(0, 0.12, 0));
            }
            for (GamePlayer gamePlayer : GameManager.CURRENT_GAME_PLAYER.values()) {
                if (gamePlayer.getTaskHandler().isPaused())
                    continue;

                ChatColor color = collectedUsers.contains(gamePlayer.getUUID())
                        ? ChatColor.WHITE
                        : ChatColor.YELLOW;
                try {
                    Bouncy.GLOW_API.unsetGlowing(armorStand, gamePlayer.getPlayer());
                    Bouncy.GLOW_API.setGlowing(armorStand, gamePlayer.getPlayer(), new GlowingTeam(color));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void collect(GamePlayer gamePlayer) {
            if (collectedUsers.contains(gamePlayer.getUUID())) {
                gamePlayer.sendMessage("&c&l[!] すでにこのケアパッケージは獲得済みです！");
                return;
            }
            gamePlayer.addHealPotion();
            collectedUsers.add(gamePlayer.getUUID());
            left--;
            gamePlayer.sendMessage("&a&l[!] 回復のポーションを獲得した");
            if (isRemoved()) {
                killSelf();
            }
        }

        public static CarePackageData create(Location location) {
            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, a -> {
                a.setPersistent(false);
                a.setInvulnerable(true);
                a.setInvisible(true);
                a.setSilent(true);
                a.setBasePlate(false);
                a.setMarker(true);
                a.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
                a.getEquipment().setHelmet(new ItemStack(Material.CHEST));
                a.addScoreboardTag("care_package");
            });

            Interaction interaction = location.getWorld().spawn(armorStand.getLocation().add(0, HEAD_OFFSET, 0), Interaction.class, i -> {
                i.setResponsive(true);
                i.setInteractionWidth(0.6f);
                i.setInteractionHeight(0.6f);
                i.setGravity(false);
                i.setInvulnerable(true);
                i.setSilent(true);
                i.setPersistent(false);
                i.addScoreboardTag("care_package");
            });
            return new CarePackageData(3, armorStand, interaction);
        }

        public boolean isRemoved() {
            return 0 >= left;
        }

        public void killSelf() {
            armorStand.remove();
            interaction.remove();
        }
    }
}