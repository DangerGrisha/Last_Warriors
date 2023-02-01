package greg.pirat1c.humiliation.events.saske;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
        generateParticles(player);
        dealAreaDamage();
        pushEnemyBack();









    }

    private boolean pushPlayerBack(Player player) {
        Vector destDirection = player.getLocation().getDirection();

        double z = destDirection.getZ();
        double x = destDirection.getX();
        double y = destDirection.getY();

        // according scientifically proven formula
        // the push should be HARDER once the player looking forward : y == 0
        // however, the push will be the same strength if player looking completely downwards : y == -1
        // therefore the formula for the player y direction vector push would be : f( Y ) = -y/2 + 1/2;
        double modifiedY = (y - 1.0) / 2.0;

        destDirection.setZ(z * -1);
        destDirection.setX(x * -1);
        destDirection.setY(modifiedY * -1);
        player.setVelocity(destDirection);





        return true;
    }

    private boolean generateParticles(Player player){
        RayTraceResult rayTraceResult = player.getWorld().rayTrace(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                4,
                FluidCollisionMode.ALWAYS,
                true,
                1.0,
                null
        );
        if (rayTraceResult != null) {

            System.out.println(rayTraceResult.getHitBlock());
            System.out.println(rayTraceResult.getHitEntity());
            System.out.println(rayTraceResult.getHitBlockFace());
            System.out.println(rayTraceResult.getHitPosition());

            BlockFace blockFace = rayTraceResult.getHitBlockFace();
            boolean isBlock = rayTraceResult.getHitBlock() != null;

            Location hitLocation = isBlock
                    ? rayTraceResult.getHitBlock().getLocation()
                    : rayTraceResult.getHitEntity().getLocation();

            switch (blockFace) {
                case UP:hitLocation.setY(hitLocation.getY() + 1.0);
                case DOWN:hitLocation.setY(hitLocation.getY() - 1.0);
                case NORTH:hitLocation.setZ(hitLocation.getZ() - 1.0);
                case SOUTH:hitLocation.setZ(hitLocation.getZ() + 1.0);
                case EAST:hitLocation.setX(hitLocation.getX() + 1.0);
                case WEST:hitLocation.setX(hitLocation.getX() - 1.0);
            }

            player.getLocation().getWorld().spawnParticle(Particle.SWEEP_ATTACK, hitLocation, 1 );
        } else {
            System.out.println("empty result");
        }
        System.out.println("\n");
        return false;
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
