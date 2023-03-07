    package greg.pirat1c.humiliation;

import greg.pirat1c.humiliation.command.*;
import greg.pirat1c.humiliation.entity.HomeInfo;
import greg.pirat1c.humiliation.events.FloorIce;
import greg.pirat1c.humiliation.events.saske.BodyReplacemenListener;
import greg.pirat1c.humiliation.events.saske.ShurikenListener;
import greg.pirat1c.humiliation.events.saske.SwordSaskeListener;
import greg.pirat1c.humiliation.events.saske.ThrowListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

    public final class HumiliationPlugin extends JavaPlugin {
    public static final String SPAWN_ENTITY_COMMAND_NAME = "spawnEntity";
    public static final String HUMILIATION_COMMAND_NAME = "humiliateMe";

    @Override
    public void onEnable() {
        Map<UUID,HomeInfo> playerIdToHomeMap = new HashMap<>();
        HomeInfo homeInfo = new HomeInfo();

       getCommand(SPAWN_ENTITY_COMMAND_NAME).setExecutor(new SpawnCommand(this)); // Plugin startup logic
       getCommand(HUMILIATION_COMMAND_NAME).setExecutor(new HumiliationCommand(this)); //
        getCommand("test").setExecutor(new TestSpawnCommand());
        getCommand("spawn").setExecutor(new SpawnHome());
        getCommand("sethome").setExecutor(new SetHomeCommand(playerIdToHomeMap));
        getCommand("home").setExecutor(new HomeCommand(playerIdToHomeMap));
        getCommand("kitslave").setExecutor(new KitSlaveCommand());
        getCommand("saskesword").setExecutor(new SaskeSword());
        getCommand("shuriken").setExecutor(new SnowShuriken());
        getCommand("bodyReplacemen").setExecutor(new SaskeBodyReplacemen());

       // Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new FloorIce(this), this);
        Bukkit.getPluginManager().registerEvents(new SwordSaskeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ShurikenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ThrowListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BodyReplacemenListener(this), this);
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
