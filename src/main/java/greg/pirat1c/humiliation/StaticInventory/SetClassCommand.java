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
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage("§cUsage: /setclass <LadyNagan|Saske|Sniper> [player]");
            return true;
        }

        String inputTag = args[0];
        PlayerClass chosenClass = PlayerClass.fromTag(inputTag);

        if (chosenClass == null) {
            sender.sendMessage("§cInvalid class. Available: LadyNagan, Saske, Sniper");
            return true;
        }

        Player target;

        if (args.length == 2) {
            target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this without specifying a target.");
                return true;
            }
            target = (Player) sender;
        }

        // Удаляем старые теги
        for (PlayerClass pc : PlayerClass.values()) {
            target.removeScoreboardTag(pc.getTag());
        }

        // Добавляем новый тег
        target.addScoreboardTag(chosenClass.getTag());

        // Сохраняем в PersistentDataContainer
        NamespacedKey key = new NamespacedKey(plugin, "class_tag");
        target.getPersistentDataContainer().set(key, PersistentDataType.STRING, chosenClass.getTag());

        target.sendMessage("§aYour class has been set to §e" + chosenClass.getTag());
        if (!target.equals(sender)) {
            sender.sendMessage("§aSet class §e" + chosenClass.getTag() + "§a for §b" + target.getName());
        }

        return true;
    }

}
