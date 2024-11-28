package greg.pirat1c.humiliation;

import greg.pirat1c.humiliation.command.AttractionGive;
import greg.pirat1c.humiliation.command.ChidoryGive;
import greg.pirat1c.humiliation.command.HomeCommand;
import greg.pirat1c.humiliation.command.HumiliationCommand;
import greg.pirat1c.humiliation.command.KitSlaveCommand;
import greg.pirat1c.humiliation.command.SaskeBodyReplacement;
import greg.pirat1c.humiliation.command.SaskeSword;
import greg.pirat1c.humiliation.command.SetHomeCommand;
import greg.pirat1c.humiliation.command.SnowShuriken;
import greg.pirat1c.humiliation.command.SpawnCommand;
import greg.pirat1c.humiliation.command.SpawnHome;
import greg.pirat1c.humiliation.command.TestSpawnCommand;
import greg.pirat1c.humiliation.command.ladynagan.*;
import greg.pirat1c.humiliation.entity.HomeInfo;
import greg.pirat1c.humiliation.events.FloorIce;
import greg.pirat1c.humiliation.events.ladynagan.*;

import greg.pirat1c.humiliation.events.saske.AttractionListener;
import greg.pirat1c.humiliation.events.saske.BodyReplacemenListener;
import greg.pirat1c.humiliation.events.saske.ChidoryListener;
import greg.pirat1c.humiliation.events.saske.ResistanceAfterKillListener;
import greg.pirat1c.humiliation.events.saske.ShurikenListener;
import greg.pirat1c.humiliation.events.saske.SpeedAfterKillListener;
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
    public CooldownManager cooldownManager = null;

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager(this);

        Map<UUID, HomeInfo> playerIdToHomeMap = new HashMap<>();
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
        getCommand("bodyReplacement").setExecutor(new SaskeBodyReplacement());
        getCommand("attractionGive").setExecutor(new AttractionGive());
        getCommand("chidoryGive").setExecutor(new ChidoryGive());
        getCommand("sniperGive").setExecutor(new SniperGive());
        getCommand("flyGive").setExecutor(new FlyGive());
        getCommand("explosionGive").setExecutor(new ExplosionGive());
        getCommand("trapGive").setExecutor(new TrapGive());
        getCommand("ultraGiveLady").setExecutor(new UltraGive());

        // Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new FloorIce(this), this);
        Bukkit.getPluginManager().registerEvents(new SwordSaskeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ShurikenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ThrowListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BodyReplacemenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ResistanceAfterKillListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SpeedAfterKillListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AttractionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChidoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SniperListener(this, cooldownManager), this);
        Bukkit.getPluginManager().registerEvents(new FlyListener(this,cooldownManager),this);
        Bukkit.getPluginManager().registerEvents(new ExplosionListener(this, cooldownManager),this);
        Bukkit.getPluginManager().registerEvents(new TrapsListener(this, cooldownManager),this);

        Bukkit.getPluginManager().registerEvents(new GlassPanelPlaceListener(this),this);
        //Bukkit.getPluginManager().registerEvents(new StaticInventoryListener(this),this);

        //Bukkit.getPluginManager().registerEvents(new SwordTest(this), this);
        //PluginManager pluginManager = Bukkit.getPluginManager();
        //Bukkit.getPluginManager().registerEvents(new SlimeSoccerListener(this), this);
        //Bukkit.getPluginManager().registerEvents(new ExperienceMinerListener(this), this);
        //Bukkit.getPluginManager().registerEvents(new KickSoccerBallListener(this), this);

    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
