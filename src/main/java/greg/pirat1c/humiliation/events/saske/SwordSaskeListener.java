package greg.pirat1c.humiliation.events.saske;

import greg.pirat1c.humiliation.utils.tuple.Tuple;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Optional;

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

        pushPlayerBack(player);
        Optional<Tuple<BlockFace, Location>> hitOptional = generateParticles(player);
        // only if block was hit
        if (hitOptional.isPresent()) {
            Tuple<BlockFace, Location> hitLocation = hitOptional.get();
            playAudio(player);
            dealAreaDamage(player.getWorld(), hitLocation.getLeft(), hitLocation.getRight());
            pushEnemyBack();
            System.out.println(event);

        }


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

    private Optional<Tuple<BlockFace, Location>> generateParticles(Player player){
        RayTraceResult rayTraceResult = player.getWorld().rayTrace(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                4,
                FluidCollisionMode.ALWAYS,
                false,
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
            boolean isPlayer = player.equals(rayTraceResult.getHitEntity());

            Location hitLocation = isBlock
                    ? rayTraceResult.getHitBlock().getLocation()
                    : rayTraceResult.getHitEntity().getLocation();

            // in case if player hits just in the air, we show the particles 4 block in front of it
            double playerMultiplier = isPlayer ? 4.0 : 1.0;


            switch (blockFace) {
                case UP:hitLocation.setY(hitLocation.getY() + playerMultiplier);
                case DOWN:hitLocation.setY(hitLocation.getY() - playerMultiplier);
                case NORTH:hitLocation.setZ(hitLocation.getZ() - playerMultiplier);
                case SOUTH:hitLocation.setZ(hitLocation.getZ() + playerMultiplier);
                case EAST:hitLocation.setX(hitLocation.getX() + playerMultiplier);
                case WEST:hitLocation.setX(hitLocation.getX() - playerMultiplier);
            }

            System.out.println("particle is generated at: " + hitLocation);
            player.getLocation().getWorld().spawnParticle(Particle.SWEEP_ATTACK, hitLocation, 1 );
            return Optional.of(Tuple.of(blockFace, hitLocation));
        } else {
            System.out.println("empty result");
        }
        return Optional.empty();

    }

    /**
     * Divide all health by 2
     * @param world the world you are playing in
     * @param location the location of the damage
     * @return
     */
    private boolean dealAreaDamage(World world, BlockFace direction, Location location) {
        System.out.println("dealing area damage");



        boolean isZDirection = direction == BlockFace.NORTH || direction == BlockFace.SOUTH;

        Collection<Entity> entities = world.getNearbyEntities(location, isZDirection ? 1 : 4,isZDirection ? 4 : 1, 4);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Player anotherPlayer = (Player) entity;
                double totalHealth = anotherPlayer.getHealth();
                anotherPlayer.damage(totalHealth / 2);
            }
        }

        //for testing purposes

        //TODO: wrong calculatioons, the damage area should be perpendicular to the payer's vision ray

        Location bubbleLocation = location.clone();
        if (isZDirection) {
            bubbleLocation.setZ(bubbleLocation.getZ() - 2);
        } else {
            bubbleLocation.setX(bubbleLocation.getX() - 2);
        }
        for (int i = 0; i < 4; i++) {
            world.spawn(bubbleLocation, Chicken.class);
            if (isZDirection) {
                bubbleLocation.setZ(bubbleLocation.getZ() + 1);
            } else {
                bubbleLocation.setX(bubbleLocation.getX() + 1);
            }
        }
        return false;
    }

    /**
     * optional operation, not supported yet
     * @return true if operation succeeds
     */
    private boolean pushEnemyBack() {
        return false;
    }

    private boolean playAudio(Player player) {
        Sound sound = Sound.ENTITY_EVOKER_CELEBRATE;
        player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        return true;
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
