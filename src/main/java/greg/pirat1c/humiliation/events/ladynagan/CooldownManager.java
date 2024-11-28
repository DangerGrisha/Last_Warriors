package greg.pirat1c.humiliation.events.ladynagan;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

    private final Map<Player, Map<String, Long>> cooldowns = new HashMap<>(); // Cooldowns by player and ability
    private final Map<Player, Map<String, BukkitRunnable>> tasks = new HashMap<>(); // Tasks for visual updates
    private final Material[] GLASS_COLORS = {
            Material.RED_STAINED_GLASS_PANE,      // Red
            Material.ORANGE_STAINED_GLASS_PANE,   // Orange
            Material.YELLOW_STAINED_GLASS_PANE,   // Yellow
            Material.GREEN_STAINED_GLASS_PANE     // Green
    };
    public final JavaPlugin plugin;

    public CooldownManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the cooldown for a specific ability and updates the inventory slot with progress.
     *
     * @param player        The player to apply the cooldown to.
     * @param ability       The ability name (unique identifier).
     * @param slot          The inventory slot to update.
     * @param cooldownTime  The cooldown time in seconds.
     */
    public void startCooldown(Player player, String ability, int slot, long cooldownTime, boolean showUI) {
        // Initialize cooldown map for the player if not present
        cooldowns.putIfAbsent(player, new HashMap<>());
        tasks.putIfAbsent(player, new HashMap<>());

        if (cooldowns.get(player).containsKey(ability)) {
            return; // Ability is already on cooldown
        }

        // Calculate the end time for the cooldown
        long endTime = System.currentTimeMillis() + (cooldownTime * 1000);
        cooldowns.get(player).put(ability, endTime);

        if (showUI) {
            // Create a visual cooldown task
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    long remainingTime = (endTime - System.currentTimeMillis()) / 1000;
                    if (remainingTime <= 0) {
                        // Cooldown complete
                        cooldowns.get(player).remove(ability);
                        tasks.get(player).remove(ability);
                        this.cancel();
                        return;
                    }

                    // Update the glass pane color based on progress
                    double progress = (double) remainingTime / cooldownTime;
                    Material glassColor = getGlassColor(progress);

                    // Update the inventory slot with a glass pane
                    ItemStack glassPane = createCooldownGlass(remainingTime, glassColor, ability);
                    player.getInventory().setItem(slot, glassPane);
                }
            };

            task.runTaskTimer(plugin, 0L, 20L); // Update every second
            tasks.get(player).put(ability, task);
        }
    }


    /**
     * Checks if a specific ability's cooldown is complete for a player.
     *
     * @param player  The player to check.
     * @param ability The ability name (unique identifier).
     * @return True if the cooldown is done, false otherwise.
     */
    public boolean isCooldownComplete(Player player, String ability) {
        return !cooldowns.containsKey(player) || !cooldowns.get(player).containsKey(ability);
    }

    /**
     * Cancels the cooldown for a specific ability of a player, if any.
     *
     * @param player  The player to cancel the cooldown for.
     * @param ability The ability name (unique identifier).
     */
    public void cancelCooldown(Player player, String ability) {
        if (tasks.containsKey(player) && tasks.get(player).containsKey(ability)) {
            tasks.get(player).get(ability).cancel();
            tasks.get(player).remove(ability);
        }

        if (cooldowns.containsKey(player)) {
            cooldowns.get(player).remove(ability);
        }
    }

    /**
     * Gets the remaining cooldown time for a specific ability of a player.
     *
     * @param player  The player to check.
     * @param ability The ability name (unique identifier).
     * @return The remaining cooldown time in seconds, or 0 if none.
     */
    public long getRemainingCooldown(Player player, String ability) {
        if (!cooldowns.containsKey(player) || !cooldowns.get(player).containsKey(ability)) {
            return 0;
        }

        return Math.max((cooldowns.get(player).get(ability) - System.currentTimeMillis()) / 1000, 0);
    }

    /**
     * Creates a glass pane item to represent the cooldown.
     *
     * @param remainingTime The remaining cooldown time in seconds.
     * @param glassColor    The glass pane material to use.
     * @param ability       The ability name to display in the item's lore.
     * @return The glass pane item.
     */
    private ItemStack createCooldownGlass(long remainingTime, Material glassColor, String ability) {
        ItemStack glassPane = new ItemStack(glassColor);
        ItemMeta meta = glassPane.getItemMeta();

        meta.setDisplayName("§eCooldown: " + remainingTime + " seconds remaining");
        meta.setLore(java.util.Collections.singletonList("§6Ability: " + ability));
        glassPane.setItemMeta(meta);

        return glassPane;
    }

    /**
     * Determines the glass color based on the cooldown progress.
     *
     * @param progress The progress percentage (0.0 to 1.0).
     * @return The appropriate glass pane material.
     */
    private Material getGlassColor(double progress) {
        if (progress > 0.75) {
            return GLASS_COLORS[0]; // Red
        } else if (progress > 0.5) {
            return GLASS_COLORS[1]; // Orange
        } else if (progress > 0.25) {
            return GLASS_COLORS[2]; // Yellow
        } else {
            return GLASS_COLORS[3]; // Green
        }
    }
}
