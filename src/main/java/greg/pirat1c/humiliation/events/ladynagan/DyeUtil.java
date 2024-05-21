package greg.pirat1c.humiliation.events.ladynagan;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DyeUtil {

    public static ItemStack createDye(Material dyeMaterial, String displayName) {
        ItemStack dye = new ItemStack(dyeMaterial);
        ItemMeta meta = dye.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            dye.setItemMeta(meta);
        }
        return dye;
    }

    public static ItemStack createRedDye(String displayName) {
        return createDye(Material.RED_DYE,  displayName);
    }

    public static ItemStack createGreenDye(String displayName) {
        return createDye(Material.GREEN_DYE, displayName);
    }

    public static ItemStack createYellowDye(String displayName) {
        return createDye(Material.YELLOW_DYE, displayName);
    }

    // You can add more methods for other dye colors if needed
}

