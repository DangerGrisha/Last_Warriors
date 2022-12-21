package greg.pirat1c.humiliation.entity;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;


public class SpawnHome implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Location location = new Location(player.getWorld(), 60, 144, 164, 90, 0);

        //System.out.println("Position: " + player.getLocation());

        player.teleport(location);

        //System.out.println("Position after teleport: " + player.getLocation());
        return false;
    }
}