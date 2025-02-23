package greg.pirat1c.humiliation.events.fukuko;

import greg.pirat1c.humiliation.events.ladynagan.CooldownManager;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static greg.pirat1c.humiliation.events.fukuko.FukukoConstants.*;
import static greg.pirat1c.humiliation.events.ishigava.IshigavaConstants.NAME_OF_ISHIGAVA_WALL;

public class PistolListener implements Listener {

    private JavaPlugin plugin = null;
    private static boolean cdOff = true;
    private boolean isInteracted = false;


    public PistolListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        System.out.println("A1");
        if (checkEventForRightClickOnCrossbow(event, player) && isInteracted) {
            event.setCancelled(true); // cancel to not disrapt crossbow logic
            System.out.println("A11");
        }
        if(checkEventForLeftClick(event,player) && cdOff && !isInteracted){
            isInteracted = true;
            System.out.println("A2");

            new BukkitRunnable() {
                @Override
                public void run() {
                    isInteracted = false;
                }
            }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds

            shootSystem(player,event);

            //make cd...
            cdOff = false;
            new BukkitRunnable() {
                @Override
                public void run() {
                    cdOff = true;
                }
            }.runTaskLater(plugin, COLDOWN_OF_PISTOL_FUKUKO); // 2 ticks = 0.1 seconds
        }
    }
    private void shootSystem(Player player, PlayerInteractEvent event){
        //System.out.println("A2");
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        ArmorStand armorStand = SummonArmorStand(player, eyeLocation, direction);
        shoot(player, armorStand, direction, event);
        //sound of shoot mb
    }
    //Shoot System Shoot SystemShoot SystemShoot SystemShoot System Shoot System
    private void shoot(Player player, ArmorStand armorStand, Vector direction, PlayerInteractEvent event) {

        Location nextLocation = null;
        // Schedule the removal of the ArmorStand after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, armorStand::remove, REMOVE_BULLET_AFTER_FUKUKO); // 1 seconds (20 ticks per second)

        Vector finalDirection = direction.clone(); // Clone the original direction vector to modify it
        // Schedule a task to move the ArmorStand forward every 0.1 second
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!armorStand.isDead()) {
                updateNextLocation(finalDirection, nextLocation);

                // Check for collisions
                //System.out.println("is Ulting A1 : " + isUlting);
                checkBulletCollision(armorStand, player);

                // Add a slight upward velocity to counteract gravity
                finalDirection.setY(finalDirection.getY() + 0.006); // Adjust this value as needed
                armorStand.setVelocity(finalDirection.normalize().multiply(1.0)); // Set weak motion forward
                checkBulletCollision(armorStand, player);
            }
        }, 0L, 2L).getTaskId(); // 0 tick delay, 2 tick interval (10 ticks per second)
    }
    private void updateNextLocation(Vector direction, Location nextLocation) {
        if (nextLocation != null && direction != null) {
            nextLocation.add(direction);
        }
    }

    private boolean checkEventForRightClickOnCrossbow(PlayerInteractEvent event, Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand != null && itemInMainHand.getType() == Material.CROSSBOW && itemInMainHand.hasItemMeta()) {
            ItemMeta meta = itemInMainHand.getItemMeta();
            return meta.hasDisplayName() && (meta.getDisplayName().equals(NAME_OF_PISTOL_FUKUKO));
        }
        return false;
    }
    private void checkBulletCollision(ArmorStand bullet, Player shooter) {
        Location bulletLocation = bullet.getLocation();

        // Slightly offset the bullet's ray trace start position upwards to avoid floor clipping
        bulletLocation = bulletLocation.clone().add(0, -0.4, 0);
        // Create a ray from the adjusted start position to the next location

        // Check for players nearby the bullet's next location
        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            // Skip the shooter
            if (nearbyPlayer.equals(shooter)) {
                continue; // Ignore the shooter
            }

            // Check if the player is within collision range
            if (nearbyPlayer.getLocation().distance(bulletLocation) <= 1.0) { // Adjust the value based on the bullet's speed
                if (!nearbyPlayer.isInvulnerable()) {
                    // Apply damage
                    nearbyPlayer.damage(DAMAGE_OF_BULLET_FUKUKO);
                    nearbyPlayer.getWorld().playSound(nearbyPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                }
                bullet.remove(); // Remove the bullet
                return;
            }
        }
    }

    private boolean checkEventForLeftClick(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains(NAME_OF_PISTOL_FUKUKO);
    }
    private static ArmorStand SummonArmorStand(Player player, Location eyeLocation, Vector direction) {
        // Spawn an ArmorStand at eye level in front of the player
        ArmorStand armorStand = player.getWorld().spawn(eyeLocation.add(direction).add(0,-0.5,0), ArmorStand.class);
        armorStand.setVisible(false); // Make the ArmorStand invisible
        armorStand.setGravity(true); // Disable gravity for the ArmorStand
        armorStand.setSmall(true);

        // armorStand.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, 255, true, false));

        // Give the ArmorStand a red dye renamed as "Bullet"
        ItemStack bullet = new ItemStack(Material.RED_DYE);
        ItemMeta bulletMeta = bullet.getItemMeta();
        bulletMeta.setDisplayName("Bullet");
        bullet.setItemMeta(bulletMeta);
        armorStand.getEquipment().setItemInMainHand(bullet);

        // Add the tag "bullet" to the ArmorStand
        armorStand.addScoreboardTag("bullet");

        return armorStand;
    }


}
