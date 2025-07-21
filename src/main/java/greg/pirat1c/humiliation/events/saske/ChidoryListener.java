package greg.pirat1c.humiliation.events.saske;

import greg.pirat1c.humiliation.events.ladynagan.CooldownManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.List;

import static greg.pirat1c.humiliation.events.saske.SaskeConstants.*;

public class ChidoryListener implements Listener {

    private CooldownManager cooldownManager;
    private JavaPlugin plugin;


    public ChidoryListener(JavaPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();



        if (checkEvent(event, player)) {
            World world = player.getWorld();
            Location initialLocation = player.getLocation();
            Vector direction = initialLocation.getDirection();

            // Play thunder sound for 3 seconds
            world.playSound(initialLocation, "saske.chidory", 1.0F, 1.0F);

            // --- Заморозка игрока ---
            player.setWalkSpeed(0f);
            player.setVelocity(new Vector(0, 0, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) SPEACH_BEFORE_DASH, 255, false, false));

                // Create particle effect around the player

            particleStaff(player,world);

            int slot = player.getInventory().getHeldItemSlot();
            ItemStack originalItem = player.getInventory().getItemInMainHand().clone();
            String abilityId = "CHIDORY";

            // Запускаем визуальный кулдаун
            cooldownManager.startCooldown(player, abilityId, slot, 50, true);



            // Вернуть предмет вручную после кулдауна
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;

                    // Только если стекло всё ещё там (не было заменено вручную)
                    ItemStack current = player.getInventory().getItem(slot);
                    if (current != null && current.getType().toString().contains("GLASS")) {
                        player.getInventory().setItem(slot, originalItem);
                    }
                }
            }.runTaskLater(plugin, 20L * 50); // 50 секунд

            //After a second make a dash
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Вернём управление
                    player.setWalkSpeed(0.2f); // стандартная скорость
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) SPEACH_BEFORE_DASH, 255, false, false));

                    // Пуск частиц и рывок
                    Vector direction = player.getLocation().getDirection();
                    world.spawnParticle(Particle.SONIC_BOOM, player.getLocation(), 7, 0.5, 0.5, 0.5, 0);

                    Vector dashVelocity = direction.multiply(MULTIPLY_VELOCITY);
                    player.setVelocity(dashVelocity);

                    // Урон в пути
                    applyDamageInPlayer(player, player.getLocation(), dashVelocity);
                }
            }.runTaskLater(plugin, SPEACH_BEFORE_DASH);

        }
    }

    private void startChidoryCooldown(Player player, int slot, ItemStack originalItem) {
        final long cooldownSeconds = 50;
        final long endTime = System.currentTimeMillis() + cooldownSeconds * 1000L;

        // Ставим стекло
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.text("§7Чидори перезаряжается..."));
        glass.setItemMeta(meta);
        player.getInventory().setItem(slot, glass);

        new BukkitRunnable() {
            int timeLeft = (int) cooldownSeconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    // Возвращаем оригинальный предмет
                    player.getInventory().setItem(slot, originalItem);
                    cancel();
                    return;
                }

                // Обновим ActionBar каждую секунду
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // раз в секунду
    }


    private void applyDamageInPath(Player player, Location startLocation, Vector dashVelocity) {
        final int iterations = 20; // Number of points along the path to check
        final Vector normalizedVelocity = dashVelocity.clone().normalize();
        final double segmentLength = dashVelocity.length() / iterations;

        for (int i = 0; i < iterations; i++) {
            // Calculate the current point's location along the dash path
            Location pointLocation = startLocation.clone().add(normalizedVelocity.clone().multiply(segmentLength * i));
            // Dynamically adjust the detection radius maybe needed here
            double searchRadius = 1.5; // Consider adjusting based on speed or elevation change
            List<Entity> nearbyEntities = (List<Entity>) pointLocation.getWorld().getNearbyEntities(pointLocation, searchRadius, searchRadius, searchRadius);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(6.0, player); // Apply damage
                }
            }
        }
    }
    private void particleStaff(Player player, World world){
        new BukkitRunnable() {
            int iterations = 20;

            @Override
            public void run() {
                if (iterations-- <= 0) {
                    cancel();
                    return;
                }

                // Adjust particle position around the player
                Location particleLocation = player.getLocation().add(0, 1, 0);

                // Spawn particle effect
                world.spawnParticle(Particle.ELECTRIC_SPARK, particleLocation, 7, 0.5, 0.5, 0.5, 0);
            }
        }.runTaskTimer(plugin, 1L, 4L); // Runs every tick for 20 ticks (1 second)
    }
    private void applyDamageInPlayer(Player player, Location startLocation, Vector dashVelocity) {
        // Radius for searching entities inside the player
        final double searchRadius = 1.5;
        final int iterations = 20; // Number of repetitions
        final int delayBetweenIterations = 1; // Delay between iterations in ticks
        // Schedule the execution of the repeated damage application
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        BukkitTask task = scheduler.runTaskTimer(plugin, () -> {
            // Get a list of entities inside the player
            List<Entity> nearbyEntities = (List<Entity>) player.getNearbyEntities(searchRadius, searchRadius, searchRadius);

            // Apply damage to all entities inside the player
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    ((LivingEntity) entity).damage(19, player); // Apply damage
                }
            }
        }, 0L, delayBetweenIterations);

        // Cancel the task after the specified number of iterations
        scheduler.runTaskLater(plugin, task::cancel, iterations * delayBetweenIterations);
    }


    private boolean checkEvent(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(NAME_OF_CHIDORY);
    }
}