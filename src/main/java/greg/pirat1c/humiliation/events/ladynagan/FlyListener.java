package greg.pirat1c.humiliation.events.ladynagan;

import greg.pirat1c.humiliation.command.ladynagan.ExplosionGive;
import greg.pirat1c.humiliation.command.ladynagan.FlyGive;
import greg.pirat1c.humiliation.command.ladynagan.UltraGive;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static greg.pirat1c.humiliation.events.ladynagan.LadyConstants.*;

public class FlyListener implements Listener {
    private int flyCounter = FLY_COUNTER; // Counter to track the number of times Fly+ is used
    private final Map<Location, Material> previousBlocks = new HashMap<>(); // Map to store previous block locations

    private JavaPlugin plugin = null;
    private CooldownManager cooldownManager = null;

    public FlyListener(JavaPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    private boolean isInteracted = false;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        // Check if the player right-clicked "Start Fly"
        if (checkEventForRightClick(event, player, "Start Fly") && !isInteracted) {
            // Change the held item to Fly+
            ItemStack flyPlusFeather = createFlyPlusFeather();
            isInteracted = true;
            player.getInventory().setItemInMainHand(flyPlusFeather);
            // Reset the fly counter when using Start Fly
            flyCounter = FLY_COUNTER;
            new BukkitRunnable() {
                @Override
                public void run() {
                    isInteracted = false;
                }
            }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds

            //timer 20 seconds to stop ability fly+
            new BukkitRunnable() {
                @Override
                public void run() {
                    deletePreviousBlocks();
                    delayForUlta(player, "FlyLadyNagan", 5, COOLDOWN_FLY);
                }
            }.runTaskLater(plugin, FLY_TIME);

        } else if (checkEventForRightClickForFly(event, player) && flyCounter >= 0 && !isInteracted) {
            // Decrement the fly counter
            flyCounter--;
            // Place invisible block under the player
            placeInvisibleBlockUnderPlayer(player);
            // Update the Fly+ feather name
            updateFlyPlusFeather(player);
            // Set the flag to true to indicate interaction
            isInteracted = true;
            // Create an asynchronous task to set the flag back to false after 0.1 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    isInteracted = false;
                }
            }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds
            if (flyCounter == 0) {
                // this will interrupt the top timer as it will notice that this cooldown already exis only if FLY_TIME < COOLDOWN_TIME
                //here should be delete last blocks
                delayForUlta(player, "FlyLadyNagan", 5, COOLDOWN_FLY);
            }

        }
    }
    private void delayForUlta(Player player, String nameOfAbilitySpecific, int inventorySlot, int delayInSeconds) {

        cooldownManager.startCooldown(player, nameOfAbilitySpecific, inventorySlot, delayInSeconds, true);
        // Schedule the dye change back to red after 20 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the player still has the yellow dye
                if (cooldownManager.isCooldownComplete(player, nameOfAbilitySpecific)) {
                    player.getInventory().setItem(inventorySlot, FlyGive.getItem());
                    flyCounter = 0;
                }
            }
        }.runTaskLater(plugin, delayInSeconds * 20L); // 20 ticks per second, so 20 seconds is 20 * 20 ticks
    }

    private ItemStack createFlyPlusFeather() {
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        meta.setDisplayName("Fly+");
        feather.setItemMeta(meta);
        return feather;
    }


    private ItemStack createFlyFeather() {
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        meta.setDisplayName("Start Fly");
        feather.setItemMeta(meta);
        return feather;
    }

    private void updateFlyPlusFeather(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Fly+ " + (flyCounter));// Update the display name
            itemInHand.setItemMeta(meta);
        }
    }

    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player, String nameOfItem) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.FEATHER &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(nameOfItem);
    }

    private boolean checkEventForRightClickForFly(PlayerInteractEvent event, Player player) {
        final Set<String> flyOptions = new HashSet<>(Arrays.asList(
                "Fly+", "Fly+ 1", "Fly+ 2", "Fly+ 3", "Fly+ 4", "Fly+ 5", "Fly+ 6"));
        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        final ItemMeta mainHandItemMeta = mainHand.hasItemMeta() ? mainHand.getItemMeta() : null;

        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                mainHand.getType() == Material.FEATHER &&
                mainHand.hasItemMeta() &&
                mainHandItemMeta.hasDisplayName() &&
                flyOptions.contains(mainHandItemMeta.getDisplayName());
    }

    private void placeInvisibleBlockUnderPlayer(Player player) {
        deletePreviousBlocks();
        Location playerLocation = player.getLocation();
        double playerX = playerLocation.getX();
        double playerZ = playerLocation.getZ();

        // Determine the block position under the player
        int blockX = (int) Math.floor(playerX);
        int blockZ = (int) Math.floor(playerZ);

        // Check if the player is close to the edge on X axis
        boolean isCloseToEdgeX = Math.abs(playerX - blockX) >= 0.6 || Math.abs(playerX - blockX) <= 0.4;

        // Check if the player is close to the edge on Z axis
        boolean isCloseToEdgeZ = Math.abs(playerZ - blockZ) >= 0.6 || Math.abs(playerZ - blockZ) <= 0.4;

        // Place the first invisible block under the player if it's air
        placeIfAir(player.getWorld().getBlockAt(blockX, playerLocation.getBlockY() - 1, blockZ));

        // If the player is close to the edge, place additional invisible blocks
        if (isCloseToEdgeX) {
            placeIfAir(player.getWorld().getBlockAt(blockX + (playerX >= blockX + 0.5 ? 1 : -1), playerLocation.getBlockY() - 1, blockZ));
        }
        if (isCloseToEdgeZ) {
            placeIfAir(player.getWorld().getBlockAt(blockX, playerLocation.getBlockY() - 1, blockZ + (playerZ >= blockZ + 0.5 ? 1 : -1)));
        }

        // Check if both X and Z edges are close, then place the fourth block
        if (isCloseToEdgeX && isCloseToEdgeZ) {
            placeIfAir(player.getWorld().getBlockAt(blockX + (playerX >= blockX + 0.5 ? 1 : -1),
                    playerLocation.getBlockY() - 1, blockZ + (playerZ >= blockZ + 0.5 ? 1 : -1)));
        }
    }



    // Method to place the block if the current block is air
    private void placeIfAir(Block block) {
        if (block.getType() == Material.AIR) {
            block.setType(Material.WHITE_STAINED_GLASS);
            previousBlocks.put(block.getLocation(), Material.AIR); // Store the previous block
        }
    }


    // Store the previous blocks before placing new ones
    private void storePreviousBlocks(Location playerLocation) {
        previousBlocks.put(playerLocation, playerLocation.getBlock().getType());
        previousBlocks.put(playerLocation.clone().add(1, 0, 0), playerLocation.clone().add(1, 0, 0).getBlock().getType());
        previousBlocks.put(playerLocation.clone().add(0, 0, 1), playerLocation.clone().add(0, 0, 1).getBlock().getType());
        previousBlocks.put(playerLocation.clone().add(1, 0, 1), playerLocation.clone().add(1, 0, 1).getBlock().getType());
    }

    // Method to replace the block if the current block is air
    private void replaceIfAir(Block block) {
        if (block != null && block.getType() == Material.AIR) {
            block.setType(Material.WHITE_STAINED_GLASS);
        }
    }

    // Method to restore the previous blocks
    private void deletePreviousBlocks() {
        for (Map.Entry<Location, Material> entry : previousBlocks.entrySet()) {
            entry.getKey().getBlock().setType(entry.getValue()); // Restore the previous block
        }
        previousBlocks.clear(); // Clear the map after restoration
    }
}
