package greg.pirat1c.humiliation.events.soccer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class SlimeSoccerListener implements Listener {
    private static final double KICK_POWER = 1.5;
    private static final double COLLISION_RADIUS = 1;

    private final JavaPlugin plugin;

    public SlimeSoccerListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        for (Entity entity : player.getNearbyEntities(COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS)) {
            if (entity.getType() == EntityType.SLIME) {
                Slime slime = (Slime) entity;

                double distance = player.getLocation().distance(slime.getLocation());
                if (distance < COLLISION_RADIUS) {
                    Vector kickDirection = slime.getLocation().subtract(player.getLocation()).toVector().normalize();
                    Vector kickVelocity = kickDirection.multiply(KICK_POWER);

                    slime.setVelocity(kickVelocity);
                }
            }
        }
    }
}