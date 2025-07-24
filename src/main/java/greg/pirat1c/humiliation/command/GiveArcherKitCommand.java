package greg.pirat1c.humiliation.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveArcherKitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /givearcherkit <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[0]);
            return true;
        }

        giveArcherKitTo(target);
        sender.sendMessage("§aArcher kit given to §e" + target.getName());
        return true;
    }

    private void giveArcherKitTo(Player player) {
        // 🪓 Деревянный меч (не ломается)
        ItemStack woodenSword = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta swordMeta = woodenSword.getItemMeta();
        swordMeta.setUnbreakable(true);
        woodenSword.setItemMeta(swordMeta);

        // 🏹 Лук с бесконечностью
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        bowMeta.setUnbreakable(true);
        bow.setItemMeta(bowMeta);

        // 🏹 Стрела
        ItemStack arrow = new ItemStack(Material.ARROW, 1);

        // 📦 Остальное
        ItemStack bread = new ItemStack(Material.BREAD, 64);
        ItemStack wool = new ItemStack(Material.GRAY_WOOL, 256);
        ItemStack water = new ItemStack(Material.WATER_BUCKET, 1);

        // 📥 Выдача
        player.getInventory().addItem(woodenSword, bow, arrow, bread, wool, water);

        // 🛡 Броня
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        player.sendMessage("§aYou have been given the archer kit!");
    }
}
