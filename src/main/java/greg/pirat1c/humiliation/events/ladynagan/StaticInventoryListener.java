package greg.pirat1c.humiliation.events.ladynagan;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class StaticInventoryListener implements Listener {
    // Define the static slots
    private static final int[] STATIC_SLOTS = {0, 8}; // Example: Slot 0 and Slot 8
    public final JavaPlugin plugin;

    public StaticInventoryListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Get the inventory holder
        InventoryHolder holder = event.getInventory().getHolder();

        // Check if the holder is a player
        if (!(holder instanceof Player)) {
            return;
        }

        // Get the clicked slot
        int clickedSlot = event.getSlot();

        // Check for hotbar swapping action
        if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            // Get the hotbar slot being swapped
            int hotbarSlot = event.getHotbarButton(); // Returns the hotbar slot (0-8)
            for (int staticSlot : STATIC_SLOTS) {
                if (hotbarSlot == staticSlot || clickedSlot == staticSlot) {
                    // Cancel the event to prevent modification
                    event.setCancelled(true);
                    ((Player) holder).sendMessage("You cannot modify this slot!");
                    return;
                }
            }
        }

        // Check if the clicked slot is in the static slots array for normal clicks
        for (int staticSlot : STATIC_SLOTS) {
            if (clickedSlot == staticSlot) {
                // Cancel the event to prevent modification
                event.setCancelled(true);
                ((Player) holder).sendMessage("You cannot modify this slot!");
                return;
            }
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        // Get the player who triggered the event
        Player player = event.getPlayer();

        // Get the slot of the main-hand item (Minecraft does not expose slot ID, but you can identify items)
        if (isStaticSlot(player, player.getInventory().getHeldItemSlot())) {
            // Cancel the event to prevent swapping to off-hand
            event.setCancelled(true);
            player.sendMessage("You cannot swap items from a static slot!");
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        // Get the player who triggered the event
        Player player = event.getPlayer();

        // Check if the item being dropped is from a static slot
        int heldSlot = player.getInventory().getHeldItemSlot(); // Current held slot
        if (isStaticSlot(player, heldSlot)) {
            // Cancel the drop event
            event.setCancelled(true);
            player.sendMessage("You cannot drop items from a static slot!");
        }
    }

    private boolean isStaticSlot(Player player, int slot) {
        for (int staticSlot : STATIC_SLOTS) {
            if (slot == staticSlot) {
                return true;
            }
        }
        return false;
    }
}
