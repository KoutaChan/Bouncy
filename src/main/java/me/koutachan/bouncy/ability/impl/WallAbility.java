package me.koutachan.bouncy.ability.impl;

import me.koutachan.bouncy.Bouncy;
import me.koutachan.bouncy.ability.Ability;
import me.koutachan.bouncy.ability.AbilityDrop;
import me.koutachan.bouncy.ability.AbilityShoot;
import me.koutachan.bouncy.game.GamePlayer;
import me.koutachan.bouncy.utils.FormatUtils;
import me.koutachan.bouncy.utils.ThroughWallArrowUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WallAbility extends Ability implements AbilityShoot, AbilityDrop {
    public final static int ID = 7;

    private final static int MAX_STOCK = 5;
    private final static char CHARGE_ICON = '■';

    private final static int GLASS_REMOVE_TICK = 20 * 10;

    private int stock;

    public WallAbility(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public void onTick() {
        if (gamePlayer.useAbility(getCt())) {
            if (++stock > MAX_STOCK) {
                stock = MAX_STOCK;
            }
        }
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof AbstractArrow abstractArrow) {
            boolean success = ThroughWallArrowUtils.trySpawnThroughWallArrow(abstractArrow);
            event.setCancelled(success);
        }
    }

    @Override
    public void onDrop() {
        if (stock > 0) {
            Location spawnPos = gamePlayer.getEyeLocation().add(gamePlayer.getEyeLocation().getDirection().multiply(2));
            new GlassWall(
                    spawnPos,
                    gamePlayer.getPlayer().getFacing()
            ).runTaskLater(Bouncy.INSTANCE, GLASS_REMOVE_TICK);
            gamePlayer.playSoundPublic(spawnPos, Sound.BLOCK_ANVIL_PLACE, 1f, 1f);
            stock--;
        }
    }

    @Override
    public String getName() {
        return "障壁";
    }

    @Override
    public String getActionBar() {
        String chargeStyle = ChatColor.BLUE + ChatColor.BOLD.toString() + String.valueOf(CHARGE_ICON).repeat(Math.max(0, stock)) +
                ChatColor.GRAY + ChatColor.BOLD + String.valueOf(CHARGE_ICON).repeat(Math.max(0, MAX_STOCK - stock));
        return "能力:障壁 " + FormatUtils.formatTick(Math.abs(getCt() - gamePlayer.getAbilityCt())) + "秒" + "（" + chargeStyle + ChatColor.WHITE + "）";
    }

    @Override
    public int getCt() {
        return 300;
    }

    @Override
    public int getId() {
        return ID;
    }

    public static class GlassWall extends BukkitRunnable {
        private final List<Block> placedBlocks;

        public GlassWall(Location blockPos, BlockFace blockFace) {
            this.placedBlocks = createWall(blockPos, blockFace, 1);
        }

        @Override
        public void run() {
            for (Block placed : placedBlocks) {
                placed.setType(Material.AIR);
            }
        }

        public List<Block> createWall(Location blockPos, BlockFace face, int radius) {
            List<Block> placed = new ArrayList<>();

            Vector direction = face.getDirection();
            Vector up = new Vector(0, 1, 0);
            Vector side = direction.clone().crossProduct(up).normalize();

            for (int h = -radius; h <= radius; h++) {
                Vector upOffset = up.clone().multiply(h);
                for (int w = -radius; w <= radius; w++) {
                    Vector sideOffset = side.clone().multiply(w);
                    Block block = blockPos.clone().add(upOffset).add(sideOffset).getBlock();
                    if (block.getType().isAir()) {
                        block.setType(Material.BLUE_STAINED_GLASS);
                        placed.add(block);
                    }
                }
            }
            return placed;
        }
    }
}