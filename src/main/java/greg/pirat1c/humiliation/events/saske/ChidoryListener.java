package greg.pirat1c.humiliation.events.saske;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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
            world.playSound(initialLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);

            new BukkitRunnable() {
                @Override
                public void run() {
                    // This vector represents the direction the player is looking.
                    Vector direction = player.getLocation().getDirection();

                    // Multiply the direction by a factor to set the velocity. This propels the player in the look direction.
                    Vector dashVelocity = direction.multiply(4);
                    player.setVelocity(dashVelocity);

                    // Apply damage to entities in the dash path.
                    applyDamageInPath(player, player.getLocation(), dashVelocity);
                }
            }.runTaskLater(plugin, 20L);
        }
    }

    private void applyDamageInPath(Player player, Location startLocation, Vector dashVelocity) {
        final double stepSize = 0.25; // Increased detection frequency
        Vector increment = dashVelocity.clone().normalize().multiply(stepSize);
        //int iterations = (int) (dashVelocity.length() / stepSize);
        int iterations = 15;
        Location currentLocation = startLocation.clone();

        for (int i = 0; i <= iterations; i++) {
            // Dynamically adjust the detection range based on the speed (optional)
            double dynamicRange = 1.0 + (dashVelocity.length() / 10);
            currentLocation.add(increment);
            player.getWorld().getNearbyEntities(currentLocation, dynamicRange, dynamicRange, dynamicRange).forEach(entity -> {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(6.0, player); // Apply damage
                }
            });
        }
    }

    private boolean checkEvent(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Chidory");
    }
}