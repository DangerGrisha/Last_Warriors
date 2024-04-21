package greg.pirat1c.humiliation.events.saske;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ChidoryListenerR implements Listener {

    private JavaPlugin plugin;

    public ChidoryListenerR(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Method to execute the Chidori ability
    private void executeChidori(Player player) {
        World world = player.getWorld();
        Location playerLocation = player.getLocation();
         org.bukkit.util.Vector forwardDirection = playerLocation.getDirection().normalize(); // Normalize to get a unit direction vector

        // Set a fixed distance for the forward dash (10 blocks)
        double dashDistance = 10;

        // Calculate the destination point where the player will be dashed to
        Location dashDestination = playerLocation.clone().add(forwardDirection.multiply(dashDistance));

        // Play thunder sound
        world.playSound(playerLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location teleportLocation = findEmptyLocation(playerLocation, forwardDirection);
                //org.bukkit.util.Vector direction = player.getLocation().getDirection();

                player.teleport(teleportLocation);

                applyDamageInPath(player, playerLocation, forwardDirection, dashDistance);
            }
        }.runTaskLater(plugin, 20L);
    }

    // Method to apply damage along the dash path
    private void applyDamageInPath(Player player, Location startLocation, org.bukkit.util.Vector forwardDirection, double dashDistance) {
        final int iterations = 20; // Number of points along the path to check
        final double segmentLength = dashDistance / iterations;

        for (int i = 0; i < iterations; i++) {
            // Calculate the current point's location along the dash path
            Location pointLocation = startLocation.clone().add(forwardDirection.clone().multiply(segmentLength * i));
            // Dynamically adjust the detection radius maybe needed here
            double searchRadius = 1.5; // Consider adjusting based on speed or elevation change
            for (Entity entity : pointLocation.getWorld().getNearbyEntities(pointLocation, searchRadius, searchRadius, searchRadius)) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(6.0, player); // Apply damage
                }
            }
        }
    }
    private Location findEmptyLocation(Location startLocation, Vector direction) {
        Location currentLocation = startLocation.clone();

        while (true) {

            if (!currentLocation.getWorld().getBlockAt(currentLocation).getType().isSolid()) {
                return currentLocation;
            }


            currentLocation.add(direction);


            if (currentLocation.getY() < 0 || currentLocation.getY() > currentLocation.getWorld().getMaxHeight()) {
                return null; // Не удалось найти пустое место
            }
        }
    }

    // Check if the player has triggered the Chidori ability
    private boolean checkEvent(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Chidori");
    }

    // Listen for player interactions
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the Chidori ability should be executed
        if (checkEvent(event, player)) {
            executeChidori(player);
        }
    }
}
