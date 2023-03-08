package greg.pirat1c.humiliation.events.saske;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;




        import org.bukkit.ChatColor;
        import org.bukkit.Material;
        import org.bukkit.enchantments.Enchantment;
        import org.bukkit.entity.*;
        import org.bukkit.event.EventHandler;
        import org.bukkit.event.Listener;
        import org.bukkit.event.entity.EntityDamageByEntityEvent;
        import org.bukkit.event.entity.PlayerDeathEvent;
        import org.bukkit.event.entity.ProjectileLaunchEvent;
        import org.bukkit.inventory.ItemStack;
        import org.bukkit.inventory.meta.ItemMeta;
        import org.bukkit.metadata.FixedMetadataValue;
        import org.bukkit.plugin.java.JavaPlugin;
        import org.bukkit.potion.PotionEffect;
        import org.bukkit.potion.PotionEffectType;
        import org.bukkit.scheduler.BukkitRunnable;
        import org.bukkit.scoreboard.Team;

        import java.sql.SQLOutput;

        import static org.bukkit.Bukkit.getName;
        import static org.bukkit.Bukkit.getPlayer;

public class SpeedAfterKillListener implements Listener {

    private static final Integer ADDED_DAMAGE = 3;
    private JavaPlugin plugin;
    public SpeedAfterKillListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    //canUse - just mean can you use this ability or something after reload
    boolean canUse = true;
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killedPlayer = event.getEntity();
        Player killerPlayer = killedPlayer.getKiller();

        if (killerPlayer != null && canUse == true) {
            // Add resistance potion effect to the killer
            killerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));




            canUse = false;


            new BukkitRunnable() {
                @Override
                public void run() {
                    canUse = true;
                }
            }.runTaskLater(plugin, 900L);
        }
    }

}
