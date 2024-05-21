package greg.pirat1c.humiliation.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class SpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private static final EntityType [] entities = {EntityType.ZOMBIE,
            EntityType.ARROW, EntityType.SNOWMAN, EntityType.IRON_GOLEM, EntityType.BEE};

    public SpawnCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;




        int ammo = 1;
        if (args.length > 0) {
            System.out.println("ammo provided: " + args[0]);
            try {
                ammo = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.out.println(args[0] + " is not a number. " + nfe.getMessage());
            }
        }

        int frequency = 10;
        if (args.length > 1) {
            System.out.println("frequency provided: " + args[1]);
            try {
                frequency = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.out.println(args[1] + " is not a number. " + nfe.getMessage());
            }
        }

        zombieGun(player, ammo, frequency);

        return true;

    }

    private void zombieGun(Player player, int ammo, int frequency) throws ClassCastException {
        final double speed = 2.0;
        Vector staticVelocity = new Vector(2,0,2);
        BukkitScheduler scheduler = getServer().getScheduler();

        for (int i = 0; i < ammo; i++) {
            /*Runnable operation = new Runnable() {
                @Override
                public void run() {
                    shootOnce(mobik, player);
                }
            };*/

            scheduler.scheduleSyncDelayedTask(
                    plugin, () -> shootOnce(player), ((long) frequency * (i + 1)));
        }

    }
    private void shootOnce(Player player) {

        Location mobLocation = player.getLocation();
        mobLocation.add(player.getLocation().getDirection().multiply(3));

        Entity spawnedEntity = player.getWorld().spawnEntity(mobLocation, EntityType.values()[new Random().nextInt(EntityType.values().length)]);
        if (spawnedEntity instanceof Zombie) {
            Zombie mobik = (Zombie) spawnedEntity;
            mobik.setBaby();
        }

        Vector direction = player.getLocation().getDirection();
        direction.multiply(2);

        spawnedEntity.setVelocity(direction);
        spawnedEntity.setGlowing(true);
    }


}
