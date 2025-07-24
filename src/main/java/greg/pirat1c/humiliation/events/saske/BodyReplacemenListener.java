package greg.pirat1c.humiliation.events.saske;

import greg.pirat1c.humiliation.command.SaskeBodyReplacement;
import greg.pirat1c.humiliation.events.ladynagan.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static greg.pirat1c.humiliation.events.saske.SaskeConstants.*;

public class BodyReplacemenListener implements Listener {

    private JavaPlugin plugin;
    private final CooldownManager cooldownManager;

    public BodyReplacemenListener(JavaPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (checkEvent(event)) {


            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            int slot = player.getInventory().getHeldItemSlot();
            // Play thunder sound for 3 seconds
            World world = player.getWorld();
            ItemStack originalItem = player.getInventory().getItemInMainHand().clone();


            world.playSound(player.getLocation(), "saske.katon", SoundCategory.MASTER, 1.0F, 1.0F);



            Bukkit.getScheduler().runTaskLater(plugin, () -> useAbility(player), SPEACH_BEFORE_REPLACEMENT);



            // Запускаем КД: слот 0 (или какой у тебя в инвентаре у предмета подмены)
            cooldownManager.startCooldown(player, NAME_OF_REPLACEMENT, player.getInventory().getHeldItemSlot(), 70, true);

            // Вернуть предмет вручную после кулдауна
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;

                    // Только если стекло всё ещё там (не было заменено вручную)
                    ItemStack current = player.getInventory().getItem(BODYREPLACEMENT_SLOT);
                    if (current != null && current.getType().toString().contains("GLASS")) {
                        player.getInventory().setItem(slot, originalItem);
                    }
                }
            }.runTaskLater(plugin, 20L * 70); // 50 секунд


        }
    }

    private boolean checkEvent(PlayerInteractEvent event) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                event.getPlayer().getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(NAME_OF_REPLACEMENT);
    }

    private void useAbility(Player player) {
        Location location = player.getEyeLocation();
        Vector direction = location.getDirection().normalize();

        RayTraceResult result = player.getWorld().rayTraceEntities(location, direction, DISTANCE_OF_TRIGGERING, entity -> entity != player);

        if (result != null && result.getHitEntity() instanceof LivingEntity) {
            LivingEntity targetEntity = (LivingEntity) result.getHitEntity();
            double distance = player.getLocation().distance(targetEntity.getLocation());
            RayTraceResult checkBlocks = player.rayTraceBlocks(distance);
            if (checkBlocks == null) {
                if (targetEntity.getLocation().distanceSquared(location) <= DISTANCE_OF_TRIGGERING * DISTANCE_OF_TRIGGERING) {
                    Location playerLocation = player.getLocation();
                    Location targetLocation = targetEntity.getLocation();

                    // 💥 Drop flags from both players via command
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dropFlag " + player.getName());
                    if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType().name().contains("BANNER")) {
                        player.getInventory().setHelmet(null);
                    }

                    if (targetEntity instanceof Player targetPlayer) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dropFlag " + targetPlayer.getName());
                        if (targetPlayer.getInventory().getHelmet() != null && targetPlayer.getInventory().getHelmet().getType().name().contains("BANNER")) {
                            targetPlayer.getInventory().setHelmet(null);
                        }
                    }

                    targetEntity.teleport(playerLocation);
                    player.teleport(targetLocation);


                    spawnSmokeParticles(playerLocation);
                    spawnSmokeParticles(targetLocation);
                }
            }
        }else {
            player.stopSound("saske.katon", SoundCategory.MASTER);
        }
    }

    private void spawnSmokeParticles(Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 50, 0.5, 0.5, 0.5);
    }
}
