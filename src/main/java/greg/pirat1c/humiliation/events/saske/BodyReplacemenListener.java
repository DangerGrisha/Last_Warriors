package greg.pirat1c.humiliation.events.saske;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;


import java.sql.SQLOutput;
import java.util.List;

import static org.bukkit.Bukkit.*;

public class BodyReplacemenListener implements Listener {


    private JavaPlugin plugin;
    public BodyReplacemenListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();


        if (checkEvent(event, player)) {


            Location location = player.getEyeLocation();
            Vector direction = location.getDirection().normalize();
            double maxDistance = 20;


                RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 20.0, 0, entity -> entity != player);



                if (result != null && (result.getHitBlock() == null) && (result.getHitEntity() instanceof LivingEntity || result.getHitEntity() instanceof Snowball || result.getHitEntity() instanceof Arrow) ) {
                    LivingEntity targetEntity = (LivingEntity) result.getHitEntity();
                    double distance = player.getLocation().distance(targetEntity.getLocation());
                    RayTraceResult checkblocks = player.rayTraceBlocks(distance);
                    if(checkblocks == null) {
                        if (targetEntity.getLocation().distanceSquared(location) <= maxDistance * maxDistance) {
                            Location playerLocation = player.getLocation();
                            Location targetLocation = targetEntity.getLocation();

                            targetEntity.teleport(playerLocation);
                            player.teleport(targetLocation);
                        }
                    }







                }
        }



    }
        /*if (item.getType() == Material.INK_SAC
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals("body replacement")
                && event.getAction() == Action.RIGHT_CLICK_AIR)

         */
    private boolean checkEvent(PlayerInteractEvent event, Player player){
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("body replacement");


    }
}