package greg.pirat1c.humiliation.events.ladynagan;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;

public class GlassPanelPlaceListener implements Listener {

    public final JavaPlugin plugin;

    // Set of all stained glass pane materials
    private static final EnumSet<Material> GLASS_PANELS = EnumSet.of(
            Material.GLASS_PANE,
            Material.WHITE_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE,
            Material.GRAY_STAINED_GLASS_PANE,
            Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.BROWN_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE,
            Material.RED_STAINED_GLASS_PANE,
            Material.BLACK_STAINED_GLASS_PANE
    );

    public GlassPanelPlaceListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Get the player and the block they are placing
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if the block being placed is a glass panel
        if (GLASS_PANELS.contains(block.getType())) {
            // Cancel the placement event
            event.setCancelled(true);

            // Notify the player
            player.sendMessage("You cannot place stained glass panels!");
        }
    }
}
