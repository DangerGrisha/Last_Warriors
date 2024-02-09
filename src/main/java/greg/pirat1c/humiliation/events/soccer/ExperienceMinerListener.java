package greg.pirat1c.humiliation.events.soccer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExperienceMinerListener implements Listener {

    private static final int STARTING_LEVEL = 100;
    private static final int POWER_PER_TICK = 5;
    private Map<UUID, BukkitRunnable> tasks = new HashMap<>();
    private JavaPlugin plugin;

    public ExperienceMinerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                player.getInventory().getItemInMainHand().getType() == Material.DIAMOND) {

            System.out.println(player.getLevel() + " -> boi");
            UUID playerUUID = player.getUniqueId();

            if (!tasks.containsKey(playerUUID)) {

                BukkitRunnable runnable = new BukkitRunnable() {
                    private int accumulatedExp = 0;

                    @Override
                    public void run() {

                        if (player.isOnline() && player.getLevel() == STARTING_LEVEL) {

                            accumulatedExp += POWER_PER_TICK;
                            player.giveExp(POWER_PER_TICK);
                        } else {

                            cancel();
                            tasks.remove(playerUUID);
                            player.setExp(0);
                            player.setLevel(STARTING_LEVEL);
                        }
                    }
                };
                tasks.put(playerUUID, runnable);
                runnable.runTaskTimer( plugin, 0, 1L);

            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        System.out.println("some bastard sit here");
        player.setLevel(STARTING_LEVEL);
    }

    private void kickBall(Player player){
        Location playerLocation = player.getLocation();
        double kickRadius = 2.0;

        System.out.println("Right click released");
        for (Entity entity : player.getNearbyEntities(kickRadius, kickRadius, kickRadius)) {
            if (entity.getType() == EntityType.SLIME) {
                System.out.println("Entity slime");
                Vector direction = playerLocation.getDirection();
                int playerExp = player.getTotalExperience();
                double forceMultiplier = 1 + playerExp / 100.0;
                double force = 2.0 * forceMultiplier;
                Vector kickVector = direction.multiply(force);
                kickVector.setY(0.5);
                entity.setVelocity(kickVector);
            }
        }
    }
}