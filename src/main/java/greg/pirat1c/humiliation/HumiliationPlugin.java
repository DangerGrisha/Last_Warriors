package greg.pirat1c.humiliation;

//import com.comphenix.protocol.PacketType;
//import com.comphenix.protocol.ProtocolLibrary;
//import com.comphenix.protocol.ProtocolManager;
//import com.comphenix.protocol.events.ListenerPriority;
//import com.comphenix.protocol.events.PacketAdapter;
//import com.comphenix.protocol.events.PacketContainer;
//import com.comphenix.protocol.events.PacketEvent;
import greg.pirat1c.humiliation.command.AttractionGive;
import greg.pirat1c.humiliation.command.ChidoryGive;
import greg.pirat1c.humiliation.command.HomeCommand;
import greg.pirat1c.humiliation.command.HumiliationCommand;
import greg.pirat1c.humiliation.command.Ishigava.AuraGive;
import greg.pirat1c.humiliation.command.Ishigava.LastWaterWallGive;
import greg.pirat1c.humiliation.command.Ishigava.WaterBridgesGive;
import greg.pirat1c.humiliation.command.Ishigava.WaterShieldGive;
import greg.pirat1c.humiliation.command.KitSlaveCommand;
import greg.pirat1c.humiliation.command.SaskeBodyReplacement;
import greg.pirat1c.humiliation.command.SaskeSword;
import greg.pirat1c.humiliation.command.SetHomeCommand;
import greg.pirat1c.humiliation.command.SnowShuriken;
import greg.pirat1c.humiliation.command.SpawnCommand;
import greg.pirat1c.humiliation.command.SpawnHome;
import greg.pirat1c.humiliation.command.TestSpawnCommand;
import greg.pirat1c.humiliation.command.fukuko.BombZoneGive;
import greg.pirat1c.humiliation.command.fukuko.MortiraGive;
import greg.pirat1c.humiliation.command.fukuko.PistolGive;
import greg.pirat1c.humiliation.command.ladynagan.*;
import greg.pirat1c.humiliation.entity.HomeInfo;
import greg.pirat1c.humiliation.events.FloorIce;
import greg.pirat1c.humiliation.events.fukuko.BombZoneListener;
import greg.pirat1c.humiliation.events.fukuko.MortiraListener;
import greg.pirat1c.humiliation.events.fukuko.PistolListener;
import greg.pirat1c.humiliation.events.ishigava.*;
import greg.pirat1c.humiliation.events.ladynagan.*;

import greg.pirat1c.humiliation.events.saske.AttractionListener;
import greg.pirat1c.humiliation.events.saske.BodyReplacemenListener;
import greg.pirat1c.humiliation.events.saske.ChidoryListener;
import greg.pirat1c.humiliation.events.saske.ResistanceAfterKillListener;
import greg.pirat1c.humiliation.events.saske.ShurikenListener;
import greg.pirat1c.humiliation.events.saske.SpeedAfterKillListener;
import greg.pirat1c.humiliation.events.saske.SwordSaskeListener;
import greg.pirat1c.humiliation.events.saske.ThrowListener;
import greg.pirat1c.humiliation.events.soccer.ExperienceMinerListener;
import greg.pirat1c.humiliation.events.soccer.KickSoccerBallListener;
import greg.pirat1c.humiliation.events.soccer.SlimeSoccerListener;
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
    //    ProtocolManager manager = ProtocolLibrary.getProtocolManager();
//        if (manager == null) {
//            getLogger().severe("Failed to load ProtocolManager from ProtocolLib. Disabling plugin.");
//            getServer().getPluginManager().disablePlugin(this);
//            return;
//       }

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
        getCommand("waterShieldGive").setExecutor(new WaterShieldGive());
        getCommand("waterBridgesGive").setExecutor(new WaterBridgesGive());
        getCommand("lastBridgesGive").setExecutor(new LastWaterWallGive());
        getCommand("auraGive").setExecutor(new AuraGive());
        getCommand("pistolGive").setExecutor(new PistolGive());
        getCommand("bombZoneGive").setExecutor(new BombZoneGive());
        getCommand("mortiraGive").setExecutor(new MortiraGive());


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

        Bukkit.getPluginManager().registerEvents(new WaterShieldListener(this),this);
        Bukkit.getPluginManager().registerEvents(new WaterBridgesListener(this),this);
        Bukkit.getPluginManager().registerEvents(new BridgeControlListener(this),this);
        Bukkit.getPluginManager().registerEvents(new LastWaterWallListener(this),this);
        Bukkit.getPluginManager().registerEvents(new AuraListener(this),this);
        //Bukkit.getPluginManager().registerEvents(new ClonesListener(this),this);

        Bukkit.getPluginManager().registerEvents(new PistolListener(this),this);
        Bukkit.getPluginManager().registerEvents(new BombZoneListener(this),this);
        Bukkit.getPluginManager().registerEvents(new MortiraListener(this),this);


        Bukkit.getPluginManager().registerEvents(new GlassPanelPlaceListener(this),this);
        //Bukkit.getPluginManager().registerEvents(new StaticInventoryListener(this),this);

        //Bukkit.getPluginManager().registerEvents(new SwordTest(this), this);
        //PluginManager pluginManager = Bukkit.getPluginManager();
        //Bukkit.getPluginManager().registerEvents(new SlimeSoccerListener(this), this);
        //Bukkit.getPluginManager().registerEvents(new ExperienceMinerListener(this), this);
        //Bukkit.getPluginManager().registerEvents(new KickSoccerBallListener(this), this);

        getLogger().info("HumiliationPlugin is now enabled!");

    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("HumiliationPlugin is now disabled!");
    }
}
