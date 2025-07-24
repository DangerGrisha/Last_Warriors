package greg.pirat1c.humiliation.command;


import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitSlaveCommand implements CommandExecutor {



    @Override


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        Player player = (Player) sender;

        player.getInventory().setItem(2,getDirt());
        ItemStack itemInSecondSlot = player.getInventory().getItem(2);
        if (itemInSecondSlot != null) {
            System.out.println("item found");
            System.out.println(itemInSecondSlot);
        } else {
            System.out.println("no item");
        }

        return true;
    }

    private ItemStack getDirt() {

        ItemStack item = new ItemStack(Material.DIRT, 64);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("SLAVE_DIRT"));
            meta.setUnbreakable(true);

            List<String> lore = new ArrayList<>();
            lore.add("Buy");
            lore.add("NEW");
            lore.add("HOUSE");


            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

}
