package greg.pirat1c.humiliation.StaticInventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class StaticInventoryListener implements Listener {
    private final JavaPlugin plugin;
    private final PlayerClassManager classManager;

    public StaticInventoryListener(JavaPlugin plugin, PlayerClassManager classManager) {
        this.plugin = plugin;
        this.classManager = classManager;
    }

    private boolean isStaticSlot(Player player, int slot) {
        PlayerClass playerClass = classManager.getClassFromPlayer(player);
        if (playerClass == null) return false;

        return playerClass.getStaticSlots().contains(slot);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Проверка на swap через цифру (горячая клавиша)
        if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            int hotbarButton = event.getHotbarButton(); // 0-8
            if (isStaticSlot(player, hotbarButton)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot hotkey-swap with a static slot!");
                return;
            }
        }

        // Проверка обычного клика в инвентаре
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            int slot = event.getSlot();
            if (isStaticSlot(player, slot)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot modify this slot!");
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        for (int slot : event.getRawSlots()) {
            if (slot < player.getInventory().getSize() && isStaticSlot(player, slot)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot drag items into a static slot!");
                break;
            }
        }
    }



    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();
        if (isStaticSlot(player, slot)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot drop items from a static slot!");
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();
        if (isStaticSlot(player, slot)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot swap items from a static slot!");
        }
    }
}

