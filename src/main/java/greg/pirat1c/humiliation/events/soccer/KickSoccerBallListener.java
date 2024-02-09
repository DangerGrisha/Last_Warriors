package greg.pirat1c.humiliation.events.soccer;

import greg.pirat1c.humiliation.entity.MouseButton;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.sql.SQLOutput;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static greg.pirat1c.humiliation.entity.MouseButton.RIGHT_CLICK;

public class KickSoccerBallListener implements Listener {
    private JavaPlugin plugin;
    private Set<UUID> holdingRightClick = new HashSet<>();

    private TickCounter counter;
    public KickSoccerBallListener(JavaPlugin plugin) {
        this.plugin = plugin;
        counter = new TickCounter(plugin);
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (player.getInventory().getItemInMainHand().getType() == Material.DIAMOND) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                holdingRightClick.add(playerUUID);
            }
        }
        counter.start();
        System.out.println("interaction: -> " + counter.getCurrentTick());

        if (!RIGHT_CLICK.equalAction(event.getAction()) && holdingRightClick.contains(playerUUID)) {
            holdingRightClick.remove(playerUUID);
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



    public class TickCounter {
        private Integer tickCount = 0;
        private JavaPlugin plugin;

        private BukkitRunnable runnable;
        private boolean isStarted = false;

        public TickCounter (JavaPlugin plugin) {
            this.plugin = plugin;
        }

        public void start() {
            if (isStarted) {
                return;
            }
            isStarted = true;

            runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    tickCount++;
                }
            };

            runnable.runTaskTimer(plugin, 0, 1);

        }

        public int getCurrentTick() {
            return tickCount;
        }

        public void stop() {
            runnable.cancel();
            isStarted = false;
        }
    }
}