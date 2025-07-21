package greg.pirat1c.humiliation.events.ladynagan;

import greg.pirat1c.humiliation.command.ladynagan.TrapGive;
import greg.pirat1c.humiliation.command.ladynagan.UltraGive;
import greg.pirat1c.humiliation.entity.ladynagan.TrapMine;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
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

import static greg.pirat1c.humiliation.events.ladynagan.LadyConstants.*;

public class TrapsListener implements Listener {


    private final Map<String, Long> bombCooldowns = new HashMap<>();
    private final Map<Player, Integer> activeBombCounts = new HashMap<>();
    //private final String TAG_NAME = LadyConstants.LADY_TAG;
    private final Map<ArmorStand, BukkitRunnable> armorStandTimers = new HashMap<>();
    private final Map<Player, List<Long>> mineCooldowns = new HashMap<>();
    //private final int MAX_MINES = 3; // Maximum number of mines a player can place
    private final int TIMER_SECONDS = 240; // 4 minutes timer
    private final Map<ArmorStand, Player> armorStandOwners = new HashMap<>();

    //private volatile Boolean isInteracted = false;
    // Map to store the armor stands and the items they are holding
    //private Map<ArmorStand, ItemStack> armorStandItems = new HashMap<>();
    private final JavaPlugin plugin;
    private final Team blueTeam;
    private final Team redTeam;
    private CooldownManager cooldownManager = null;
    public TrapsListener(JavaPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.blueTeam = getTeam("BLUE");
        this.redTeam = getTeam("RED");
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        log("in onPlayerInteract");
        final Player player = event.getPlayer();
        final Block clickedBlock = event.getClickedBlock();
        final String playerTeam = getTeam(player);
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            placeMine(event, player, clickedBlock, playerTeam);
            log("A1 " + playerTeam);
        }

    }

    private void placeMine(PlayerInteractEvent event, Player player, Block clickedBlock, String playerTeam) {
        int currentBombCount = activeBombCounts.getOrDefault(player, 0);

        // Prevent placement if maximum active mines reached
        /*
        if (currentBombCount >= MAX_MINES) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You have reached the maximum number of active mines!");
            return;
        }
         */

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND &&
                player.getInventory().getItemInMainHand().getType() == SET_UP_BLOCK &&
                TRAP_NAME.equals(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName())) {

            Block blockToPlace = clickedBlock.getRelative(event.getBlockFace());
            if (blockToPlace.getType() == Material.AIR) {
                Location loc = blockToPlace.getLocation();

                // Check if the player is too close to the mine location(we using so massive objects to because spigot cannot normaly check
                // radius , radius normally working for every direction except north-west) so it's why we doing ->
                Location playerLocation = player.getLocation();
                double playerX = playerLocation.getX();
                double playerY = playerLocation.getY();
                double playerZ = playerLocation.getZ();

// Get the center of the block's location
                double blockCenterX = loc.getX() + 0.5;
                double blockCenterY = loc.getY() + 0.5;
                double blockCenterZ = loc.getZ() + 0.5;

// Calculate the squared distance (avoids the cost of Math.sqrt)
                double distanceSquared = Math.pow(playerX - blockCenterX, 2)
                        + Math.pow(playerY - blockCenterY, 2)
                        + Math.pow(playerZ - blockCenterZ, 2);

// Check if the distance squared is less than the squared radius (1.5^2 = 2.25 for 1.5 blocks)
                if (distanceSquared <= 1) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot place a mine so close to yourself!");
                    return;
                }


                String bombId = player.getUniqueId() + "-bomb" + currentBombCount;

                // Check for proximity restrictions
                if (!isNearPlayersOrMines(loc, player, 3)) {
                    startBlockRemoval(blockToPlace);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ArmorStand armorStand = replaceBlockWithArmorStand(loc);
                            // Assign team
                            if (playerTeam.equals("BLUE")) {
                                addToBlueTeam(armorStand);
                            } else if (playerTeam.equals("RED")) {
                                addToRedTeam(armorStand);
                            }
                            // Associate the ArmorStand with its owner
                            armorStandOwners.put(armorStand, player);
                            // Increment active mines and start timer
                            activeBombCounts.put(player, currentBombCount + 1);
                            startArmorStandTimer(armorStand, player, bombId, TRAPS_SLOT);

                            // Check if the 6th slot is empty and set it to a gray stained glass pane if it is
                            if (player.getInventory().getItem(TRAPS_SLOT) == null ||
                                    player.getInventory().getItem(TRAPS_SLOT).getType() == Material.AIR) {
                                player.getInventory().setItem(TRAPS_SLOT, createGrayGlassPane());
                            }
                        }
                    }.runTaskLater(plugin, 0);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot place a mine near players or other mines!");
                }
            }
        }
    }

    private void startArmorStandTimer(ArmorStand armorStand, Player player, String bombId, int inventorySlot) {
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (!armorStand.isDead()) {
                    armorStand.remove(); // Remove the mine after the timer expires

                    // Start cooldown and decrement active mine count
                    coolDown(player, bombId, inventorySlot, COOLDOWN_SECONDS);
                    activeBombCounts.put(player, activeBombCounts.getOrDefault(player, 0) - 1);
                }
                armorStandTimers.remove(armorStand); // Clean up
            }
        };

        timer.runTaskLater(plugin, TIMER_SECONDS * 20L);
        armorStandTimers.put(armorStand, timer);
    }


    private void coolDown(Player player, String bombId, int inventorySlot, int coolDownSeconds) {
        long endTime = System.currentTimeMillis() + (coolDownSeconds * 1000);
        bombCooldowns.put(bombId, endTime);

        cooldownManager.startCooldown(player, bombId, inventorySlot, coolDownSeconds, false);

        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= endTime) {
                    bombCooldowns.remove(bombId);
                    player.sendMessage(ChatColor.GREEN + "Your mine is ready to use again!");
                    giveAddItem(player,inventorySlot);


                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    public void giveAddItem(Player player, int inventorySlot){
        ItemStack mineItem = TrapGive.getItem();
        ItemStack currentItem = player.getInventory().getItem(inventorySlot);

        if (currentItem == null || currentItem.getType() == Material.AIR || currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            // Slot is empty, contains air, or has a gray stained glass pane
            player.getInventory().setItem(inventorySlot, mineItem);
        } else {
            player.getInventory().addItem(mineItem);
        }
    }


    /**
     * Checks if there are players or mines (armor stands) near the specified location, excluding the placing player.
     *
     * @param location The location to check.
     * @param placer   The player placing the mine (to be excluded from the check).
     * @param radius   The radius to search within.
     * @return True if there are players or mines nearby (excluding the placer), false otherwise.
     */
    private boolean isNearPlayersOrMines(Location location, Player placer, int radius) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(placer)) {
                return true; // Found a player (not the placer) within the radius
            }
            if (entity instanceof ArmorStand) {
                return true; // Found a mine (armor stand) within the radius
            }
        }
        return false;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the entity being damaged is an ArmorStand
        if (event.getEntity() instanceof ArmorStand && event.getDamager() instanceof Player) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            Player damager = (Player) event.getDamager();

            // Check if the ArmorStand is a trap (has the correct custom name)
            if (TRAP_NAME.equals(armorStand.getCustomName())) {
                // Cancel the armor stand's timer if it exists
                if (armorStandTimers.containsKey(armorStand)) {
                    armorStandTimers.get(armorStand).cancel();
                    armorStandTimers.remove(armorStand);
                }

                // Retrieve the original owner of the ArmorStand
                Player originalOwner = armorStandOwners.get(armorStand);

                if (originalOwner != null) {
                    // If the damager is an ally, simply return the mine
                    if (damager.equals(originalOwner)) {
                        giveAddItem(originalOwner,SLOT_OF_TRAPS);
                        originalOwner.sendMessage(ChatColor.GREEN + "You retrieved your mine!");
                    } else {
                        // If the damager is an enemy, return the bomb to the original owner and start cooldown
                        String bombId = originalOwner.getUniqueId() + "-damagedMine"; // Unique identifier
                        coolDown(originalOwner, bombId, TRAPS_SLOT, COOLDOWN_SECONDS);

                    }
                } else {
                    damager.sendMessage(ChatColor.YELLOW + "This mine has no owner!");
                }

                // Remove the armor stand from the map and the world
                armorStandOwners.remove(armorStand);
                armorStand.remove();
            }
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        // Get the player's team
        String playerTeam = getTeam(player);

        // Check if there's an armor stand (bomb) nearby
        for (ArmorStand armorStand : playerLocation.getWorld().getEntitiesByClass(ArmorStand.class)) {
            Location armorStandLocation = armorStand.getLocation();

            // Check if the armor stand is within one block radius of the player
            if (armorStandLocation.distance(playerLocation) <= DETECT_RADIUS) {
                // Determine the bomb's team
                if (isInRedTeam(armorStand)) {
                    // Bomb belongs to Red Team; triggered by Blue players
                    if (playerTeam.equals("BLUE")) {
                        // Trigger explosion and damage only Blue Team players
                        triggerExplosion(armorStand, armorStandLocation, "BLUE");
                    }
                } else if (isInBlueTeam(armorStand)) {
                    // Bomb belongs to Blue Team; triggered by Red players
                    if (playerTeam.equals("RED")) {
                        // Trigger explosion and damage only Red Team players
                        triggerExplosion(armorStand, armorStandLocation, "RED");
                    }
                }
            }
        }
    }

    private void triggerExplosion(ArmorStand armorStand, Location explosionLocation, String teamToDamage) {
        String bombId = armorStand.getUniqueId().toString();

        armorStand.getWorld().createExplosion(explosionLocation, EXPLOSION_POWER, false, DAMAGE_TERRAIN);

        //send message to owner
        Player originalOwner = armorStandOwners.get(armorStand);
        originalOwner.sendMessage(ChatColor.RED + "Triggered an mine!");

        for (Entity entity : armorStand.getNearbyEntities(DAMAGE_RADIUS, DAMAGE_RADIUS, DAMAGE_RADIUS)) {
            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;

                if (getTeam(targetPlayer).equals(teamToDamage)) {
                    targetPlayer.damage(DAMAGE_AMOUNT); // Damage only opposing team
                }
            }
        }

        // Decrement active mines
        for (Map.Entry<Player, Integer> entry : activeBombCounts.entrySet()) {
            Player player = entry.getKey();
            if (armorStandTimers.containsKey(armorStand)) {
                armorStandTimers.get(armorStand).cancel();
            }
            armorStandTimers.remove(armorStand);
            activeBombCounts.put(player, activeBombCounts.getOrDefault(player, 0) - 1);
            coolDown(player, bombId, TRAPS_SLOT, COOLDOWN_SECONDS);
        }

        armorStand.remove();
    }





    // Method to replace a block with an armor stand
    public ArmorStand replaceBlockWithArmorStand(Location location) {

        //log("in replaceBlockWithArmorStand");
        ArmorStand armorStand = location.getWorld().spawn(location.add(0.5, 0, 0.5), ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        //armorStand.setInvulnerable(false);
        armorStand.setBasePlate(false);
        armorStand.setInvulnerable(true);
        //if we add true in marker , armorstand will be Invulnerable
        //armorStand.setMarker(false);
        armorStand.setCustomName(TRAP_NAME);
        armorStand.setCustomNameVisible(false);
        armorStand.setArms(true);
        armorStand.setSmall(true);

        ItemStack redDye = new ItemStack(Material.BLUE_DYE);
        ItemMeta dyeMeta = redDye.getItemMeta();
        dyeMeta.displayName(Component.text("TrapLedy"));
        redDye.setItemMeta(dyeMeta);
        armorStand.getEquipment().setItemInMainHand(redDye);

        //armorStandItems.put(armorStand, redDye); // Store the armor stand and the item it's holding

        return armorStand;
    }

    /**remove pi
     * armorStand.setCanPickupItems(false) -> is not working so
     * to do it I added this method
     * @param event
     */
    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        //System.out.println("A1");
        if (event.getEntity() instanceof ArmorStand) {
           //System.out.println("A2");
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (TRAP_NAME.equals(armorStand.getCustomName())) {
                //System.out.println("A3");
                event.setCancelled(true);
            }
        }
    }



    // Method to start the block removal after a delay
    private void startBlockRemoval(Block block) {
        block.setType(Material.AIR);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == SET_UP_BLOCK) {
                        block.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 0L);
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

    private ItemStack createGrayGlassPane() {
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = grayPane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(ChatColor.GRAY + "Mine Cooldown"));
            grayPane.setItemMeta(meta);
        }
        return grayPane;
    }


    private void log(String data) {
        System.out.println(data);
    }


}
