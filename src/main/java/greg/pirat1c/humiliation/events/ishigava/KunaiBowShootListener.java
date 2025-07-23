package greg.pirat1c.humiliation.events.ishigava;

import greg.pirat1c.humiliation.command.Ishigava.KunaiGive;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KunaiBowShootListener implements Listener {

    private final Map<UUID, Location> tetherTargets = new HashMap<>();
    private final Map<UUID, Integer> clickCounter = new HashMap<>();
    private final Map<UUID, Integer> originalSlotMap = new HashMap<>();
    private final Map<UUID, Arrow> activeArrowMap = new HashMap<>();
    private final Map<UUID, ArmorStand> activeStandMap = new HashMap<>();
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    // Карта: кто притянул кого (1 игрок -> 2 игрок)
    private final Map<UUID, UUID> attachedPlayers = new HashMap<>();
    private final Map<UUID, Player> hookedPlayerMap = new HashMap<>();




    private final JavaPlugin plugin;
    private static final double FORCE_MULTIPLIER = 2.1;

    public KunaiBowShootListener(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onKunaiBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack bow = event.getBow();
        if (!isKunaiBow(bow)) return;

        event.setCancelled(true);

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack dye = new ItemStack(Material.RED_DYE);
        ItemMeta meta = dye.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Tether"));
            dye.setItemMeta(meta);
        }
        player.getInventory().setItem(slot, dye);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);

        double power = player.getExp();
        if (power < 0.1) return;

        Location start = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(1.0));
        Vector velocity = player.getLocation().getDirection().normalize().multiply(power * FORCE_MULTIPLIER);

        Arrow arrow = player.getWorld().spawnArrow(start, velocity, (float) velocity.length(), 0f);
        arrow.setShooter(player);

        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

        ArmorStand stand = player.getWorld().spawn(start, ArmorStand.class);

        String tag = "uuid:" + player.getUniqueId();
        arrow.addScoreboardTag("kunai_arrow"); // добавить общий тег
        arrow.addScoreboardTag(tag);
        stand.addScoreboardTag(tag);

        //remember active slot
        UUID uuid = player.getUniqueId();

        originalSlotMap.put(uuid, slot);
        //armorstand create attributes
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSmall(true);
        stand.setSilent(true);
        stand.setCollidable(false);

        BukkitTask task = new BukkitRunnable() {
            boolean stuck = false;

            @Override
            public void run() {
                if (!arrow.isValid()) {
                    stand.remove();
                    cancel();
                    return;
                }

                if (arrow.isOnGround()) {
                    if (!stuck) {
                        stuck = true;
                        tetherTargets.put(uuid, arrow.getLocation().clone());
                    }
                    Location loc = arrow.getLocation().clone().add(0, 0.2, 0);
                    stand.teleport(loc);
                    return;
                }

                Vector dir = arrow.getVelocity().normalize();
                Location followLoc = arrow.getLocation().subtract(dir.multiply(0.5)).add(0, 0.1, 0);
                stand.teleport(followLoc);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        activeTasks.put(uuid, task);


    }

    private boolean isKunaiBow(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() &&
                Component.text("Kunai").equals(meta.displayName());
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!Component.text("Tether").equals(meta.displayName())) return;

        UUID uuid = player.getUniqueId();

        // ✅ Проверка спама
        int clicks = clickCounter.getOrDefault(uuid, 0);
        if (clicks >= 15) return;
        clickCounter.put(uuid, clicks + 1);

        // ✅ Таймер сброса счётчика (1 секунда)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Integer current = clickCounter.get(uuid);
            if (current != null && current > 0) {
                clickCounter.put(uuid, current - 1);
            }
        }, 20L);

        Material type = item.getType();

        // ✅ Красный → Притянуть себя к точке
        if (type == Material.RED_DYE) {
            if (!tetherTargets.containsKey(uuid)) return;
            Location target = tetherTargets.get(uuid).clone();

            Vector direction = target.toVector().subtract(player.getLocation().toVector()).normalize();
            Vector pull = direction.multiply(1.1 / 3.0); // скорость

            player.setVelocity(pull);
        }

        // ✅ Зелёный → Притянуть другого игрока к себе
        else if (type == Material.LIME_DYE) {
            Player target = hookedPlayerMap.get(uuid); // тот, кого притягиваем
            if (target == null || !target.isOnline() || target.isDead()) return;

            Location self = player.getLocation();
            boolean targetInAir = !target.isOnGround();

            double speed = targetInAir ? 0.8 : 0.4;
            Vector direction = self.toVector().subtract(target.getLocation().toVector()).normalize();
            Vector pull = direction.multiply(speed);

            target.setVelocity(pull);
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!originalSlotMap.containsKey(uuid)) return;

        int originalSlot = originalSlotMap.get(uuid);
        int newSlot = event.getNewSlot();

        if (newSlot != originalSlot) {
            // Вернуть Kunai в оригинальный слот
            player.getInventory().setItem(originalSlot, KunaiGive.getItem());

            // Удаляем все сущности с тегом uuid:<player>
            String tag = "uuid:" + uuid;

            for (Entity entity : player.getWorld().getEntities()) {
                if (!(entity instanceof Arrow || entity instanceof ArmorStand)) continue;
                if (entity.getScoreboardTags().contains(tag)) {
                    entity.remove();
                    System.out.println("[DEBUG] removed entity with scoreboard tag: " + tag);
                }
            }

            // Очистить все связанные мапы
            originalSlotMap.remove(uuid);
            tetherTargets.remove(uuid);
            clickCounter.remove(uuid);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player target)) return;

        UUID shooterUUID = shooter.getUniqueId();
        String tag = "uuid:" + shooterUUID;

        // ✅ Проверка: только kunai-стрела от этого игрока
        if (!arrow.getScoreboardTags().contains("kunai_arrow")) return;
        if (!arrow.getScoreboardTags().contains(tag)) return;

        // ✅ Привязать игроков
        hookedPlayerMap.put(shooterUUID, target);
        attachedPlayers.put(shooterUUID, target.getUniqueId());

        // ✅ Установить зелёный краситель
        int slot = originalSlotMap.getOrDefault(shooterUUID, shooter.getInventory().getHeldItemSlot());
        shooter.getInventory().setItem(slot, createDye(Material.LIME_DYE));

        // ✅ Арморстенд
        ArmorStand stand = findStandByShooter(shooter.getWorld(), shooterUUID);
        if (stand == null) return;

        BukkitTask oldTask = activeTasks.get(shooterUUID);
        if (oldTask != null) oldTask.cancel();

        // ✅ Новый таск слежения
        BukkitTask newTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || target.getGameMode() == GameMode.SPECTATOR ||
                        !shooter.isOnline() || shooter.getGameMode() == GameMode.SPECTATOR) {

                    stand.remove();
                    // Вернуть Kunai в оригинальный слот
                    int slot = originalSlotMap.getOrDefault(shooterUUID, shooter.getInventory().getHeldItemSlot());
                    shooter.getInventory().setItem(slot, KunaiGive.getItem());
                    originalSlotMap.remove(shooterUUID);
                    tetherTargets.remove(shooterUUID);
                    clickCounter.remove(shooterUUID);
                    attachedPlayers.remove(shooterUUID);
                    hookedPlayerMap.remove(shooterUUID);

                    stand.remove();
                    cancel();
                    return;

                }

                // Арморстенд следует за целью
                Location follow = target.getLocation().clone().add(0, 0.2, 0);
                stand.teleport(follow);

                // ✅ Проверка полёта
                boolean shooterInAir = shooter.getLocation().subtract(0, 0.1, 0).getBlock().getType().isAir();
                Material dyeMaterial = shooterInAir ? Material.RED_DYE : Material.LIME_DYE;

                ItemStack current = shooter.getInventory().getItem(slot);
                if (current == null || current.getType() != dyeMaterial || !current.hasItemMeta()) {
                    shooter.getInventory().setItem(slot, createDye(dyeMaterial));
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        activeTasks.put(shooterUUID, newTask);
    }
    private ArmorStand findStandByShooter(World world, UUID shooterUUID) {
        String tag = "uuid:" + shooterUUID;

        for (Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
            if (entity.getScoreboardTags().contains(tag)) {
                return (ArmorStand) entity;
            }
        }

        return null;
    }

    private ItemStack createDye(Material color) {
        ItemStack dye = new ItemStack(color);
        ItemMeta meta = dye.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Tether"));
            dye.setItemMeta(meta);
        }
        return dye;
    }

}
