package greg.pirat1c.humiliation.events;

import greg.pirat1c.humiliation.HumiliationPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import static org.bukkit.Bukkit.getServer;

public class FloorIce implements Listener {

    private JavaPlugin plugin;
    public FloorIce(JavaPlugin plugin) {


            this.plugin = plugin;

    }
    @EventHandler
    public void iceFloor(PlayerInteractEvent event){
        Player p = event.getPlayer();
        Location blockLocation = p.getLocation();

        double x = blockLocation.getBlockX();
        double y = blockLocation.getBlockY();
        double z = blockLocation.getBlockZ();
        double dx,dy,dz;
        //Location lox = p.getLocation();
        if(p.getInventory().getItemInMainHand().getType() == Material.LIGHT_BLUE_DYE && event.getAction() == Action.RIGHT_CLICK_AIR){


           // PlaceIce(p,0,-1,0);
            //  [] [] []
            //for(int i = -1; i <= 1 ; i++){
                //PlaceIce(p,x,y,z,i,-1,1);}

            //for(int i = -1; i <= 1 ; i++){
                //PlaceIce(p,x,y,z,i,-1,0);}

            //for(int i = -1; i <= 1 ; i++){
                //PlaceIce(p,x,y,z,i,-1,-1);}

            for(int d = -1; d <= 1; d++){

                for(int i = -1; i <= 1 ; i++){
                placeIce(p,x,y,z,i,-1,d);}

            }
            BukkitScheduler scheduler = getServer().getScheduler();

        }





    }
    private void placeIce(Player p , double x, double y, double z, double dx, double dy, double dz)  {
        x += dx;
        y += dy;
        z += dz;
        Location Loc = new Location(p.getWorld(),x,y,z);
        Loc.getBlock().setType(Material.ICE);



        getServer().getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
                @Override
                 public void run() {
                    // BrokeIce(Loc);}
                    brokeIce(Loc);
                }
            },100L );





        }





    private static void brokeIce(Location Loc) {

        Loc.getBlock().setType(Material.AIR);


    }

}
