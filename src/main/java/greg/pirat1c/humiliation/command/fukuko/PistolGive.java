package greg.pirat1c.humiliation.command.fukuko;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

import static greg.pirat1c.humiliation.events.fukuko.FukukoConstants.NAME_OF_PISTOL_FUKUKO;

public class PistolGive implements CommandExecutor {
    private static final Material material = Material.CROSSBOW; // Change to CROSSBOW
    private static final String displayName = NAME_OF_PISTOL_FUKUKO;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return false;
        }

        Player player = (Player) sender;
        player.getInventory().addItem(getChargedCrossbow()); // Give charged crossbow

        return true;
    }

    public static ItemStack getChargedCrossbow() {
        ItemStack crossbow = new ItemStack(material, 1);
        CrossbowMeta meta = (CrossbowMeta) crossbow.getItemMeta(); // Cast to CrossbowMeta

        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setUnbreakable(true);
            meta.setLore(Collections.singletonList("A sniper's best friend"));

            // Load the crossbow with an arrow
            meta.addChargedProjectile(new ItemStack(Material.ARROW));

            crossbow.setItemMeta(meta);
        }
        return crossbow;
    }
}
