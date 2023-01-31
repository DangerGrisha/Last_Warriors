package greg.pirat1c.humiliation.command;

import greg.pirat1c.humiliation.entity.HomeInfo;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class HomeCommand implements CommandExecutor {

    private Map<UUID, HomeInfo> playerIdToHomeInfo;

    public HomeCommand(Map<UUID, HomeInfo> playerIdToHomeInfo) {
        this.playerIdToHomeInfo = playerIdToHomeInfo;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        Location playerLocation = playerIdToHomeInfo.get(player.getUniqueId()).getPlayerHome();;




        player.teleport(playerLocation);

        //System.out.println("Position after teleport: " + player.getLocation());
        sender.sendMessage("You have been teleported to your home");
        return false;
    }
}
