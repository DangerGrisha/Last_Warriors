package greg.pirat1c.humiliation.events.ishigava;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import static greg.pirat1c.humiliation.events.ishigava.IshigavaConstants.*;

public class BridgeControlListener implements Listener {
    private JavaPlugin plugin;
    private static final String BRIDGE_ITEM_NAME = "Bridge"; // Item name
    private static boolean isLastDieWas = false;
    public static int currentDistance = 5;

    public BridgeControlListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack currentItem = player.getInventory().getItem(event.getNewSlot()); // Item in the new slot

        // Check if Shift is being held
        if (player.isSneaking()) {
            int newSlot = event.getNewSlot();
            int oldSlot = event.getPreviousSlot();

            // Check if the red dye "Bridge" is in the sixth slot
            ItemStack bridgeDye = player.getInventory().getItem(5); // 6th slot, index 5
            if (bridgeDye != null && bridgeDye.getType() == Material.RED_DYE && hasBridgeName(bridgeDye)) {
                if ((newSlot == 3 && oldSlot == 5) || (newSlot == 4 && oldSlot == 5)) { // Decrease distance
                    if (currentDistance > 5) {
                        currentDistance -= 1;
                        player.setLevel(currentDistance);
                    } else {
                        player.sendMessage("Cannot decrease distance further, minimum is 5 blocks.");
                    }
                    event.setCancelled(true);
                } else if ((newSlot == 6 && oldSlot == 5) || (newSlot == 7 && oldSlot == 5)) { // Increase distance
                    if (currentDistance < 20) {
                        currentDistance += 1;
                        player.setLevel(currentDistance);
                    } else {
                        player.sendMessage("Cannot increase distance further, maximum is 20 blocks.");
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean hasBridgeName(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("Bridge");
    }
}
