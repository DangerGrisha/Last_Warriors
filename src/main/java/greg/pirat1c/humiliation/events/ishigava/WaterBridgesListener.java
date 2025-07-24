package greg.pirat1c.humiliation.events.ishigava;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import static greg.pirat1c.humiliation.events.ishigava.IshigavaConstants.*;

import java.util.List;

public class WaterBridgesListener implements Listener {

    private JavaPlugin plugin;

    public WaterBridgesListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.RED_DYE && item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(NAME_OF_ISHIGAVA_BRIDGE) &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            int distanceMultiplier = player.getLevel(); // Get XP level as distance multiplier
            //int baseDistance = 5; // Base distance
            int totalDistance = distanceMultiplier; // Final distance for bridge creation

            Vector direction = player.getEyeLocation().getDirection();
            Location spawnLocation = player.getLocation().add(direction.multiply(totalDistance)); // Use final distance

            if (!isNearPlayersOrBridges(spawnLocation, player, 3)) {
                spawnBridge(spawnLocation, player);
            } else {
                player.sendMessage("Cannot spawn a bridge here");
            }
        }
    }

    private void spawnBridge(Location location, Player player) {
        World world = player.getWorld();
        ArmorStand armorStand = world.spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setMetadata("bridge", new FixedMetadataValue(plugin, true));
        });

        // Create a platform around the ArmorStand, replacing only air blocks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLocation = location.clone().add(x, 1, z); // Floor one level below ArmorStand
                if (blockLocation.getBlock().getType() == Material.AIR) { // Check if the block is air
                    blockLocation.getBlock().setType(Material.LAPIS_BLOCK);
                }
            }
        }

        // Schedule bridge removal after 20 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location blockLocation = location.clone().add(x, 1, z);
                    if (blockLocation.getBlock().getType() == Material.LAPIS_BLOCK) { // Check before removal
                        blockLocation.getBlock().setType(Material.AIR);
                    }
                }
            }
            armorStand.remove();
        }, 400L); // 400 ticks = 20 seconds
    }

    private boolean isNearPlayersOrBridges(Location location, Player placer, int radius) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(placer)) {
                return true; // Found another player (not the placer) within the radius
            }
            if (entity instanceof ArmorStand && entity.hasMetadata("bridge")) {
                return true; // Found another bridge within the radius
            }
        }
        return false; // No players or bridges are too close
    }
}
