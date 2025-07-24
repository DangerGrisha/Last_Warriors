package greg.pirat1c.humiliation.command.ladynagan;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LadyNaganGive implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /giveladynagan <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[0]);
            return true;
        }

        giveKitTo(target);
        sender.sendMessage("§aLadyNagant kit given to " + target.getName());
        return true;
    }

    private void giveKitTo(Player player) {
        player.getInventory().setItem(0, SniperGive.getItem(player));
        player.getInventory().setItem(2, FlyGive.getItem());
        player.getInventory().setItem(4, createBannerSlot());

        ItemStack traps = TrapGive.getItem().clone();
        traps.setAmount(3);
        player.getInventory().setItem(6, traps);

        player.getInventory().setItem(7, ExplosionGive.getItem());
        player.getInventory().setItem(8, UltraGive.getItem());

        player.getInventory().addItem(new ItemStack(Material.GRAY_WOOL, 64));
        player.getInventory().addItem(new ItemStack(Material.SHEARS, 1));
        player.getInventory().addItem(new ItemStack(Material.BREAD, 64));

        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET, 1));

        for (int i = 0; i < 4; i++) {
            player.getInventory().addItem(new ItemStack(Material.GRAY_WOOL, 64));
        }

        player.sendMessage(Component.text("All items have been given!"));
    }


    private ItemStack createBannerSlot() {
        ItemStack item = new ItemStack(Material.RED_DYE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Banner Slot"));
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }
}
