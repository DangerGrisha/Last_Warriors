package greg.pirat1c.humiliation.StaticInventory;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerClassManager {
    private final JavaPlugin plugin;

    public PlayerClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerClass getClassFromPlayer(Player player) {
        String tag = player.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "class_tag"),
                PersistentDataType.STRING
        );

        if (tag == null) return null;

        PlayerClass pc = PlayerClass.fromTag(tag);
        return pc;
    }


}
