package greg.pirat1c.humiliation.events.saske;


import greg.pirat1c.humiliation.command.SnowShuriken;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static greg.pirat1c.humiliation.events.saske.SaskeConstants.SHURICKEN_NAME;

public class ThrowListener implements Listener {

    private JavaPlugin plugin;
    public ThrowListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball && event.getEntity().getShooter() instanceof Player) {
            final Player player = (Player) event.getEntity().getShooter();
            new BukkitRunnable() {
                @Override
                public void run() {


                    player.getInventory().addItem(SnowShuriken.getShuriken());

                }
            }.runTaskLater(plugin, 200L);
        }
    }
}


