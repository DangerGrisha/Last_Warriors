package greg.pirat1c.humiliation.command;


import jdk.internal.org.jline.utils.Display;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SaskeSword implements CommandExecutor {

    private static final Material material = Material.DIAMOND_SWORD;

    //display name katana - sword
    //private static final Display display = new Display("katana sword");



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

        ItemStack sword = new ItemStack(material, 1);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("katana sword");
            meta.setUnbreakable(true);

            List<String> lore = new ArrayList<>();

            lore.add("something");



            meta.setLore(lore);
            sword.setItemMeta(meta);
        }
        return sword;
    }

}
