package greg.pirat1c.humiliation.events.fukuko;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import static greg.pirat1c.humiliation.events.fukuko.FukukoConstants.*;
import static greg.pirat1c.humiliation.events.ladynagan.LadyConstants.SET_UP_BLOCK;
import static greg.pirat1c.humiliation.events.ladynagan.LadyConstants.TRAP_NAME;

public class MortiraListener implements Listener {

    private final JavaPlugin plugin;
    private final Team blueTeam;
    private final Team redTeam;
    private final int radius = 30;

    public MortiraListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.blueTeam = getTeam("BLUE");
        this.redTeam = getTeam("RED");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND &&
                    player.getInventory().getItemInMainHand().getType() == MATERIAL_OF_MORTIRA_FUKUKO &&
                    NAME_OF_MORTIRA_FUKUKO.equals(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName())) {

                final Block clickedBlock = event.getClickedBlock();
                final String playerTeam = getTeam(player);
                Location MortiraBlockLocation = clickedBlock.getLocation().add(0, 1, 0);

                placeMortira(event, player, MortiraBlockLocation);
                turnOnCycle(MortiraBlockLocation, playerTeam);
            }
        }

    }

    private void turnOnCycle(Location mortiraLocation, String ownerTeam) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mortiraLocation.getBlock().getType() != MATERIAL_OF_MORTIRA_FUKUKO) {
                    cancel(); // Stop the cycle if the BombZone is gone
                    return;
                }

                Player target = findTarget(mortiraLocation, radius, ownerTeam);

                if (target != null) {
                    Location spawnLocation = mortiraLocation.clone().add(0, 1, 0); // Clone to avoid modifying original
                    ArmorStand projectile = spawnProjectile(spawnLocation);
                    launchProjectile(projectile, target.getLocation(), ownerTeam);
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Runs every 5 seconds
    }

    private Player findTarget(Location location, int radius, String ownerTeam) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                String playerTeam = getTeam(player);
                if (!playerTeam.equals(ownerTeam)) { // Only target players from the opposite team
                    return player;
                }
            }
        }
        return null;
    }

    private ArmorStand spawnProjectile(Location location) {
        ArmorStand projectile = location.getWorld().spawn(location.add(0, 1, 0), ArmorStand.class);
        projectile.setVisible(true);
        projectile.setGravity(false);
        projectile.setSmall(true);
        projectile.setCustomName("projectile");
        projectile.setCustomNameVisible(false);
        projectile.setInvulnerable(true);
        return projectile;
    }

    private void launchProjectile(ArmorStand projectile, Location targetLocation, String ownerTeam) {
        Location start = projectile.getLocation();
        Vector direction = targetLocation.toVector().subtract(start.toVector()).normalize();

        double distance = start.distance(targetLocation);
        double speed = Math.min(1.5, distance / 80);

        direction.multiply(speed);
        direction.setY(1.5);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 100 || projectile.isDead()) {
                    projectile.remove();
                    cancel();
                    return;
                }

                Location currentLocation = projectile.getLocation();
                Block blockBelow = currentLocation.clone().add(0, -1, 0).getBlock();
                if (blockBelow.getType() != Material.AIR && blockBelow.getType() != MATERIAL_OF_MORTIRA_FUKUKO) {
                    explodeProjectile(projectile);
                    cancel();
                    return;
                }

                projectile.teleport(currentLocation.add(direction));
                direction.setY(direction.getY() - 0.05);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void explodeProjectile(ArmorStand projectile) {
        Location explosionLocation = projectile.getLocation();
        projectile.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, explosionLocation, 1);
        projectile.getWorld().playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Apply damage to all nearby entities
        double explosionRadius = 4.0;
        for (Entity entity : projectile.getWorld().getNearbyEntities(explosionLocation, explosionRadius, explosionRadius, explosionRadius)) {
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).damage(10.0); // Deal 10 damage to all entities
            }
        }

        projectile.remove();
    }





    private void placeMortira(PlayerInteractEvent event, Player player, Location BlockLocation){

            if (!isNearPlayersOrArmorStands(BlockLocation, player, 2)) {
                ArmorStand armorStand = summonArmorStand(BlockLocation);
                placeBlockBack(BlockLocation.getBlock());
            }else{
                player.sendRawMessage("Can't place here");
            }
    }
    // Method to place block back cause minecraft default logic after placing armorstand so fast -> removing current block
    private void placeBlockBack(Block block) {
        block.setType(Material.AIR);
        new BukkitRunnable() {
            @Override
            public void run() {
                    block.setType(MATERIAL_OF_MORTIRA_FUKUKO);
            }
        }.runTaskLater(plugin, 0L);
    }
    // Method to replace a block with an armor stand
    public ArmorStand summonArmorStand(Location location) {

        //log("in replaceBlockWithArmorStand");
        ArmorStand armorStand = location.getWorld().spawn(location.add(0.5, 0, 0.5), ArmorStand.class);
        armorStand.setVisible(true);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        //armorStand.setInvulnerable(false);
        armorStand.setBasePlate(false);
        armorStand.setInvulnerable(true);
        //if we add true in marker , armorstand will be Invulnerable
        //armorStand.setMarker(false);
        armorStand.setCustomName(NAME_OF_MORTIRA_FUKUKO);
        armorStand.setCustomNameVisible(false);
        armorStand.setArms(true);
        armorStand.setSmall(false);

        ItemStack redDye = new ItemStack(Material.BLUE_DYE);
        ItemMeta dyeMeta = redDye.getItemMeta();
        dyeMeta.setDisplayName("MortiraArm");
        redDye.setItemMeta(dyeMeta);
        armorStand.getEquipment().setItemInMainHand(redDye);

        //armorStandItems.put(armorStand, redDye); // Store the armor stand and the item it's holding

        return armorStand;
    }

    private boolean isNearPlayersOrArmorStands(Location location, Player placer, int radius) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(placer)) {
                return true; // Found a player (not the placer) within the radius
            }
            if (entity instanceof ArmorStand) {
                return true; // Found a mine (armor stand) within the radius
            }
        }
        return false;
    }

    private String getTeam(Player player) {
        // Get the player's team using some method (for demonstration purposes)
        Team playerTeam = getPlayerTeam(player);

        // Check the player's team
        if (playerTeam != null) {
            return playerTeam.getName(); // Return the player's team in uppercase
        } else {
            // If the player's team is not defined or cannot be determined, return a default team
            return "DEFAULT";
        }
    }
    // Method to retrieve the existing team from the scoreboard
    private Team getTeam(String name) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        return scoreboard.getTeam(name);
    }

    private Team getPlayerTeam(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                return team;
            }
        }
        return null; // Player is not on any team
    }

}
