package greg.pirat1c.humiliation.events.ishigava;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.List;

public class WaterBridgesListener implements Listener {

    private JavaPlugin plugin;

    public WaterBridgesListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.RED_DYE && item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("Bridge") &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            int distanceMultiplier = player.getLevel(); // Получаем уровень XP как множитель дистанции
            //int baseDistance = 5; // Базовое расстояние
            int totalDistance = distanceMultiplier; // Итоговое расстояние для создания моста

            Vector direction = player.getEyeLocation().getDirection();
            Location spawnLocation = player.getLocation().add(direction.multiply(totalDistance)); // Используем итоговое расстояние

            if (!isNearPlayersOrBridges(spawnLocation, player, 3)) {
                spawnBridge(spawnLocation, player);
            } else {
                player.sendMessage("Cannot spawn a bridge here");
            }
        }
    }


    private void spawnBridge(Location location, Player player) {
        World world = player.getWorld();
        ArmorStand armorStand = world.spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setMetadata("bridge", new FixedMetadataValue(plugin, true));
        });

        // Создаем пол вокруг ArmorStand, заменяя только воздух
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLocation = location.clone().add(x, 1, z); // Пол на 1 уровень ниже ArmorStand
                if (blockLocation.getBlock().getType() == Material.AIR) { // Проверка, что текущий блок - воздух
                    blockLocation.getBlock().setType(Material.LAPIS_BLOCK);
                }
            }
        }

        // Задача на удаление моста через 20 секунд
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location blockLocation = location.clone().add(x, 1, z);
                    if (blockLocation.getBlock().getType() == Material.LAPIS_BLOCK) { // Проверка перед удалением
                        blockLocation.getBlock().setType(Material.AIR);
                    }
                }
            }
            armorStand.remove();
        }, 400L); // 400 тиков = 20 секунд
    }

    private boolean isNearPlayersOrBridges(Location location, Player placer, int radius) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(placer)) {
                return true; // Found another player (not the placer) within the radius
            }
            if (entity instanceof ArmorStand && entity.hasMetadata("bridge")) {
                return true; // Found another bridge within the radius
            }
        }
        return false; // No players or bridges are too close
    }




}
