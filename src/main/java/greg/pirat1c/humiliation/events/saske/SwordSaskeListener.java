package greg.pirat1c.humiliation.events.saske;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SwordSaskeListener implements Listener {

    public static final String SLAVE_DIRT = "SLAVE_DIRT";
    private JavaPlugin plugin;
    public SwordSaskeListener(JavaPlugin plugin) {


        this.plugin = plugin;

    }
    @EventHandler
    public void validateAndExecuteEvent(PlayerInteractEvent event){
        if(isSwordEvent(event, event.getPlayer())) {
            executeEvent(event);
        }
    }

    private void executeEvent(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Location blockLocation = player.getLocation();

        pushPlayerBack(player);
        dealAreaDamage();
        pushEnemyBack();









    }

    private boolean pushPlayerBack(Player player) {
        Vector destDirection = player.getLocation().getDirection();

        double z = destDirection.getZ();
        double x = destDirection.getX();

        // in order to provide stable x,z direstion throw
        // we have to stabilize it by introducing a multiplier M such as
        // (x^2 + z^2) * M == 1
        double mult = Math.sqrt( 1 / (Math.pow(x, 2) + Math.pow(z, 2)));

        destDirection.setZ(z * -1 * mult);
        destDirection.setX(x * -1 * mult);
        destDirection.setY(0.5);
        player.setVelocity(destDirection);




        return true;
    }

    private boolean dealAreaDamage() {
        return false;
    }

    /**
     * optional operation, not supported yet
     * @return true if operation succeeds
     */
    private boolean pushEnemyBack() {
        return false;
    }

    /** validate if event s sasake sword event
     *
     * @param event
     * @param player
     * @return
     */
    private boolean isSwordEvent(PlayerInteractEvent event, Player player){
        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )  &&
                player.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD
                && SLAVE_DIRT.equals(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName());


    }






}
