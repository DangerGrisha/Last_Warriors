package greg.pirat1c.humiliation.command;

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

        player.teleport(location);

        sender.sendMessage("You have been teleported to spawn");
        return false;
    }
}