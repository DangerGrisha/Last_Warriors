package greg.pirat1c.humiliation.command.ladynagan;


import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static greg.pirat1c.humiliation.events.ladynagan.LadyConstants.NAME_OF_ULTRA_BUTTON;

public class UltraGive implements CommandExecutor {
    private static final Material material = Material.STICK;

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
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(NAME_OF_ULTRA_BUTTON));
            meta.setUnbreakable(true);

            List<String> lore = new ArrayList<>();

            lore.add("something");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

}
