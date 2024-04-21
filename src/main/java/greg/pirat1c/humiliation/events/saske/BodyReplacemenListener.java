package greg.pirat1c.humiliation.events.saske;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class BodyReplacemenListener implements Listener {

    private JavaPlugin plugin;

    public BodyReplacemenListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (checkEvent(event)) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            Bukkit.getScheduler().runTaskLater(plugin, () -> useAbility(player), 20L);

        }
    }

    private boolean checkEvent(PlayerInteractEvent event) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                event.getPlayer().getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Body Replacement");
    }

    private void useAbility(Player player) {
        Location location = player.getEyeLocation();
        Vector direction = location.getDirection().normalize();
        double maxDistance = 20;

        RayTraceResult result = player.getWorld().rayTraceEntities(location, direction, maxDistance, entity -> entity != player);

        if (result != null && result.getHitEntity() instanceof LivingEntity) {
            LivingEntity targetEntity = (LivingEntity) result.getHitEntity();
            double distance = player.getLocation().distance(targetEntity.getLocation());
            RayTraceResult checkBlocks = player.rayTraceBlocks(distance);
            if (checkBlocks == null) {
                if (targetEntity.getLocation().distanceSquared(location) <= maxDistance * maxDistance) {
                    Location playerLocation = player.getLocation();
                    Location targetLocation = targetEntity.getLocation();

                    targetEntity.teleport(playerLocation);
                    player.teleport(targetLocation);


                    spawnSmokeParticles(playerLocation);
                    spawnSmokeParticles(targetLocation);
                }
            }
        }
    }

    private void spawnSmokeParticles(Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 50, 0.5, 0.5, 0.5);
    }
}
