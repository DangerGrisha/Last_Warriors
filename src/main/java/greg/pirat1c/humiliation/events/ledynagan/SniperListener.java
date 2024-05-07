package greg.pirat1c.humiliation.events.ledynagan;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Objects;

public class SniperListener implements Listener {

    private final JavaPlugin plugin;
    private boolean wearingPumpkin = false;

    private ItemStack previousItemInMainHand;
    private PotionEffect slowEffect; // Store the slow effect

    // delay after u get pumpkin for shoot
    private static final long delayAfterPumpkin = 10L;
    private boolean delayAfterPumpkinIsDone = false;
    private static final long delayAfterShoot = 60L;
    private static final int distanceDetectFromBullet = 10; // distance at which bullet can detect u and rotate direction to u
    private static final double damageOfBullet = 5.0;
    private static final long removeBulletAfter = 60L; //removeBullet after some seconds after shoot 20L - 1s
    private boolean delayAfterShootIsDone = true;
    private boolean changedDirectionOfBullet = false;
    public Vector finalDirection;
    public ArmorStand armorStand = null;
    public Location armorLocation = null;
    private Location nextLocation;
    public SniperListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the event corresponds to right-click with "T-742K Mori" stick or crossbow
        if (checkEventForRightClick(event, player) || checkEventForRightClickOnCrossbow(event, player)) {
            event.setCancelled(true); // Cancel the action to prevent hitting

            // If right mouse button is clicked
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!wearingPumpkin) {
                    // Equip a pumpkin as a helmet for the player
                    ItemStack pumpkinHelmet = new ItemStack(Material.CARVED_PUMPKIN);
                    player.getInventory().setHelmet(pumpkinHelmet);

                    if (checkEventForRightClickOnCrossbow(event, player)) {
                        // Replace the crossbow with the previous item
                        player.getInventory().setItemInMainHand(previousItemInMainHand);
                    } else {
                        // Replace the stick with a loaded crossbow
                        previousItemInMainHand = player.getInventory().getItemInMainHand();
                        ItemStack crossbow = createT742KMoriCrossbow();
                        player.getInventory().setItemInMainHand(crossbow);
                        setSlowEffect(player,3);

                    }
                    wearingPumpkin = true;
                } else {
                    removePumpkinAndEffect(player);
                }
            }
            //delay after pumpkin for shoot that u cannot shoot instantly
            new BukkitRunnable() {
                @Override
                public void run() {
                    delayAfterPumpkinIsDone = true;
                }
            }.runTaskLater(plugin, delayAfterPumpkin);
            //System.out.println("A0");
            //Location armorLocation = null;
            if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && delayAfterPumpkinIsDone && checkEventForRightClickOnCrossbow(event, player)) {
                changedDirectionOfBullet = false;
                //System.out.println("A1");
                if (!delayAfterShootIsDone) {
                    armorLocation = armorStand.getLocation();
                    if(isPlayerNearbyOfBullet(armorLocation,player) && armorLocation != null){
                        changedDirectionOfBullet = true;
                        //System.out.println("A3");
                    }

                }
                //System.out.println("A4" + armorLocation);
                if (delayAfterShootIsDone) {
                    //System.out.println("A2");
                    Location eyeLocation = player.getEyeLocation();
                    Vector direction = eyeLocation.getDirection();
                    armorStand = SummonArmorStand(player, eyeLocation, direction);
                    shoot(player,armorStand,direction);
                }

            }
        }
    }
    //Shoot System Shoot SystemShoot SystemShoot SystemShoot System Shoot System
    private void shoot(Player player, ArmorStand armorStand, Vector direction) {
        final boolean[] directionChanged = {false}; // Declare an array with one element to make it effectively final

        // Schedule the removal of the ArmorStand after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, armorStand::remove, removeBulletAfter); // 3 seconds (20 ticks per second)

        // Variable to store the task ID of ArmorStand movement task
        int armorStandTaskId = -1;
        finalDirection = direction.clone(); // Clone the original direction vector to modify it
        // Schedule a task to move the ArmorStand forward every 0.1 second
        armorStandTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!armorStand.isDead()) {
                updateNextLocation(finalDirection);

                // Check for collisions
                checkBulletCollision(armorStand);

                // Add a slight upward velocity to counteract gravity
                finalDirection.setY(finalDirection.getY() + 0.002); // Adjust this value as needed

                //System.out.println("C1" + finalDirection);
                if (changedDirectionOfBullet && !directionChanged[0]) { // Check if the direction of the bullet needs to be changed
                    Player nearestPlayer = GetPlayerNearbyOfBullet(armorStand.getLocation(), player);
                    if (nearestPlayer != null) { // If there is a nearest player
                        // Calculate the new direction vector towards the nearest player
                        Vector newDirection = nearestPlayer.getLocation().subtract(armorStand.getLocation()).toVector();

                        // Nullify the final direction to prepare for the new direction
                        finalDirection = new Vector(0, 0, 0);

                        // Update the bullet's direction
                        finalDirection = newDirection;
                        finalDirection.setY(finalDirection.getY() + 0.05);
                        // Update the bullet's next location (optional, if needed)
                        updateNextLocation(newDirection);

                        // Indicate that the direction has been changed
                        directionChanged[0] = true;

                        // Update the rotation of the armor stand to face the new direction
                        updateArmorStandRotation(armorStand, newDirection);
                    }
                }
                armorStand.setVelocity(finalDirection.normalize().multiply(1)); // Set weak motion forward
                checkBulletCollision(armorStand);
            }
        }, 0L, 2L).getTaskId(); // 0 tick delay, 2 tick interval (10 ticks per second)

        // Schedule a task to mark the direction as unchanged after a delay
        int finalArmorStandTaskId = armorStandTaskId;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            directionChanged[0] = true; // Reset the direction changed flag after a delay
            // Cancel the movement task
            Bukkit.getScheduler().cancelTask(finalArmorStandTaskId);
        }, 60L); // 3 seconds (20 ticks per second)

        // Delay after shoot
        delayAfterShootIsDone = false;
        new BukkitRunnable() {
            @Override
            public void run() {
                delayAfterShootIsDone = true;
            }
        }.runTaskLater(plugin, delayAfterShoot);
    }



    private void updateNextLocation(Vector direction) {
        if (nextLocation != null && direction != null) {
            nextLocation.add(direction);
        }
    }

    // Method to update the rotation of the armor stand to face a given direction
    private void updateArmorStandRotation(ArmorStand armorStand, Vector direction) {
        double x = direction.getX();
        double z = direction.getZ();
        double theta = Math.atan2(-x, z); // Calculate the angle between z-axis and direction vector
        theta += Math.PI / 2; // Adjust the angle to align with Bukkit's coordinate system
        theta *= -180 / Math.PI; // Convert radians to degrees
        Location loc = armorStand.getLocation();
        loc.setYaw((float) theta); // Set the yaw (horizontal rotation)
        armorStand.teleport(loc); // Teleport to apply rotation
    }


    private void checkBulletCollision(ArmorStand bullet) {
        Location bulletLocation = bullet.getLocation();
        nextLocation = bulletLocation.clone().add(bullet.getVelocity());

        // Create a ray from the bullet's current location to the next location
        RayTraceResult result = bulletLocation.getWorld().rayTrace(bulletLocation, bullet.getVelocity(), bullet.getVelocity().length() + 1, FluidCollisionMode.NEVER, true, 0, null);

        if (result != null && result.getHitBlock() != null) {
            // Block is hit, remove the bullet
            bullet.remove();
            // You may also play a sound indicating the collision with the block
            bulletLocation.getWorld().playSound(bulletLocation, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
            return;
        }

        // Check for player nearby the bullet's next location
        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            if (nearbyPlayer.getLocation().distance(nextLocation) <= 1.0) { // Adjust the value based on the bullet's speed
                if (!nearbyPlayer.isInvulnerable()) {
                    nearbyPlayer.damage(damageOfBullet);
                    nearbyPlayer.getWorld().playSound(nearbyPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                }
                bullet.remove(); // Remove the bullet
                return;
            }
        }
    }





    private Player GetPlayerNearbyOfBullet(Location location,Player owner) {
        //System.out.println("B1");
        for (Player player : Bukkit.getOnlinePlayers()) {
            //System.out.println("B3");
            if (player.getLocation().distance(location) <= distanceDetectFromBullet && player != null && !isAlly(player, owner)) {
                //System.out.println("B3");
                return player;
            }
        }
        return null;
    }
    private boolean isPlayerNearbyOfBullet(Location location,Player owner) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(location) <= distanceDetectFromBullet && player != null && !isAlly(player, owner)) {
                return true;
            }
        }
        return false;
    }
    private boolean isAlly(Player player, Player placer) {
        // Check if the player is in the same team as the placer
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        Team placerTeam = placer.getScoreboard().getPlayerTeam(placer);
        Team playerTeam = player.getScoreboard().getPlayerTeam(player);
        return placerTeam != null && playerTeam != null && placerTeam.equals(playerTeam);
    }

    private static ArmorStand SummonArmorStand(Player player, Location eyeLocation, Vector direction) {
        // Spawn an ArmorStand at eye level in front of the player
        ArmorStand armorStand = player.getWorld().spawn(eyeLocation.add(direction), ArmorStand.class);
        armorStand.setVisible(true); // Make the ArmorStand invisible
        armorStand.setGravity(true); // Disable gravity for the ArmorStand
        armorStand.setSmall(true);

        // Give the ArmorStand a red dye renamed as "Bullet"
        ItemStack bullet = new ItemStack(Material.RED_DYE);
        ItemMeta bulletMeta = bullet.getItemMeta();
        bulletMeta.setDisplayName("Bullet");
        bullet.setItemMeta(bulletMeta);
        armorStand.getEquipment().setItemInMainHand(bullet);
        return armorStand;
    }


    //Aiming System Aiming System Aiming System Aiming System Aiming System
    private ItemStack createT742KMoriCrossbow() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        crossbow.setDurability((short)crossbow.getType().getMaxDurability());
        ItemMeta meta = crossbow.getItemMeta();
        meta.setDisplayName("T-742K Mori");
        crossbow.setItemMeta(meta);
        ItemStack arrow = new ItemStack(Material.ARROW);
        CrossbowMeta crossbowMeta = (CrossbowMeta) crossbow.getItemMeta();
        crossbowMeta.addChargedProjectile(arrow);
        crossbow.setItemMeta(crossbowMeta);

        return crossbow;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Check if the player is wearing a pumpkin and the held item is not the stick
        if (wearingPumpkin && (player.getInventory().getItem(event.getNewSlot()) == null ||
                player.getInventory().getItem(event.getNewSlot()).getType() != Material.STICK)) {
            removePumpkinAndEffect(player);
        }
    }
    private void removePumpkinAndEffect(Player player) {
        // Remove the pumpkin from the helmet slot
        player.getInventory().setHelmet(null);

        // Remove the slow effect
        player.removePotionEffect(PotionEffectType.SLOW);

        // Replace the crossbow with the previous item
        player.getInventory().setItemInMainHand(previousItemInMainHand);

        wearingPumpkin = false;
    }



    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Check if the player is wearing a pumpkin and is sneaking
        if (wearingPumpkin && event.isSneaking()) {
            setSlowEffect(player,5);
        }
        if (wearingPumpkin && !event.isSneaking() && slowEffect != null) {
            // Remove the slow effect if it exists and the player is not sneaking
            player.removePotionEffect(slowEffect.getType());
            setSlowEffect(player,3);
        }
    }
    public void setSlowEffect(Player player,int level){
        slowEffect = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, level);
        player.addPotionEffect(slowEffect);
    }

    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.STICK &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("T-742K Mori");
    }
    private boolean checkEventForRightClickOnCrossbow(PlayerInteractEvent event, Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand != null && itemInMainHand.getType() == Material.CROSSBOW && itemInMainHand.hasItemMeta()) {
            ItemMeta meta = itemInMainHand.getItemMeta();
            return meta.hasDisplayName() && meta.getDisplayName().equals("T-742K Mori");
        }
        return false;
    }
}
