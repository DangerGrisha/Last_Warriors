package greg.pirat1c.humiliation.events.ishigava;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import static greg.pirat1c.humiliation.events.ishigava.IshigavaConstants.*;

public class LastWaterWallListener implements Listener {
    private static boolean isOn = false;
    private boolean isInteracted = false;


    private JavaPlugin plugin;

    public LastWaterWallListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (checkEventForRightClick(event, player) && !isOn && !isInteracted) {
            isOn = true;
            initiateCycle(player, MAX_BEACONS);


            isInteracted = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    isInteracted = false;
                }
            }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds

        }else if(checkEventForRightClick(event, player) && isOn && !isInteracted){
            isOn = false;

            isInteracted = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    isInteracted = false;
                }
            }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds
        }

    }

    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains(NAME_OF_ISHIGAVA_WALL);
    }

    private void initiateCycle(Player player, int maxBeacons) {
        spawnArmorStandsRecursively(player, player.getLocation(), player.getLocation().getDirection(), maxBeacons);
        spawnArmorStandsRecursively(player, player.getLocation().add(0,4,0), player.getLocation().getDirection(), maxBeacons);
    }

    private void spawnArmorStandsRecursively(Player player, Location currentLocation, Vector direction, int remainingBeacons) {
        if (remainingBeacons <= 0) {
            return; // Base case: no more beacons to spawn
        }

        // Normalize and adjust direction
        direction.setY(0);
        direction.normalize();
        double distanceToBeacon = 1;
        double distanceToDuration = 2.5; // Distance for 'duration'
        double distanceToTemporary = 1.345; // Distance for 'temporary'
        if(remainingBeacons == MAX_BEACONS){//first time
            distanceToBeacon = 2.6;
            distanceToDuration = 2.5;
            //distanceToTemporary = 1.345;
        }else{
            distanceToBeacon = 1.5;// Distance for 'beacon' if not first time
            distanceToDuration = 3.5; //
           distanceToTemporary = 1.4;
        }

        // Play sound at player's location
        player.playSound(player, Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);

        // Create the initial beacon and duration ArmorStands at adjusted positions
        ArmorStand beacon = spawnArmorStand(currentLocation.clone().add(direction.multiply(distanceToBeacon)), "beacon", "beacon", remainingBeacons);
        ArmorStand duration = spawnArmorStand(currentLocation.clone().add(direction.multiply(distanceToDuration)), "duration", "duration", remainingBeacons);
        ArmorStand temporary = spawnArmorStand(currentLocation.clone().add(direction.multiply(distanceToTemporary)), "temporary", "temporary", remainingBeacons);
        // Schedule the next step after 1 second
        new BukkitRunnable() {
            @Override
            public void run() {
                // Spawn the temporary ArmorStand
                temporary.setRotation(player.getLocation().getYaw(), 0);

                // Continue the cycle with the new location and reduced count
                if(isOn){
                    spawnArmorStandsRecursively(player, temporary.getLocation().add(0,8,0), temporary.getLocation().getDirection(), remainingBeacons - 1);
                }
            }
        }.runTaskLater(plugin, 20); // 20 ticks = 1 second delay
    }




    private ArmorStand spawnArmorStand(Location location, String tag, String entityTag, int remainingBeacons) {

        // Spawn the ArmorStand 4 blocks below the target location
        Location startLocation = location.clone().add(0, -8, 0);
        Location targetLocation = location.clone().add(0, 1, 0);

        ArmorStand armorStand = (ArmorStand) startLocation.getWorld().spawnEntity(startLocation, EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setMarker(true);
        armorStand.setCustomNameVisible(false);
        armorStand.setCustomName(tag);
        armorStand.setArms(true);
        armorStand.setRightArmPose(new EulerAngle(0, 0, 0));

        // Apply lime dye named "Last Wall" if not temporary
        if (!"temporary".equals(entityTag)) {
            ItemStack limeDye = new ItemStack(Material.LIME_DYE);
            ItemMeta dyeMeta = limeDye.getItemMeta();
            dyeMeta.setDisplayName("Last Wall");
            limeDye.setItemMeta(dyeMeta);
            armorStand.getEquipment().setItemInMainHand(limeDye);
        } else {
            // Spawn particles for temporary ArmorStand
            final Location particleStart = armorStand.getLocation();
            final double particleStep = 0.2; // Distance between particles
            final int particleHeight = 25;  // Number of blocks above to spawn particles

            for (double y = 0; y <= particleHeight; y += particleStep) {
                Location particleLocation = particleStart.clone().add(0, y, 0);
                particleLocation.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, particleLocation, 1,
                        new org.bukkit.Particle.DustOptions(org.bukkit.Color.BLUE, 1.0F)); // Blue dust particle
            }
        }

        if (entityTag != null) {
            armorStand.addScoreboardTag(entityTag);
        }

        // Slowly raise the ArmorStand to the target location
        new BukkitRunnable() {
            double currentY = startLocation.getY();
            final double targetY = targetLocation.getY();
            final double step = 0.05; // Step size for each movement (adjust as needed for smoothness)
            boolean soundsPlayed = false;

            @Override
            public void run() {
                if (currentY >= targetY || armorStand.isDead()) {
                    this.cancel(); // Stop the task once it reaches the target height or if the ArmorStand is removed
                    return;
                }

                // Move the ArmorStand slightly up
                currentY += step;
                Location newLocation = armorStand.getLocation();
                newLocation.setY(currentY);
                armorStand.teleport(newLocation); // Update the ArmorStand's position

                // Play sounds 0.5 seconds after the wall starts raising
                if (!soundsPlayed) {
                    soundsPlayed = true;

                    // Schedule the sounds
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            playNearbySound(startLocation, Sound.ENTITY_WARDEN_DIG, 0.5f, 1.0f); // Play "dig" sound
                        }
                    }.runTaskLater(plugin, 5L); // 0.5 seconds = 10 ticks

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            playNearbySound(startLocation, Sound.ENTITY_WARDEN_ROAR, 0.5f, 1.0f); // Play first "roar" sound
                        }
                    }.runTaskLater(plugin, 15L); // 0.2 seconds after "dig" = 14 ticks

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            playNearbySound(startLocation, Sound.ENTITY_WARDEN_ROAR, 0.5f, 1.0f); // Play second "roar" sound
                        }
                    }.runTaskLater(plugin, 30L); // 0.2 seconds after first "roar" = 18 ticks
                }
            }
        }.runTaskTimer(plugin, remainingBeacons * 20 + 5L, 1L); // Run every tick (1L) for smooth movement

        //DOWN
        new BukkitRunnable() {
            double currentY = targetLocation.getY(); // Start from the target height
            final double targetY = startLocation.getY(); // Target is the start location
            final double step = 0.05; // Step size for downward movement
            boolean soundsPlayed = false;

            @Override
            public void run() {
                if (currentY <= targetY || armorStand.isDead()) {
                    armorStand.remove(); // Remove the ArmorStand once it reaches the bottom
                    this.cancel();
                    return;
                }

                // Schedule the sounds 0.5 seconds after starting the downward movement
                if (!soundsPlayed) {
                    soundsPlayed = true;

                    // Play "roar" sound after a delay
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            playNearbySound(startLocation, Sound.ENTITY_WARDEN_ROAR, 0.5f, 1.0f);
                        }
                    }.runTaskLater(plugin, 10L); // Delay of 10 ticks (0.5 seconds)
                }

                // Move the ArmorStand slightly down
                currentY -= step;
                Location newLocation = armorStand.getLocation();
                newLocation.setY(currentY);
                armorStand.teleport(newLocation); // Update the ArmorStand's position
            }
        }.runTaskTimer(plugin, 600L, 1L); // Start after 600 ticks (30 seconds) and run every tick
        return armorStand;
    }

        private void playNearbySound(Location location, Sound sound, float volume, float pitch) {
        int radius = 30; // Radius within which players will hear the sound
        location.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(location) <= radius)
                .forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }



}
