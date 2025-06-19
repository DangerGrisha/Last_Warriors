package greg.pirat1c.humiliation.StaticInventory;

import greg.pirat1c.humiliation.StaticInventory.PlayerClass;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SetClassCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public SetClassCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cUsage: /setclass <LadyNagan|Swordsman|Sniper>");
            return true;
        }

        String inputTag = args[0];
        PlayerClass chosenClass = PlayerClass.fromTag(inputTag);

        if (chosenClass == null) {
            player.sendMessage("§cInvalid class. Available: LadyNagan, Swordsman, Sniper");
            return true;
        }


        NamespacedKey key = new NamespacedKey(plugin, "class_tag");
        player.getPersistentDataContainer().set(key, PersistentDataType.STRING, chosenClass.getTag());
        player.sendMessage("§aYour class has been set to §e" + chosenClass.getTag());

        player.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "class_tag"),
                PersistentDataType.STRING,
                chosenClass.getTag()
        );
        player.sendMessage("§aYour class has been set to §e" + chosenClass.getTag());


        return true;
    }
}
