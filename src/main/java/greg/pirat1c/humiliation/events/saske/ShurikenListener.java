package greg.pirat1c.humiliation.events.saske;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.sql.SQLOutput;

import static org.bukkit.Bukkit.getName;
import static org.bukkit.Bukkit.getPlayer;

public class ShurikenListener implements Listener {

    private static final Integer ADDED_DAMAGE = 3;
    private JavaPlugin plugin;
    public ShurikenListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        System.out.println(event.getEntity());

        if (event.getDamager() instanceof Snowball && event.getEntity() instanceof LivingEntity) {

            final double totalDamage = event.getDamage() + ADDED_DAMAGE;
            event.setDamage(totalDamage);
            System.out.println(totalDamage);
            System.out.println(((LivingEntity)event.getEntity()).getHealth());
        }
    }

}
