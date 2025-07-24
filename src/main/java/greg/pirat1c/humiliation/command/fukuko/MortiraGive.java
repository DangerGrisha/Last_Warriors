package greg.pirat1c.humiliation.command.fukuko;


import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static greg.pirat1c.humiliation.events.fukuko.FukukoConstants.MATERIAL_OF_MORTIRA_FUKUKO;
import static greg.pirat1c.humiliation.events.fukuko.FukukoConstants.NAME_OF_MORTIRA_FUKUKO;

public class MortiraGive implements CommandExecutor {
    private static final Material material = MATERIAL_OF_MORTIRA_FUKUKO;
    private static final String displayName = NAME_OF_MORTIRA_FUKUKO;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        Player player = (Player) sender;

        player.getInventory().addItem(getItem());
        ItemStack itemInSecondSlot = player.getInventory().getItem(2);
        if (itemInSecondSlot != null) {
            System.out.println("item found");
            System.out.println(itemInSecondSlot);
        } else {
            System.out.println("no item");
        }

        return true;
    }

    public static ItemStack getItem() {
        ItemStack sword = new ItemStack(material, 1);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setUnbreakable(true);

            List<String> lore = new ArrayList<>();

            lore.add("something");

            meta.setLore(lore);
            sword.setItemMeta(meta);
        }
        return sword;
    }

}
