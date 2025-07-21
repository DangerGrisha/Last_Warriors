package greg.pirat1c.humiliation.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveStarterKitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /givestarterkit <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[0]);
            return true;
        }

        giveStarterKitTo(target);
        sender.sendMessage("§aStarter kit given to §e" + target.getName());
        return true;
    }

    private void giveStarterKitTo(Player player) {
        // 🛡 Броня
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        // ⚔ Оружие и щит
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
        player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));

        // 🍞 Еда
        player.getInventory().addItem(new ItemStack(Material.BREAD, 64));

        // 🧱 Шерсть (серая)
        player.getInventory().addItem(new ItemStack(Material.GRAY_WOOL, 256));
        player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET, 1));

        player.sendMessage("§aYou have been given the starter kit!");
    }
}
