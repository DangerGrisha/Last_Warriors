package greg.pirat1c.humiliation.events.ladynagan;

import greg.pirat1c.humiliation.entity.ladynagan.TrapMine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrapsListener implements Listener {
    private final JavaPlugin plugin;
    private final Team blueTeam;
    private final String TAG_NAME = LadyConstants.LADY_TAG;
    private static final String TRAP_NAME = "Trap";
    private final Team redTeam;
    //private volatile Boolean isInteracted = false;
    // Map to store the armor stands and the items they are holding
    //private Map<ArmorStand, ItemStack> armorStandItems = new HashMap<>();

    public TrapsListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.blueTeam = getTeam("BLUE");
        this.redTeam = getTeam("RED");
    }

    public static final Material setUpBlock = Material.PURPLE_CONCRETE;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        log("in onPlayerInteract");
        final Player player = event.getPlayer();
        final Block clickedBlock = event.getClickedBlock();
        final String playerTeam = getTeam(player);
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            placeMine(event, player, clickedBlock, playerTeam);
        }

    }

    private void placeMine(PlayerInteractEvent event, Player player, Block clickedBlock,  String playerTeam) {
        log("placeMine");
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND &&
                player.getInventory().getItemInMainHand().getType() == setUpBlock &&
                TRAP_NAME.equals(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName())) {
            Block blockToPlace = clickedBlock.getRelative(event.getBlockFace());
            if (blockToPlace.getType() == Material.AIR) {
                startBlockRemoval(blockToPlace);
                //blockToPlace.setType(setUpBlock);
                Location loc = blockToPlace.getLocation();

                blockToPlace.setType(Material.AIR);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ArmorStand armorStand = replaceBlockWithArmorStand(loc);
                        // Add the armor stand to the corresponding team
                        if (playerTeam.equals("BLUE")) {
                            addToBlueTeam(armorStand);
                        } else if (playerTeam.equals("RED")) {
                            addToRedTeam(armorStand);
                        }
                    }
                }.runTaskLater(plugin, 5);

            }
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        log("in entity damage");
        // Check if the entity damaging is a player and the entity damaged is an ArmorStand
        if (event.getDamager() instanceof Player && event.getEntity() instanceof ArmorStand) {
            log("Illia gay with Mattew");

            new BukkitRunnable() {
                @Override
                public void run() {
                    log("Illia says greg that he is gay");
                }
            }.runTaskLater(plugin, 20);
            Player damager = (Player) event.getDamager();
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            // Check if the ArmorStand has the "Trap" tag
            if (armorStand.getCustomName().equals(TRAP_NAME)) {
                // Remove the ArmorStand
                      if(damager.getScoreboardTags().contains(TAG_NAME)){
                    damager.getInventory().addItem(getItem());
                }
                armorStand.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        log("in player_move");
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        // Get the player's team
        String playerTeam = getTeam(player);

        // Check if there's an armor stand nearby
        for (ArmorStand armorStand : playerLocation.getWorld().getEntitiesByClass(ArmorStand.class)) {
            Location armorStandLocation = armorStand.getLocation();

            // Check if the armor stand is within one block radius of the player
            if (armorStandLocation.distance(playerLocation) <= 1) {
                // Check if the player belongs to the opposite team of the armor stand
                if ((playerTeam.equals("BLUE") && isInRedTeam(armorStand)) || (playerTeam.equals("RED") && isInBlueTeam(armorStand))) {
                    armorStand.getWorld().createExplosion(armorStandLocation, 4.0f); // Explode the armor stand
                    armorStand.remove(); // Remove the armor stand
                }
            }
        }
    }


    // Method to replace a block with an armor stand
    public ArmorStand replaceBlockWithArmorStand(Location location) {

        log("in replaceBlockWithArmorStand");
        ArmorStand armorStand = location.getWorld().spawn(location.add(0.5, 0, 0.5), ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        //armorStand.setInvulnerable(false);
        armorStand.setBasePlate(false);
        // armorStand.setMarker(true);
        armorStand.setCustomName("Trap");
        armorStand.setCustomNameVisible(false);
        armorStand.setArms(false);
        armorStand.setSmall(true);

        ItemStack redDye = new ItemStack(Material.BLUE_DYE);
        ItemMeta dyeMeta = redDye.getItemMeta();
        dyeMeta.setDisplayName("Trap");
        redDye.setItemMeta(dyeMeta);
        armorStand.getEquipment().setItemInMainHand(redDye);

        //armorStandItems.put(armorStand, redDye); // Store the armor stand and the item it's holding

        return armorStand;
    }



    private ItemStack getItem() {
        return TrapMine.createMineObject().createMine(1);
    }


    // Method to start the block removal after a delay
    private void startBlockRemoval(Block block) {
        block.setType(Material.AIR);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == setUpBlock) {
                    block.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    // Method to retrieve the existing team from the scoreboard
    private Team getTeam(String name) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        return scoreboard.getTeam(name);
    }

    // Method to add an armor stand to the blue team
    private void addToBlueTeam(ArmorStand armorStand) {
        blueTeam.addEntry(armorStand.getUniqueId().toString());
    }

    // Method to add an armor stand to the red team
    private void addToRedTeam(ArmorStand armorStand) {
        redTeam.addEntry(armorStand.getUniqueId().toString());
    }

    // Method to check if the armor stand belongs to the blue team
    private boolean isInBlueTeam(ArmorStand armorStand) {
        return blueTeam.hasEntry(armorStand.getUniqueId().toString());
    }

    // Method to check if the armor stand belongs to the red team
    private boolean isInRedTeam(ArmorStand armorStand) {
        return redTeam.hasEntry(armorStand.getUniqueId().toString());
    }

    private String getTeam(Player player) {
        // Get the player's team using some method (for demonstration purposes)
        Team playerTeam = getPlayerTeam(player);

        // Check the player's team
        if (playerTeam != null) {
            return playerTeam.getName(); // Return the player's team in uppercase
        } else {
            // If the player's team is not defined or cannot be determined, return a default team
            return "DEFAULT";
        }
    }

    private Team getPlayerTeam(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                return team;
            }
        }
        return null; // Player is not on any team
    }

    private void log(String data) {
        System.out.println(data);
    }


}
