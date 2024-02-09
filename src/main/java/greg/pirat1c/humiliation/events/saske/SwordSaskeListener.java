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
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;


import java.util.Collection;

public class SwordSaskeListener implements Listener {

    private JavaPlugin plugin;

    public SwordSaskeListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isSwordEvent(event, player)) {
            executeSwordAbility(player);
        }
    }

    private void executeSwordAbility(Player player) {

        pushPlayerBack(player);
        Location hitLocation = generateParticles(player);
        System.out.println("2");
        if (hitLocation != null) {
            System.out.println("3");
            playAudio(player);
            dealAreaDamage(player, hitLocation);
            pushEnemiesBack(player, hitLocation);
        }
    }

    private boolean isSwordEvent(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && player.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD
                && player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("katana saske");
    }

    private void pushPlayerBack(Player player) {
        Vector direction = player.getLocation().getDirection().multiply(-0.5);
        player.setVelocity(player.getVelocity().add(direction));
    }

    private Location generateParticles(Player player) {
        World world = player.getWorld();
        RayTraceResult rayTraceResult = world.rayTrace(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                4,
                FluidCollisionMode.NEVER,
                true,
                1,
                (entity) -> !entity.equals(player)
        );

        Location hitLocation;

        if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
            // If a block is hit, get that location
            hitLocation = rayTraceResult.getHitBlock().getLocation();
        } else {
            // If no block is hit, calculate the location in the direction the player is looking
            hitLocation = player.getEyeLocation().add(player.getLocation().getDirection().multiply(1));
        }

        // Spawn particles at the location. This will be at the hit block or in the air.
        world.spawnParticle(Particle.SWEEP_ATTACK, hitLocation.add(0.5, 0.5, 0.5), 1);

        return hitLocation;
    }

    private void playAudio(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
    }

    private void dealAreaDamage(Player player, Location hitLocation) {
        World world = player.getWorld();
        // Length should just be 1 for a straight line of damage, width is the range of the sword's swipe
        double length = 1; // Length of the sword's strike
        double width = 2; // Width of the sword's strike, a narrow line
        double height = 1; // Height of the damage area, 1 block above the ground

        // Calculate the direction vector relative to the player's direction
        Vector playerDirection = player.getLocation().getDirection().normalize();

        // Starting point should be right in front of the player
        Location start = hitLocation.clone().add(0, 1, 0); // Adjust y if needed to match the sword's visual effect

        // Loop through the blocks in front of the player to simulate the strike
        for (double z = 0; z < length; z++) {
            // Calculate the current point in the strike
            Location currentLocation = start.clone().add(playerDirection.clone().multiply(z));

            // Spawn particles at this location to visualize the strike area
            world.spawnParticle(Particle.CRIT_MAGIC, currentLocation, 1, 0.1, 0.1, 0.1, 0.01);

            // Check for entities at this point (you might need a slight buffer)
            Collection<Entity> entities = world.getNearbyEntities(currentLocation, 1, 1, 1);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    // Apply damage to the entity
                    ((LivingEntity) entity).damage(5, player); // Adjust the damage value as needed
                }
            }
        }
    }


    private void pushEnemiesBack(Player player, Location hitLocation) {
        double range = 3.0;
        Vector direction = player.getLocation().getDirection().normalize().multiply(1.5);
        Collection<Entity> entities = player.getWorld().getNearbyEntities(hitLocation, range, range, range);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && entity != player) {
                entity.setVelocity(direction);
            }
        }
    }
}
