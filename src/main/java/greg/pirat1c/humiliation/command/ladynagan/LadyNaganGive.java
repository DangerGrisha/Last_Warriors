package greg.pirat1c.humiliation.command.ladynagan;

import net.kyori.adventure.text.Component;
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
        if (!(sender instanceof Player player)) return false;

        // Выдаём классовые предметы
        player.getInventory().setItem(0, SniperGive.getItem());
        player.getInventory().setItem(2, FlyGive.getItem());
        player.getInventory().setItem(4, createBannerSlot());
        player.getInventory().setItem(6, TrapGive.getItem());
        player.getInventory().setItem(7, ExplosionGive.getItem());
        player.getInventory().setItem(8, UltraGive.getItem());

        // Дополнительные ресурсы
        player.getInventory().addItem(new ItemStack(Material.WHITE_WOOL, 64));
        player.getInventory().addItem(new ItemStack(Material.SHEARS, 1));
        player.getInventory().addItem(new ItemStack(Material.BREAD, 64));

        // Ещё 256 шерсти (в 4 стака по 64)
        for (int i = 0; i < 4; i++) {
            player.getInventory().addItem(new ItemStack(Material.WHITE_WOOL, 64));
        }

        player.sendMessage(Component.text("All items have been given!"));
        return true;
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
