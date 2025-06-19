package greg.pirat1c.humiliation.command.ladynagan;


import greg.pirat1c.humiliation.entity.ladynagan.TrapMine;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static greg.pirat1c.humiliation.events.ladynagan.LadyConstants.SET_UP_BLOCK;

public class TrapGive implements CommandExecutor {
    private static final String displayName = "Trap";

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
        return getItem(1);
    }
    public static ItemStack getItem(int amount) {
        ItemStack sword = new ItemStack(SET_UP_BLOCK, 1);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName));
            meta.setUnbreakable(true);

            List<String> lore = new ArrayList<>();

            lore.add("something");

            meta.setLore(lore);
            sword.setItemMeta(meta);
        }
        return sword;
    }

}
