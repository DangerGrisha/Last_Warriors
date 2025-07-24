package greg.pirat1c.humiliation.events.ishigava;

import greg.pirat1c.humiliation.command.Ishigava.KunaiGive;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
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

        double power = player.getExp();
        if (power < 0.1) return;

        ItemStack bow = event.getBow();
        if (!isKunaiBow(bow)) {
            return;
        }

        event.setCancelled(true);
        if (event.getProjectile() != null) {
            event.getProjectile().remove(); // удалим стрелу, которую мог заспавнить Minecraft
        }
        UUID uuid = player.getUniqueId();
        if (activeArrowMap.containsKey(player.getUniqueId())) {
            return; // уже активный выстрел
        }
        if (activeStandMap.containsKey(uuid)) {
            return;
        }


        int slot = player.getInventory().getHeldItemSlot();
        ItemStack tether = createTetherItemWithSwordAttributes(Material.RED_DYE);
        player.getInventory().setItem(slot, tether);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);


        Location start = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(1.0));
        Vector velocity = player.getLocation().getDirection().normalize().multiply(power * FORCE_MULTIPLIER);

        Arrow arrow = player.getWorld().spawnArrow(start, velocity, (float) velocity.length(), 0f);
        arrow.setShooter(player);

        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        activeArrowMap.put(uuid, arrow);


        ArmorStand stand = player.getWorld().spawn(start, ArmorStand.class);

        String tag = "uuid:" + player.getUniqueId();
        arrow.addScoreboardTag("kunai_arrow"); // добавить общий тег
        arrow.addScoreboardTag(tag);
        stand.addScoreboardTag(tag); // 👈 Добавляем тег к стенду!


        //remember active slot
        activeStandMap.put(uuid, stand);

        originalSlotMap.put(uuid, slot);
        //armorstand create attributes
        stand.setVisible(false);
        stand.setMarker(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setSmall(true);
        stand.setSilent(true);
        stand.setCollidable(false);

        final Location origin = start.clone(); // ← начальная точка

        BukkitTask task = new BukkitRunnable() {
            boolean stuck = false;

            @Override
            public void run() {
                if (!arrow.isValid()) {
                    cleanup();
                    return;
                }

                // ✅ Прерывание, если расстояние > 15 блоков
                if (arrow.isOnGround()) {
                    if (arrow.getLocation().distance(player.getLocation()) > 15) {
                        cleanup();
                        return;
                    }

                    if (!stuck) {
                        stuck = true;
                        tetherTargets.put(uuid, stand.getLocation().clone());
                    }
                    Location loc = arrow.getLocation().clone().add(0, 0.2, 0);
                    stand.teleport(loc);
                    return;
                }

                if (arrow.isOnGround()) {
                    if (!stuck) {
                        stuck = true;
                        tetherTargets.put(uuid, stand.getLocation().clone());
                    }
                    Location loc = arrow.getLocation().clone().add(0, 0.2, 0);
                    stand.teleport(loc);
                    return;
                }

                Vector dir = arrow.getVelocity().normalize();
                Location followLoc = arrow.getLocation().subtract(dir.multiply(0.5)).add(0, 0.1, 0);
                stand.teleport(followLoc);
            }

            private void cleanup() {
                arrow.remove();
                stand.remove();

                // Вернуть предмет Kunai в оригинальный слот
                int slot = originalSlotMap.getOrDefault(uuid, -1);
                if (slot >= 0) {
                    player.getInventory().setItem(slot, KunaiGive.getItem());
                }

                activeArrowMap.remove(uuid);
                activeStandMap.remove(uuid);
                originalSlotMap.remove(uuid);
                tetherTargets.remove(uuid);
                clickCounter.remove(uuid);
                attachedPlayers.remove(uuid);
                hookedPlayerMap.remove(uuid);
                cancel();
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

        updateOffhandKunai(player);

        if (newSlot != originalSlot) {
            // Вернуть Kunai в оригинальный слот
            player.getInventory().setItem(originalSlot, KunaiGive.getItem());

            // Удаляем все сущности с тегом uuid:<player>
            String tag = "uuid:" + uuid;

            for (Entity entity : player.getWorld().getEntities()) {
                if (!(entity instanceof Arrow || entity instanceof ArmorStand)) continue;
                if (entity.getScoreboardTags().contains(tag)) {
                    entity.remove();
                }
            }

            Arrow arrow = activeArrowMap.remove(uuid);
            if (arrow != null && arrow.isValid()) arrow.remove();

            // Очистить все связанные мапы
            originalSlotMap.remove(uuid);
            tetherTargets.remove(uuid);
            clickCounter.remove(uuid);


            //shooter.getInventory().setItem(slot, KunaiGive.getItem());

            attachedPlayers.remove(uuid);
            hookedPlayerMap.remove(uuid);
            activeStandMap.remove(uuid); // ❗ если больше не нужен
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player target)) return;

        UUID shooterUUID = shooter.getUniqueId();
        String tag = "uuid:" + shooterUUID;

        if (!arrow.getScoreboardTags().contains("kunai_arrow")) return;
        if (!arrow.getScoreboardTags().contains(tag)) return;

        // ❗ Удаляем фантомные stuck стрелы у цели
        for (Entity e : target.getWorld().getNearbyEntities(target.getLocation(), 1, 2, 1)) {
            if (e instanceof Arrow otherArrow && e != arrow) {
                if (otherArrow.getScoreboardTags().contains("kunai_arrow")) {
                    otherArrow.remove();
                }
            }
        }

        // ✅ Привязать игроков
        hookedPlayerMap.put(shooterUUID, target);
        attachedPlayers.put(shooterUUID, target.getUniqueId());
        tetherTargets.put(shooterUUID, target.getLocation().clone().add(0, 0.2, 0));

        // ✅ Следим за здоровьем
        double[] initialHealth = {target.getHealth()};
        double[] thresholdHealth = {Math.max(initialHealth[0] - 10.0, 0)};


        // ✅ Установить зелёный краситель
        int slot = originalSlotMap.getOrDefault(shooterUUID, shooter.getInventory().getHeldItemSlot());
        shooter.getInventory().setItem(slot, createDye(Material.LIME_DYE));

        // ✅ Арморстенд
        ArmorStand stand = activeStandMap.get(shooterUUID);
        System.out.println(stand + "  stand");
        if (stand == null) return;

        // Завершаем предыдущий таск по стрелке
        BukkitTask oldTask = activeTasks.get(shooterUUID);
        if (oldTask != null) {
            oldTask.cancel();
            activeTasks.remove(shooterUUID);
        }

        // Удалим стрелу и уберём из map
        Arrow oldArrow = activeArrowMap.remove(shooterUUID);
        if (oldArrow != null && oldArrow.isValid()) {
            oldArrow.remove();
        }


        // ✅ Новый таск слежения
        BukkitTask newTask = new BukkitRunnable() {
            int airTicks = 0;

            @Override
            public void run() {
                if (!target.isOnline() || target.getGameMode() == GameMode.SPECTATOR ||
                        !shooter.isOnline()) {
                    cleanupAndCancel();
                    shooter.getInventory().setItem(slot, KunaiGive.getItem());
                    return;
                }
                // ❗ Здоровье
                double currentHealth = target.getHealth();
                if (currentHealth > initialHealth[0]) {
                    initialHealth[0] = currentHealth;
                    thresholdHealth[0] = Math.min(currentHealth - 10.0, 20.0);
                }

                if (currentHealth <= thresholdHealth[0]) {
                    shooter.sendMessage(Component.text("Target broke the tether by losing too much health."));
                    shooter.getInventory().setItem(slot, KunaiGive.getItem());
                    cleanupAndCancel();
                    return;
                }

                // ✅ Проверка расстояния между shooter и target
                if (shooter.getLocation().distance(target.getLocation()) > 15) {
                    cleanupAndCancel();
                    shooter.getInventory().setItem(slot, KunaiGive.getItem());
                    return;
                }

                // ✅ Проверка полёта цели
                if (!target.isOnGround()) {
                    airTicks++;
                    if (airTicks >= 60) {
                        cleanupAndCancel();
                        shooter.getInventory().setItem(slot, KunaiGive.getItem());
                        return;
                    }
                } else {
                    airTicks = 0;
                }

                // ✅ Проверка смены слота — если игрок больше не держит предмет в слоте, прервать
                int currentSlot = shooter.getInventory().getHeldItemSlot();
                int expectedSlot = originalSlotMap.getOrDefault(shooterUUID, -1);
                if (currentSlot != expectedSlot) {
                    cleanupAndCancel();
                    shooter.getInventory().setItem(slot, KunaiGive.getItem());
                    return;
                }

                // Арморстенд следует за целью
                Location follow = target.getLocation().clone().add(0, 0.2, 0);
                tetherTargets.put(shooterUUID, follow.clone());

                stand.teleport(follow);

                // ✅ Проверка шифта
                Material dyeMaterial = shooter.isSneaking() ? Material.LIME_DYE : Material.RED_DYE;
                ItemStack current = shooter.getInventory().getItem(expectedSlot);
                if (current == null || current.getType() != dyeMaterial || !current.hasItemMeta()) {
                    shooter.getInventory().setItem(expectedSlot, createTetherItemWithSwordAttributes(dyeMaterial));
                }
            }

            private void cleanupAndCancel() {
                stand.remove();
                int slot = originalSlotMap.getOrDefault(shooterUUID, shooter.getInventory().getHeldItemSlot());

                originalSlotMap.remove(shooterUUID);
                tetherTargets.remove(shooterUUID);
                clickCounter.remove(shooterUUID);
                attachedPlayers.remove(shooterUUID);
                hookedPlayerMap.remove(shooterUUID);
                activeStandMap.remove(shooterUUID);
                target.damage(4.0);
                cancel();
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

    private void updateOffhandKunai(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        if (isKunaiBow(main)) {
            // Если во второй руке ничего нет — выдать деревянный меч "Kunai"
            if (off.getType() == Material.AIR) {
                ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
                ItemMeta meta = sword.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text("Kunai"));
                    sword.setItemMeta(meta);
                }
                player.getInventory().setItemInOffHand(sword);
            }
        } else {
            // Если во второй руке Kunai-меч, а из главной убрали лук — удалить меч
            if (off.getType() == Material.WOODEN_SWORD) {
                ItemMeta meta = off.getItemMeta();
                if (meta != null && Component.text("Kunai").equals(meta.displayName())) {
                    player.getInventory().setItemInOffHand(null);
                }
            }
        }
    }


    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        // Ждём 1 тик, чтобы предмет уже обновился
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateOffhandKunai(player), 1L);
    }


    public ItemStack createTetherItemWithSwordAttributes(Material dyeColor) {
        ItemStack dye = new ItemStack(dyeColor);
        ItemMeta meta = dye.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Tether"));

            // +5 attack damage
            AttributeModifier damageModifier = new AttributeModifier(
                    UUID.randomUUID(),
                    "generic.attack_damage",
                    5.0,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);

            // -2.4 attack speed
            AttributeModifier speedModifier = new AttributeModifier(
                    UUID.randomUUID(),
                    "generic.attack_speed",
                    -2.4,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedModifier);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // чтобы скрыть цифры в описании, если нужно

            dye.setItemMeta(meta);
        }

        return dye;
    }
}

