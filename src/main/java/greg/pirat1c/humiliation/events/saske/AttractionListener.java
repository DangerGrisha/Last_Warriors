package greg.pirat1c.humiliation.events.saske;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;


public class AttractionListener implements Listener {

    private final JavaPlugin plugin;

    public AttractionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    private void spawnParticlesAroundBlock(Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 50, 5, 5, 5);
    }


    private void startBlockRemoval(Block block) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == Material.BEDROCK) {
                    block.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND &&
                player.getInventory().getItemInMainHand().getType() == Material.BEDROCK &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Attraction")) {
            Block blockToPlace = clickedBlock.getRelative(event.getBlockFace());
            if (blockToPlace.getType() == Material.AIR) {
                spawnParticlesAroundBlock(blockToPlace.getLocation());
                blockToPlace.setType(Material.BEDROCK);
                Location loc = blockToPlace.getLocation();
                replaceBlockWithArmorStand(loc);
                startAttraction(loc, player);
                startBlockRemoval(blockToPlace);
            }
        }
    }

    public void replaceBlockWithArmorStand(Location location) {
        ArmorStand armorStand = location.getWorld().spawn(location.add(0.5, 0, 0.5), ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setInvulnerable(true);
        armorStand.setBasePlate(false);
        armorStand.setMarker(true);
        armorStand.setCustomName("Attraction");
        armorStand.setCustomNameVisible(false);
        armorStand.setArms(true);

        ItemStack redDye = new ItemStack(Material.RED_DYE);
        ItemMeta dyeMeta = redDye.getItemMeta();
        dyeMeta.setDisplayName("Attraction");
        redDye.setItemMeta(dyeMeta);
        armorStand.getEquipment().setItemInMainHand(redDye);
    }

    public void startAttraction(Location location, Player placer) {
        new BukkitRunnable() {
            int ticks = 0;
            ArmorStand armorStand = null;
            Set<LivingEntity> damagedEntities = new HashSet<>();

            @Override
            public void run() {
                ticks++;
                if (ticks >= 40) {
                    if (armorStand != null && !armorStand.isDead()) {
                        armorStand.remove();
                    }
                    cancel();
                    return;
                }

                for (Entity entity : location.getWorld().getNearbyEntities(location, 7, 7, 7)) {
                    if (!(entity instanceof ArmorStand) && !(entity instanceof Player && isAlly((Player) entity, placer))) {
                        Vector direction = location.toVector().subtract(entity.getLocation().toVector()).normalize();
                        entity.setVelocity(direction.multiply(0.5));

                        if (entity.getLocation().distanceSquared(location) <= 10 && entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity) entity;
                            if (!damagedEntities.contains(livingEntity)) {
                                livingEntity.damage(5);
                                damagedEntities.add(livingEntity);
                            }
                        }
                    }
                }

                if (ticks % 5 == 0) {
                    spawnParticlesAroundBlock(location);
                }

                if (ticks == 1) {
                    armorStand = getArmorStand(location);
                }
            }
        }.runTaskTimer(plugin, 20L, 2L);
    }




    private ArmorStand getArmorStand(Location location) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
            if (entity instanceof ArmorStand && entity.getCustomName() != null && entity.getCustomName().equals("Attraction")) {
                return (ArmorStand) entity;
            }
        }
        return null;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getBlock().getType() == Material.ARMOR_STAND) {
            ArmorStand armorStand = (ArmorStand) player.getLocation().getBlock().getState();
            if (!armorStand.isVisible() && !armorStand.hasGravity() && armorStand.isMarker()) {
                event.setCancelled(true); // Cancel player movement towards this armor stand
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand && event.getCause() == DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true); // Cancel any damage to the armor stand
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.BEDROCK && event.getItemInHand().getItemMeta().getDisplayName().equals("Attraction")) {
            event.getBlockPlaced().setType(Material.AIR); // Remove Bedrock immediately after placing
        }
        if (event.getBlockPlaced().getType() == Material.ARMOR_STAND) {
            event.setCancelled(true); // Cancel placing armor stands
        }
    }

    private boolean isAlly(Player player, Player placer) {
        // Check if the player is in the same team as the placer
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        Team placerTeam = placer.getScoreboard().getPlayerTeam(placer);
        Team playerTeam = player.getScoreboard().getPlayerTeam(player);
        return placerTeam != null && playerTeam != null && placerTeam.equals(playerTeam);
    }
}
