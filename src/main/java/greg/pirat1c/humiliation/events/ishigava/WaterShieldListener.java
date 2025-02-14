package greg.pirat1c.humiliation.events.ishigava;

import org.bukkit.Location;
import org.bukkit.Material;
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

public class WaterShieldListener implements Listener {

    private JavaPlugin plugin;

    public WaterShieldListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (checkEventForRightClick(event, player)) {
            if (player.isSneaking()) {
                // Spawns the full shield structure
                spawnShieldStructure(player, plugin);
            } else {
                // Spawns only the main ArmorStand 4 blocks lower with a different name
                Vector direction = player.getEyeLocation().getDirection();
                Location spawnLocation = player.getLocation().add(direction.multiply(4)).add(0, 1, 0);
                spawnSingleMovingShield(spawnLocation, plugin, direction);
            }
        }
    }

    private void spawnSingleMovingShield(Location location, JavaPlugin plugin, Vector direction) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setInvulnerable(true);
            stand.setRightArmPose(new EulerAngle(0, 0, 0));
            stand.setCanPickupItems(false);
            stand.setMarker(true);
            stand.setMetadata("water_shield_move", new FixedMetadataValue(plugin, true));

            ItemStack limeDye = new ItemStack(Material.LIME_DYE);
            ItemMeta dyeMeta = limeDye.getItemMeta();
            dyeMeta.setDisplayName("WaterShieldMove");
            limeDye.setItemMeta(dyeMeta);
            stand.getEquipment().setItemInMainHand(limeDye);

            // Move ArmorStand forward every 5 ticks
            moveArmorStand(stand, direction, plugin);

            // Remove after 20 seconds
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, stand::remove, 400L);
        });
    }

    private void moveArmorStand(ArmorStand stand, Vector direction, JavaPlugin plugin) {
        new BukkitRunnable() {
            public void run() {
                if (!stand.isValid()) {
                    this.cancel();
                    return;
                }
                stand.teleport(stand.getLocation().add(direction.clone().multiply(0.06)));
            }
        }.runTaskTimer(plugin, 0L, 2L); // Run task every 5 ticks (approximately 0.25 seconds)
    }

    private void spawnArmorStand(Location location, boolean isMain, JavaPlugin plugin) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setInvulnerable(true);
            stand.setRightArmPose(new EulerAngle(0, 0, 0));
            stand.setCanPickupItems(false); // Prevent item pickup
            stand.setMarker(false); // Reduce hitbox size if true
            stand.setMetadata("water_shield", new FixedMetadataValue(plugin, true)); // Add metadata

            if (isMain) {
                ItemStack limeDye = new ItemStack(Material.LIME_DYE);
                ItemMeta dyeMeta = limeDye.getItemMeta();
                dyeMeta.setDisplayName("WaterShield");
                limeDye.setItemMeta(dyeMeta);
                stand.getEquipment().setItemInMainHand(limeDye);
            }

            // Prevent players from interacting with `ArmorStand`
            //stand.addEquipmentLock(EquipmentSlot.H); // Prevent interaction with the held item

            // Remove after 20 seconds
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, stand::remove, 400L);
        });
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        Location hitLocation = projectile.getLocation();
        //System.out.println("got a hit (in WaterShieldListener)");
        // Get nearby entities in an optimized radius
        List<Entity> nearbyEntities = (List<Entity>) hitLocation.getWorld().getNearbyEntities(hitLocation, 1, 1, 1, entity ->
                (entity instanceof ArmorStand && entity.hasMetadata("water_shield")));

        for (Entity entity : nearbyEntities) {
            ArmorStand armorStand = (ArmorStand) entity;
            if (armorStand.getLocation().distance(hitLocation) <= 1) { // Reduce radius to 1 for higher accuracy
                if (projectile.getType() == EntityType.SNOWBALL || projectile.getType() == EntityType.EGG || projectile.getType() == EntityType.ARROW || projectile.getType() == EntityType.SPECTRAL_ARROW) {
                    projectile.remove();
                    break;
                }
            }
        }
    }

    private void spawnShieldStructure(Player player, JavaPlugin plugin) {
        Vector forward = player.getEyeLocation().getDirection().normalize();
        Vector right = perpendicular(forward);

        Location baseLocation = player.getLocation().add(forward).add(0, player.getEyeHeight() - 1, 0); // Center at eye level

        // Main ArmorStand
        spawnArmorStand(baseLocation, true, plugin);
        spawnArmorStand(baseLocation.clone().add(0, 2, 0), false, plugin); // Add ArmorStand above the main one

        // Spawn secondary ArmorStands and add extra ArmorStands above them
        double[] distances = {0.5, 1.0, 1.5}; // Distances for secondary ArmorStands
        for (double dist : distances) {
            // Right side
            Location rightLocation = baseLocation.clone().add(right.clone().multiply(dist));
            spawnArmorStand(rightLocation, false, plugin);
            spawnArmorStand(rightLocation.clone().add(0, 2, 0), false, plugin); // Add ArmorStand above secondary

            Location rightExtra = baseLocation.clone().add(right.clone().multiply(-dist));
            spawnArmorStand(rightExtra, false, plugin);
            spawnArmorStand(rightExtra.clone().add(0, 2, 0), false, plugin); // Add ArmorStand above secondary

            // Left side
            Location leftLocation = baseLocation.clone().add(right.clone().multiply(-dist));
            spawnArmorStand(leftLocation, false, plugin);
            spawnArmorStand(leftLocation.clone().add(0, 2, 0), false, plugin); // Add ArmorStand above secondary

            Location leftExtra = baseLocation.clone().add(right.clone().multiply(dist));
            spawnArmorStand(leftExtra, false, plugin);
            spawnArmorStand(leftExtra.clone().add(0, 2, 0), false, plugin); // Add ArmorStand above secondary
        }
    }

    private Vector perpendicular(Vector direction) {
        return new Vector(-direction.getZ(), 0, direction.getX()).normalize();
    }

    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains(NAME_OF_ISHIGAVA_SHIELD);
    }
}
