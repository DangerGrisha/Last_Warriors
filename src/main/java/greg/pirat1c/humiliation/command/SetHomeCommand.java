package greg.pirat1c.humiliation.command;

import greg.pirat1c.humiliation.entity.HomeInfo;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class SetHomeCommand implements CommandExecutor {
    private Map<UUID, HomeInfo> playerIdToHomeInfo;

    public SetHomeCommand(Map<UUID, HomeInfo> playerIdToHomeInfo) {
        this.playerIdToHomeInfo = playerIdToHomeInfo;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Location currentXYZ = player.getLocation();
        UUID pID = player.getUniqueId();
        playerIdToHomeInfo.put(pID,new HomeInfo(currentXYZ));




        return false;
    }
}
