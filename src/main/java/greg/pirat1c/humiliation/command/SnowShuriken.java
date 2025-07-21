package greg.pirat1c.humiliation.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static greg.pirat1c.humiliation.events.saske.SaskeConstants.SHURICKEN_NAME;

public class SnowShuriken implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

            player.getInventory().setItem(2,getShuriken());
        ItemStack itemInSecondSlot = player.getInventory().getItem(2);
        if (itemInSecondSlot != null) {
            System.out.println("item found");
            System.out.println(itemInSecondSlot);
        } else {
            System.out.println("no item");
        }

        return true;
    }

    public static ItemStack getShuriken() {

        ItemStack item = new ItemStack(Material.SNOWBALL, 3);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(SHURICKEN_NAME));
            meta.setUnbreakable(true);

            List<String> lore = new ArrayList<>();



            meta.lore(List.of(
                    Component.text("Line 1").color(NamedTextColor.GRAY),
                    Component.text("Line 2").color(NamedTextColor.DARK_PURPLE)
            ));

            item.setItemMeta(meta);
        }
        return item;
    }
}
