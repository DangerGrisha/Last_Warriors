package greg.pirat1c.humiliation.entity.ladynagan;

import greg.pirat1c.humiliation.entity.CustomEntity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrapMine extends CustomEntity {

    final private boolean unbreakable;

    static TrapMine singletonInstance;

    public static TrapMine createMineObject() {
        if (singletonInstance == null) {
            singletonInstance = new TrapMine(Material.PURPLE_CONCRETE, "stab", "Trap", true);
        }
        return singletonInstance;
    }

    private TrapMine(Material material, String lore, String displayName, boolean unbreakable) {
        super(material, lore, displayName);
        this.unbreakable = unbreakable;
    }

    public ItemStack createMine(int amount) {
        log("adding mine to the stack");
        ItemStack mineAsStack = new ItemStack(material, amount);
        ItemMeta meta = mineAsStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setUnbreakable(unbreakable);
            meta.setLore(Arrays.asList(lore));
            mineAsStack.setItemMeta(meta);
        }

        return mineAsStack;
    }

    private void log(String data) {
        System.out.println(data);
    }
}
