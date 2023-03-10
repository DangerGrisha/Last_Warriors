package greg.pirat1c.humiliation.events.saske;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.sql.SQLOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.*;

public class AttractionListener implements Listener {


    private JavaPlugin plugin;
    public AttractionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Проверка, что нажата правая кнопка мыши
       Player player = event.getPlayer();
        if (checkEvent(event, player)) {
            //Block clickedBlock = event.getClickedBlock();
            // Получение координат блока
            //Location blockLocation = clickedBlock.getLocation();
            //Location blockLocation = player.getLocation();
            // Получение всех игроков в радиусе 10 блоков от блока
            //List<Player> playersInRange = blockLocation.getWorld().getPlayers().stream()
                  //  .filter(player1 -> player1.getLocation().distance(blockLocation) <= 10)
                    //.collect(Collectors.toList());
            int range = 5;
            int height = 5;
            Location location = player.getLocation();


            for (Entity entity :  player.getNearbyEntities(range, height, range)) {


                Vector destDirection2 = entity.getLocation().getDirection();
                Vector destDirection = player.getLocation().getDirection();


                double x2 = entity.getLocation().getX();
                double y2 = entity.getLocation().getY();
                double z2 = entity.getLocation().getZ();


                double x = player.getLocation().getX();
                double y = player.getLocation().getY();
                double z = player.getLocation().getZ();
                System.out.println("X:" + x + "Y:" + y + "Z:"+ z);
                System.out.println("X2:" + x2 + "Y2:" + y2 + "Z2:"+ z2);


                System.out.println("block Location:" + player);


                double modifiedX = x - x2;
                double modifiedY = y - y2;
                double modifiedZ = z - z2;

                destDirection2.setX(modifiedX/2);
                destDirection2.setY(modifiedY/2);
                destDirection2.setZ(modifiedZ/2);




                //Vector direction = new Vector(modifiedX, modifiedY, modifiedZ);
                System.out.println("modX:" + modifiedX + "modY:" + modifiedY + "modZ:"+ modifiedZ);
                entity.setVelocity(destDirection2);
               // playersInRange.forEach(player1 -> player1.setVelocity(direction));
            }

            System.out.println("VLAD gay");

        }

    }



    private boolean checkEvent(PlayerInteractEvent event, Player player){
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.INK_SAC &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("Attraction");


    }
}


