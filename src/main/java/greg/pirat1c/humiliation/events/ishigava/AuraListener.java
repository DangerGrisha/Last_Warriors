package greg.pirat1c.humiliation.events.ishigava;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AuraListener implements Listener {

    private JavaPlugin plugin;
    private final Team blueTeam;
    private final Team redTeam;
    private final Set<Player> activePlayers = new HashSet<>();

    public AuraListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.blueTeam = getTeam("BLUE");
        this.redTeam = getTeam("RED");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the player right-clicked with the correct item
        if (isAuraActivator(event, player)) {
            startAuraEffect(player);
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (activePlayers.contains(player)) {
            cancelAuraEffect(player, "Ability canceled because you died.");
        }
    }

    private boolean isAuraActivator(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == org.bukkit.Material.RED_DYE &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                "AURA".equals(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName());
    }

    private void startAuraEffect(Player player) {
        // Register the player as using the ability
        activePlayers.add(player);

        // Play a sound to indicate the ability has been activated
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        Team playerTeam = getPlayerTeam(player);
        if (playerTeam == null) {
            activePlayers.remove(player);
            return;
        }

        // Use arrays to allow dynamic updates
        double[] initialHealth = {player.getHealth()}; // The player's health at the start
        double[] thresholdHealth = {Math.max(initialHealth[0] - 10, 0)}; // Initial threshold health

        // Create a task to manage the aura effect
        new BukkitRunnable() {
            final int durationTicks = 20 * 20; // 20 seconds
            final double radius = 8.0; // Radius of the sphere
            final Random random = new Random();
            final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 100, 100), 2.0F); // Dark cyan, bigger size
            int elapsedTicks = 0;

            @Override
            public void run() {
                if (!activePlayers.contains(player) || player.isDead()) {
                    cancelAuraEffect(player, "Ability canceled because you died.");
                    this.cancel();
                    return;
                }

                double currentHealth = player.getHealth();

                // Update thresholdHealth if player heals above initialHealth
                if (currentHealth > initialHealth[0]) {
                    initialHealth[0] = currentHealth; // Update initialHealth
                    thresholdHealth[0] = Math.min(currentHealth - 10, 20); // Threshold cannot exceed 20 HP
                }

                // Cancel the ability if health falls below thresholdHealth
                if (currentHealth <= thresholdHealth[0]) {
                    cancelAuraEffect(player, "Ability canceled because your health dropped below the threshold.");
                    this.cancel();
                    return;
                }

                if (elapsedTicks >= durationTicks) {
                    activePlayers.remove(player);
                    this.cancel(); // Stop after 20 seconds
                    return;
                }

                // Get the player's location
                Location center = player.getLocation().add(0, 1, 0); // Slightly above the ground
                Set<Player> affectedPlayers = new HashSet<>();

                // Check for players within the radius
                for (Player nearbyPlayer : center.getWorld().getPlayers()) {
                    if (!nearbyPlayer.equals(player) && // Exclude the activating player
                            nearbyPlayer.getLocation().distance(center) <= radius &&
                            playerTeam.hasEntry(nearbyPlayer.getName())) {
                        affectedPlayers.add(nearbyPlayer);
                    }
                }

                // Spawn random particles within the sphere
                for (int i = 0; i < 200; i++) { // Increase particle count for denser effect
                    double offsetX = (random.nextDouble() * 2 - 1) * radius; // Random X within -radius to +radius
                    double offsetY = (random.nextDouble() * 2 - 1) * radius; // Random Y within -radius to +radius
                    double offsetZ = (random.nextDouble() * 2 - 1) * radius; // Random Z within -radius to +radius

                    // Ensure the point is within the sphere
                    if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ <= radius * radius) {
                        Location particleLocation = center.clone().add(offsetX, offsetY, offsetZ);
                        player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
                    }
                }

                // Make affected players invisible for 2 seconds
                for (Player affectedPlayer : affectedPlayers) {
                    affectedPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.INVISIBILITY, 40, 0, false, false));
                }

                elapsedTicks += 20; // Increment by 1 second (20 ticks)
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }



    private void cancelAuraEffect(Player player, String reason) {
        if (activePlayers.contains(player)) {
            activePlayers.remove(player);
            player.sendMessage(reason);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }

    private Team getPlayerTeam(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                return team;
            }
        }
        return null;
    }

    private Team getTeam(String name) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        return scoreboard.getTeam(name);
    }
}
