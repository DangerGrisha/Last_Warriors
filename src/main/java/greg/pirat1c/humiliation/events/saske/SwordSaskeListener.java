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
            //playAudio(player);
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
            hitLocation = player.getEyeLocation().add(player.getLocation().getDirection().multiply(4));
        }

        // Spawn particles at the location. This will be at the hit block or in the air.
        world.spawnParticle(Particle.SWEEP_ATTACK, hitLocation.add(0.5, 0.5, 0.5), 1);

        return hitLocation;
    }

   // private void playAudio(Player player) {
    //    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
    //}

    private void dealAreaDamage(Player player, Location hitLocation) {
        System.out.println("4");
        World world = player.getWorld();
        // Define the size of the damage area (assuming each block is 1 unit in size)
        double length = 3; // Corresponding to the length of the diamond blocks
        double width = 2; // Corresponding to the width of the diamond blocks
        double height = 1; // Assuming we want to damage entities up to 1 block above the ground

        // Get the direction in which the player is looking
        Vector direction = player.getLocation().getDirection().normalize();

        // Calculate the right and forward vectors relative to the player's direction
        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        Vector forward = direction.clone().setY(0).normalize();
        System.out.println("5");
        // Loop through the area and damage entities
        for (double x = -width / 2; x <= width / 2; x++) {
            System.out.println("6");
            for (double z = -length / 2; z <= length / 2; z++) {
                System.out.println("7");
                // Calculate the world coordinates of the current point in the loop
                Location point = hitLocation.clone().add(right.clone().multiply(x)).add(forward.clone().multiply(z));
                // Get entities in this specific point (considering a small buffer around the point)
                Collection<Entity> entities = world.getNearbyEntities(point, 0.5, height, 0.5);
                for (Entity entity : entities) {
                    System.out.println("8");
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        System.out.println("9");
                        // Apply damage to the entity
                        ((LivingEntity) entity).damage(5, player); // Damage value can be adjusted as needed
                    }
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
