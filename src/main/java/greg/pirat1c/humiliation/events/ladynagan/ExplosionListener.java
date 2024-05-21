package greg.pirat1c.humiliation.events.ladynagan;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ExplosionListener responsible for lady's explosion ability
 * player can set up herself to explode on ability activation
 * ability has cooldown
 */
public class ExplosionListener implements Listener {
    private final JavaPlugin plugin;
    private final String tagCheck = LadyConstants.LADY_TAG;
    private final String dyeName = "Self-Destruction";
    private final Long delayPerk = 400L;

    public ExplosionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // Check if the player has the green dye named "Self-Destruction" in their inventory
        if (hasSelfDestructionItem(player)) {
            player.getWorld().createExplosion(player.getLocation(), 4.0f, true, true);

            // Replace the green dye with a yellow dye in the player's inventory
            replaceGreenDyeWithYellow(player);

            // Schedule the dye change back to red after 20 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Check if the player still has the yellow dye
                    if (player.getInventory().contains(Material.YELLOW_DYE)) {
                        replaceYellowDyeWithRed(player);
                    }
                }
            }.runTaskLater(plugin, delayPerk); // 20 ticks per second, so 20 seconds is 20 * 20 ticks
        }
    }

    /**
     * Setting ability to explode yourself to cooldown
     * @param player
     */
    private void replaceGreenDyeWithYellow(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.GREEN_DYE && item.hasItemMeta() &&
                    item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(dyeName)) {
                // Replace the green dye with a yellow dye
                player.getInventory().setItem(i, DyeUtil.createYellowDye(dyeName));
                break;
            }
        }
    }
    private void replaceYellowDyeWithRed(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.YELLOW_DYE && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(dyeName)) {
                // Replace the yellow dye with a red dye
                player.getInventory().setItem(i, DyeUtil.createRedDye(dyeName));
                break;
            }
        }
    }

    private boolean isInteracted = false;
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if ((player.getScoreboardTags().contains(tagCheck) && checkEventForRightClick(event, player)) && !isInteracted) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.RED_DYE || itemInHand.getType() == Material.GREEN_DYE) {
                // we are making a delay to prevent a bug with fast reuse
                isInteracted = true;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        isInteracted = false;
                    }
                }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds
                // Switch red dye to green and vice versa
                if (itemInHand.getType() == Material.RED_DYE) {
                    itemInHand.setType(Material.GREEN_DYE);
                    itemInHand.setItemMeta((ItemMeta) DyeUtil.createGreenDye(dyeName));
                } else {
                    itemInHand.setType(Material.RED_DYE);
                    itemInHand.setItemMeta((ItemMeta) DyeUtil.createRedDye(dyeName));
                }
                // Update the player's inventory with the modified item
                player.getInventory().setItemInMainHand(itemInHand);
            }
        }
    }

    private boolean hasSelfDestructionItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.GREEN_DYE && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(dyeName)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains(dyeName);
    }
}
