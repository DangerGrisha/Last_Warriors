package greg.pirat1c.humiliation.events.fukuko;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import static greg.pirat1c.humiliation.events.fukuko.FukukoConstants.NAME_OF_PISTOL_FUKUKO;
import static greg.pirat1c.humiliation.events.fukuko.FukukoConstants.NAME_OF_ULT_FUKUKO;

public class BombZoneListener implements Listener {

    private final JavaPlugin plugin;
    private final Team blueTeam;
    private final Team redTeam;
    private final int radius = 10;

    public BombZoneListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.blueTeam = getTeam("BLUE");
        this.redTeam = getTeam("RED");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block clickedBlock = event.getClickedBlock();
        final String playerTeam = getTeam(player);
        if (clickedBlock == null) return;
        Location bombZoneLocation = clickedBlock.getLocation().add(0,1,0);
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && checkEventForName(player)) {
            createParticleSquare(bombZoneLocation, radius);
            createCycleOfCheckPlayers(bombZoneLocation, radius, playerTeam);
            removeBombZoneAfterDelay(bombZoneLocation, 20);
            //System.out.println("meow1");
        }
        //System.out.println("meow2");
    }
    private void removeBombZoneAfterDelay(Location location, int seconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (location.getBlock().getType() == Material.REDSTONE_BLOCK) {
                    //System.out.println("Removing BombZone at " + location);
                    location.getBlock().setType(Material.AIR); // Remove the Redstone Block
                }
            }
        }.runTaskLater(plugin, seconds * 20L); // Converts seconds to Minecraft ticks
    }

    private boolean checkEventForName(Player player) {
        //System.out.println("meow3");
        return (player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains(NAME_OF_ULT_FUKUKO));
    }
    private void createCycleOfCheckPlayers(Location center, int radius, String ownerTeam) {
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = center.getWorld();
                if (world == null) {
                    //System.out.println("wrong meoww2w");
                    cancel();
                    return;
                }

                // Debugging: Print the block type before checking
               // System.out.println("Checking block at " + center + " | Found: " + center.getBlock().getType());

                // Check if the block is still a Redstone Block
                if (center.getBlock().getType() != Material.REDSTONE_BLOCK) {
                    //System.out.println("wrong meow1 (Block is not Redstone Block, instead found: " + center.getBlock().getType() + ")");
                    cancel(); // Stop the cycle if the BombZone is gone
                    return;
                }


                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld() != world) continue; // Ignore players in other worlds

                    Location playerLoc = player.getLocation();
                    double dx = Math.abs(playerLoc.getX() - center.getX());
                    double dz = Math.abs(playerLoc.getZ() - center.getZ());

                    // Check if player is inside the 20x20 zone
                    if (dx <= radius && dz <= radius) {
                        String targetTeam = getTeam(player);

                        if (!ownerTeam.equals(targetTeam)) { // Enemy detected!
                            //System.out.println("Enemy detected! Firing fireball at " + player.getName());

                            Location fireballLoc = playerLoc.clone().add(0, 10, 0);
                            Fireball fireball = (Fireball) world.spawnEntity(fireballLoc, EntityType.FIREBALL);

                            // Make the fireball fall straight down
                            fireball.setDirection(new Vector(0, -1, 0));
                            fireball.setVelocity(new Vector(0, -1, 0)); // Makes it go directly downward

                            // Make it impossible to redirect
                            fireball.setShooter(null); // Prevents players from reflecting it
                            fireball.setIsIncendiary(false); // No fire spread
                            fireball.setYield(0);
                        }

                    }
                }
            }
        }.runTaskTimer(plugin, 0, 50); // Runs every 2.5 seconds (50 ticks)
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

    private void createParticleSquare(Location center, int radius) {

       // System.out.println("meow4");
        World world = center.getWorld();
        if (world == null) return;

        int delay = 0; // Delay between particle updates

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the block is still there and is a Redstone Block
                if (center.getBlock().getType() != Material.REDSTONE_BLOCK) {
                    //System.out.println("wrong meow2");
                    cancel(); // Stop the cycle if the BombZone is gone
                    return;
                }
                // Loop through X and Z edges
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        // Only place particles on the edges
                        if (x == -radius || x == radius || z == -radius || z == radius) {
                            Location particleLocation = center.clone().add(x, 10, z);
                            world.spawnParticle(Particle.FALLING_DUST, particleLocation, 5, Material.RED_SAND.createBlockData());
                        }
                    }
                    //System.out.println("meowwww");
                }
            }
        }.runTaskTimer(plugin, delay, 20); // Runs every second
    }
}
