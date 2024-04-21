package greg.pirat1c.humiliation.events.saske;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;

public class ChidoryListener implements Listener {

    private JavaPlugin plugin;

    public ChidoryListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (checkEvent(event, player)) {
            World world = player.getWorld();
            Location initialLocation = player.getLocation();
            Vector direction = initialLocation.getDirection();

            // Play thunder sound for 3 seconds
            world.playSound(initialLocation, "saske.chidory", 1.0F, 1.0F);

            // Create particle effect around the player

            particleStaff(player,world);

            //After a second make a dash
            new BukkitRunnable() {
                @Override
                public void run() {
                    // This vector represents the direction the player is looking.
                    Vector direction = player.getLocation().getDirection();
                    world.spawnParticle(Particle.valueOf("SONIC_BOOM"), player.getLocation(), 7, 0.5, 0.5, 0.5, 0);
                    // Multiply the direction by a factor to set the velocity. This propels the player in the look direction.
                    Vector dashVelocity = direction.multiply(4);
                    player.setVelocity(dashVelocity);

                    // Apply damage to entities in the dash path.
                    applyDamageInPlayer(player, player.getLocation(), dashVelocity);
                }
            }.runTaskLater(plugin, 33L);
        }
    }

    private void applyDamageInPath(Player player, Location startLocation, Vector dashVelocity) {
        final int iterations = 20; // Number of points along the path to check
        final Vector normalizedVelocity = dashVelocity.clone().normalize();
        final double segmentLength = dashVelocity.length() / iterations;

        for (int i = 0; i < iterations; i++) {
            // Calculate the current point's location along the dash path
            Location pointLocation = startLocation.clone().add(normalizedVelocity.clone().multiply(segmentLength * i));
            // Dynamically adjust the detection radius maybe needed here
            double searchRadius = 1.5; // Consider adjusting based on speed or elevation change
            List<Entity> nearbyEntities = (List<Entity>) pointLocation.getWorld().getNearbyEntities(pointLocation, searchRadius, searchRadius, searchRadius);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(6.0, player); // Apply damage
                }
            }
        }
    }
    private void particleStaff(Player player, World world){
        new BukkitRunnable() {
            int iterations = 20;

            @Override
            public void run() {
                if (iterations-- <= 0) {
                    cancel();
                    return;
                }

                // Adjust particle position around the player
                Location particleLocation = player.getLocation().add(0, 1, 0);

                // Spawn particle effect
                world.spawnParticle(Particle.ELECTRIC_SPARK, particleLocation, 7, 0.5, 0.5, 0.5, 0);
            }
        }.runTaskTimer(plugin, 1L, 4L); // Runs every tick for 20 ticks (1 second)
    }
    private void applyDamageInPlayer(Player player, Location startLocation, Vector dashVelocity) {
        // Radius for searching entities inside the player
        final double searchRadius = 1.5;
        final int iterations = 20; // Number of repetitions
        final int delayBetweenIterations = 1; // Delay between iterations in ticks
        // Schedule the execution of the repeated damage application
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        BukkitTask task = scheduler.runTaskTimer(plugin, () -> {
            // Get a list of entities inside the player
            List<Entity> nearbyEntities = (List<Entity>) player.getNearbyEntities(searchRadius, searchRadius, searchRadius);

            // Apply damage to all entities inside the player
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(6, player); // Apply damage
                }
            }
        }, 0L, delayBetweenIterations);

        // Cancel the task after the specified number of iterations
        scheduler.runTaskLater(plugin, task::cancel, iterations * delayBetweenIterations);
    }


    private boolean checkEvent(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Chidory");
    }
}