package greg.pirat1c.humiliation.events.saske;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SwordTest extends JavaPlugin implements Listener {

    private JavaPlugin plugin;
    public SwordTest(JavaPlugin plugin) {


        this.plugin = plugin;

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (isSwordEvent(event,player)) {
            strikeTriangle(player);
        }
    }

    private void strikeTriangle(Player player) {
        Location playerLoc = player.getLocation();
        double triangleHeight = 3;
        double triangleBase = 3;

        for (Entity entity : player.getNearbyEntities(triangleBase, triangleHeight, triangleBase)) {
            if (entity instanceof LivingEntity && isInTriangleZone(playerLoc, entity.getLocation(), triangleHeight, triangleBase)) {
                ((LivingEntity) entity).damage(5, player);
            }
        }
    }

    private boolean isInTriangleZone(Location playerLoc, Location entityLoc, double triangleHeight, double triangleBase) {
        double distance = entityLoc.distance(playerLoc);
        double angle = playerLoc.toVector().angle(entityLoc.toVector());

        return distance <= triangleBase && angle <= Math.atan(triangleBase / triangleHeight);
    }
    private boolean isSwordEvent(PlayerInteractEvent event, Player player) {
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("katana saske");



    }
}

