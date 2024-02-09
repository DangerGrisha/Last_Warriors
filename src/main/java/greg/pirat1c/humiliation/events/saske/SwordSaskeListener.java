package greg.pirat1c.humiliation.events.saske;

import org.bukkit.*;
import org.bukkit.block.Block;
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
       // Location hitLocation = generateParticles(player);
        Vector direction = player.getLocation().getDirection().normalize();
        Location hitLocation = player.getLocation().add(direction).add(0, player.getEyeHeight() + 1, 0);
        System.out.println("2");
        if (hitLocation != null) {
            System.out.println("3");
            playAudio(player);
            dealAreaDamage(player);
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
       // world.spawnParticle(Particle.NAUTILUS, hitLocation.add(0.5, 0.5, 0.5), 1);

        return hitLocation;
    }

    private void playAudio(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
    }

    private void dealAreaDamage(Player player) {
        World world = player.getWorld();
        Vector direction = player.getEyeLocation().getDirection().normalize();

        // Calculate the side direction for the left and right damage application
        Vector sideDirection = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        // The block in front of the player where the damage will start, adjusted to eye level
        Location frontBlock = player.getEyeLocation().add(direction);

        // Apply damage to the front block and one block to each side
        applyDamageAndPushEntities(world, frontBlock, player);
        applyDamageAndPushEntities(world, frontBlock.clone().add(sideDirection), player);
        applyDamageAndPushEntities(world, frontBlock.clone().subtract(sideDirection.multiply(1.5)), player);
        spawnSwordCutParticles(world, frontBlock, direction, player);

    }

    private void applyDamageAndPushEntities(World world, Location location, Player player) {
        double damageRadius = 1.0; // Radius around the block within which entities will be damaged
        Vector pushDirection = player.getLocation().getDirection().normalize().multiply(1.5);

        Collection<Entity> entities = world.getNearbyEntities(location, damageRadius, damageRadius, damageRadius);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.damage(5.0); // Damage value, adjust as needed
                livingEntity.setVelocity(pushDirection); // Push the entity back
                // Optionally, spawn particles directly at the entity's location for additional effect
                world.spawnParticle(Particle.CRIT, livingEntity.getLocation(), 5, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }
    private void spawnSwordCutParticles(World world, Location startLocation, Vector direction, Player player) {
        world.spawnParticle(Particle.SWEEP_ATTACK, startLocation.add(0.1, 0.1, 0.1), 1);
        // This will be the horizontal vector for the sword swing
        Vector sideDirection = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        // Adjust the Y offset to spawn particles just below the player's line of sight
        double yOffset = -0.2;

        // Define the arc/line length of the sword swing effect
        double lineLength = 6; // Adjust if needed
        int particleCount = 30; // Number of particles to spawn along the line

        // Spawn the first line of particles
        spawnParticleLine(world, startLocation, sideDirection, direction, yOffset, lineLength - 2, particleCount);
        spawnParticleLineSweepAttack(world, startLocation, sideDirection, direction, yOffset, lineLength -2, particleCount-27);
        // Spawn a second line of particles one block further forward
        Location oneBlockFurther = startLocation.clone().add(direction);
        spawnParticleLine(world, oneBlockFurther, sideDirection, direction, yOffset, lineLength, particleCount);
        spawnParticleLineSweepAttack(world, oneBlockFurther, sideDirection, direction, yOffset, lineLength, particleCount-24);
    }

    private void spawnParticleLine(World world, Location startLocation, Vector sideDirection, Vector direction, double yOffset, double lineLength, int particleCount) {
        // Calculate the points along the line of the sword swing and spawn particles
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            // Calculate the offset along the sword swing line
            Vector offset = sideDirection.clone().multiply(progress * lineLength - (lineLength / 2));
            // Set the particle location just below the player's line of sight
            Location particleLocation = startLocation.clone().add(offset).add(0, yOffset, 0);
            // Spawn the particle at the calculated location
            world.spawnParticle(Particle.NAUTILUS, particleLocation, 1, 0.3, 0.1, 0.3, 0);
        }
    }
    private void spawnParticleLineSweepAttack(World world, Location startLocation, Vector sideDirection, Vector direction, double yOffset, double lineLength, int particleCount) {
        // Calculate the points along the line of the sword swing and spawn particles
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            // Calculate the offset along the sword swing line
            Vector offset = sideDirection.clone().multiply(progress * lineLength - (lineLength / 2));
            // Set the particle location just below the player's line of sight
            Location particleLocation = startLocation.clone().add(offset).add(0, yOffset, 0);
            // Spawn the particle at the calculated location
            world.spawnParticle(Particle.SWEEP_ATTACK, particleLocation, 1, 0.3, 0.1, 0.3, 0);
        }
    }
}
