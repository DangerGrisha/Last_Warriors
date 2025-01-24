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

public class BridgeControlListener implements Listener {
    private JavaPlugin plugin;
    private static final String BRIDGE_ITEM_NAME = "Bridge"; // Название предмета
    private static boolean isLastDieWas = false;
    public static int currentDistance = 5;

    public BridgeControlListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack currentItem = player.getInventory().getItem(event.getNewSlot()); // Предмет на новом слоте

        // Проверяем, зажат ли Shift
        if (player.isSneaking()) {
            int newSlot = event.getNewSlot();
            int oldSlot = event.getPreviousSlot();

            // Проверяем, находится ли краситель "Bridge" на шестом слоте
            ItemStack bridgeDye = player.getInventory().getItem(5); // 6-й слот, индекс 5
            if (bridgeDye != null && bridgeDye.getType() == Material.RED_DYE && hasBridgeName(bridgeDye)) {
                if ((newSlot == 3 && oldSlot == 5) || (newSlot == 4 && oldSlot == 5)) { // Уменьшение дистанции
                    if (currentDistance > 5) {
                        currentDistance -= 1;
                        player.setLevel(currentDistance);
                    } else {
                        player.sendMessage("Cannot decrease distance further, minimum is 5 blocks.");
                    }
                    event.setCancelled(true);
                } else if ((newSlot == 6 && oldSlot == 5) || (newSlot == 7 && oldSlot == 5)) { // Увеличение дистанции
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
