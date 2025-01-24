package greg.pirat1c.humiliation.events.ishigava;

import org.bukkit.Location;
import org.bukkit.Material;
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

public class WaterShieldListener implements Listener {

    private JavaPlugin plugin;

    public WaterShieldListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (checkEventForRightClick(event, player)) {
            if (player.isSneaking()) {
                // Спавнит полную структуру щита
                spawnShieldStructure(player, plugin);
            } else {
                // Спавнит только основной ArmorStand ниже на 4 блока и с другим названием
                // Спавнит только основной ArmorStand ниже на 4 блока и с другим названием
                Vector direction = player.getEyeLocation().getDirection();
                Location spawnLocation = player.getLocation().add(direction.multiply(4)).add(0, 1, 0);
                spawnSingleMovingShield(spawnLocation, plugin, direction);
            }
        }
    }

    private void spawnSingleMovingShield(Location location, JavaPlugin plugin, Vector direction) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setInvulnerable(true);
            stand.setRightArmPose(new EulerAngle(0, 0, 0));
            stand.setCanPickupItems(false);
            stand.setMarker(true);
            stand.setMetadata("water_shield_move", new FixedMetadataValue(plugin, true));

            ItemStack limeDye = new ItemStack(Material.LIME_DYE);
            ItemMeta dyeMeta = limeDye.getItemMeta();
            dyeMeta.setDisplayName("WaterShieldMove");
            limeDye.setItemMeta(dyeMeta);
            stand.getEquipment().setItemInMainHand(limeDye);

            // Двигать ArmorStand вперед каждые 5 тиков
            moveArmorStand(stand, direction, plugin);

            // Удалить через 20 секунд
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, stand::remove, 400L);
        });
    }

    private void moveArmorStand(ArmorStand stand, Vector direction, JavaPlugin plugin) {
        new BukkitRunnable() {
            public void run() {
                if (!stand.isValid()) {
                    this.cancel();
                    return;
                }
                stand.teleport(stand.getLocation().add(direction.clone().multiply(0.06)));
            }
        }.runTaskTimer(plugin, 0L, 2L); // Запуск задачи каждые 5 тиков (примерно 0.25 секунды)
    }

    private void spawnArmorStand(Location location, boolean isMain, JavaPlugin plugin) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setInvulnerable(true);
            stand.setRightArmPose(new EulerAngle(0, 0, 0));
            stand.setCanPickupItems(false); // Запретить подбирать предметы
            stand.setMarker(false); // Уменьшить размер хитбокса if true
            stand.setMetadata("water_shield", new FixedMetadataValue(plugin, true)); // Добавление метаданны

            if (isMain) {
                ItemStack limeDye = new ItemStack(Material.LIME_DYE);
                ItemMeta dyeMeta = limeDye.getItemMeta();
                dyeMeta.setDisplayName("WaterShield");
                limeDye.setItemMeta(dyeMeta);
                stand.getEquipment().setItemInMainHand(limeDye);
            }

            // Запретить игрокам взаимодействие с `ArmorStand`
            //stand.addEquipmentLock(EquipmentSlot.H); // Запретить взаимодействие с предметом в руке

            // Удалить через 20 секунд
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, stand::remove, 400L);
        });
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        Location hitLocation = projectile.getLocation();
        //System.out.println("got a hit (in WaterShieldListener)");
        // Получаем близлежащие сущности в оптимизированном радиусе
        List<Entity> nearbyEntities = (List<Entity>) hitLocation.getWorld().getNearbyEntities(hitLocation, 1, 1, 1, entity ->
                (entity instanceof ArmorStand && entity.hasMetadata("water_shield")));

        for (Entity entity : nearbyEntities) {
            ArmorStand armorStand = (ArmorStand) entity;
            if (armorStand.getLocation().distance(hitLocation) <= 1) { // Уменьшаем радиус до 1 для повышения точности
                if (projectile.getType() == EntityType.SNOWBALL || projectile.getType() == EntityType.EGG || projectile.getType() == EntityType.ARROW || projectile.getType() == EntityType.SPECTRAL_ARROW) {
                    projectile.remove();
                    break;
                }
            }
        }
    }


    private void spawnShieldStructure(Player player, JavaPlugin plugin) {
        Vector forward = player.getEyeLocation().getDirection().normalize();
        Vector right = perpendicular(forward);

        Location baseLocation = player.getLocation().add(forward).add(0, player.getEyeHeight() - 1, 0); // Центрирование на уровне глаз

        // Основной ArmorStand
        spawnArmorStand(baseLocation, true, plugin);
        spawnArmorStand(baseLocation.clone().add(0, 2, 0), false, plugin); // Добавление ArmorStand над основным

        // Спавн второстепенных ArmorStands и добавление дополнительных ArmorStands над ними
        double[] distances = {0.5, 1.0, 1.5}; // Расстояния для второстепенных ArmorStands
        for (double dist : distances) {
            // Справа
            Location rightLocation = baseLocation.clone().add(right.clone().multiply(dist));
            spawnArmorStand(rightLocation, false, plugin);
            spawnArmorStand(rightLocation.clone().add(0, 2, 0), false, plugin); // Добавление ArmorStand над второстепенным

            Location rightExtra = baseLocation.clone().add(right.clone().multiply(-dist));
            spawnArmorStand(rightExtra, false, plugin);
            spawnArmorStand(rightExtra.clone().add(0, 2, 0), false, plugin); // Добавление ArmorStand над второстепенным

            // Слева
            Location leftLocation = baseLocation.clone().add(right.clone().multiply(-dist));
            spawnArmorStand(leftLocation, false, plugin);
            spawnArmorStand(leftLocation.clone().add(0, 2, 0), false, plugin); // Добавление ArmorStand над второстепенным

            Location leftExtra = baseLocation.clone().add(right.clone().multiply(dist));
            spawnArmorStand(leftExtra, false, plugin);
            spawnArmorStand(leftExtra.clone().add(0, 2, 0), false, plugin); // Добавление ArmorStand над второстепенным
        }
    }

    private Vector perpendicular(Vector direction) {
        return new Vector(-direction.getZ(), 0, direction.getX()).normalize();
    }



    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Quick_Wall");
    }
}
