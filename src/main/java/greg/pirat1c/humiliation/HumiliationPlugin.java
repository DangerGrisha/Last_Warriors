package greg.pirat1c.humiliation;

import greg.pirat1c.humiliation.entity.HumiliationCommand;
import greg.pirat1c.humiliation.entity.SpawnCommand;
import greg.pirat1c.humiliation.entity.TestSpawn;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class HumiliationPlugin extends JavaPlugin {
    public static final String SPAWN_ENTITY_COMMAND_NAME = "spawnEntity";
    public static final String HUMILIATION_COMMAND_NAME = "humiliateMe";

    @Override
    public void onEnable() {
       getCommand(SPAWN_ENTITY_COMMAND_NAME).setExecutor(new SpawnCommand(this)); // Plugin startup logic
       getCommand(HUMILIATION_COMMAND_NAME).setExecutor(new HumiliationCommand(this)); //
        getCommand("test").setExecutor(new TestSpawn());// Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
