package greg.pirat1c.humiliation.events.saske;

import org.bukkit.*;
import org.bukkit.block.Block;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // Check if the click was with the right mouse button and the player is holding a Bedrock block with the "Attraction" name
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND &&
                player.getInventory().getItemInMainHand().getType() == Material.BEDROCK &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Attraction")) {
            // Check if the block in front of the player is empty
            Block blockToPlace = clickedBlock.getRelative(event.getBlockFace());
            if (blockToPlace.getType() == Material.AIR) {
                // Place the block
                blockToPlace.setType(Material.BEDROCK);
                Location loc = blockToPlace.getLocation();
                blockToPlace.setType(Material.AIR);
                replaceBlockWithArmorStand(loc);
                startAttraction(loc, player);
            }
        }
    }

    private void replaceBlockWithArmorStand(Location location) {
        ArmorStand armorStand = location.getWorld().spawn(location.add(0.5, 0, 0.5), ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false); // Prevent players from picking up items from the armor stand
        armorStand.setInvulnerable(true); // Make armor stand invulnerable
        armorStand.setBasePlate(false); // Hide base plate
        armorStand.setMarker(true); // Make armor stand a marker (invisible to players)
        armorStand.setCustomName("Attraction"); // Set custom name to identify the armor stand
        armorStand.setCustomNameVisible(false); // Make custom name invisible


        // Add arms
        armorStand.setArms(true);

        // Give red dye to the right hand
        ItemStack redDye = new ItemStack(Material.RED_DYE);
        ItemMeta dyeMeta = redDye.getItemMeta();
        dyeMeta.setDisplayName("Attraction");
        redDye.setItemMeta(dyeMeta);
        armorStand.getEquipment().setItemInMainHand(redDye);

    }

    private void startAttraction(Location location, Player placer) {
        new BukkitRunnable() {
            int ticks = 0;
            ArmorStand armorStand = null;
            Set<LivingEntity> damagedEntities = new HashSet<>(); // Хранит противников, которые уже получили урон

            @Override
            public void run() {
                ticks++;
                if (ticks >= 40) { // 10 seconds
                    if (armorStand != null && !armorStand.isDead()) {
                        armorStand.remove();
                    }
                    cancel();
                    return;
                }

                for (Entity entity : location.getWorld().getNearbyEntities(location, 7, 7, 7)) {
                    if (!(entity instanceof ArmorStand) && !(entity instanceof Player && isAlly((Player) entity, placer))) {
                        Vector direction = location.toVector().subtract(entity.getLocation().toVector()).normalize();
                        entity.setVelocity(direction.multiply(0.5)); // Attract entities

                        if (entity.getLocation().distanceSquared(location) <= 10 && entity instanceof LivingEntity) { // Within 10 blocks
                            LivingEntity livingEntity = (LivingEntity) entity;
                            if (!damagedEntities.contains(livingEntity)) {
                                livingEntity.damage(5); // Deal 1 damage
                                damagedEntities.add(livingEntity);
                            }
                        }
                    }
                }

                if (ticks == 1) {
                    armorStand = getArmorStand(location);
                }
            }
        }.runTaskTimer(plugin, 20L, 2L); // Start after 1 second and repeat every 0.1 second
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
