package greg.pirat1c.humiliation.command.ladynagan;


import greg.pirat1c.humiliation.entity.ladynagan.TrapMine;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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

    private ItemStack getItem() {
        return TrapMine.createMineObject().createMine(1);
    }

}
