package fr.anarchick.frc.overworld;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class Listeners implements Listener {

    public static boolean isOverWorld(final World world) {
        return world.getName().equals("world");
    }

    private static final BlockFace[] FACES = new BlockFace[] {
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private boolean isVeinOre(final Block block) {
        for (final BlockFace face : FACES) {
            final Block relative = block.getRelative(face);

            if (MaterialTags.ORES.isTagged(relative)) {
                return true;
            }

        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent event) {
        if (!isOverWorld(event.getBlock().getWorld())) return;

        final Block block = event.getBlock();
        final Material material = block.getType();

        if (material == Material.COBBLESTONE || material == Material.COBBLED_DEEPSLATE) {
            event.setCancelled(true);
            return;
        }

        if (MaterialTags.ORES.isTagged(material)) {

            // Allow the player to mine more Ores if they are in a vein
            if (isVeinOre(block)) {
                return;
            }

            final Material newType;

            if (MaterialTags.DEEPSLATE_ORES.isTagged(material)) {
                newType = Material.COBBLED_DEEPSLATE;
            } else {
                newType = Material.COBBLESTONE;
            }

            block.setType(newType);

        }

    }
}
