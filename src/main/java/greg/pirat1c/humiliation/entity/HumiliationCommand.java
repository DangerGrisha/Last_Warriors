package greg.pirat1c.humiliation.entity;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class HumiliationCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public HumiliationCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
    try {
        Location armorLocation = player.getLocation();
        armorLocation.add(new Vector(15, 15, 15));
        for (int i = 0; i < 10; i++) {
            player.getWorld().spawnEntity(armorLocation, EntityType.ARMOR_STAND);
            armorLocation.add(new Vector(15, 15 + i * 2, 15 + i * 10));
            Thread.sleep(300);
        }
    } catch (InterruptedException ex) {
        //ignore exception
    }
        return true;
    }
}
