package greg.pirat1c.humiliation.events.ladynagan;

import com.sun.jdi.event.EventSet;
import greg.pirat1c.humiliation.command.ladynagan.SniperGive;
import greg.pirat1c.humiliation.command.ladynagan.UltraGive;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static greg.pirat1c.humiliation.events.ladynagan.LadyConstants.*;
/**
 *                      What class is doing  (EXPLONATION) SniperListener
    by: Thank you greg from begin of 2024, the code is readeable as fuck
RU:
 Цель: Сделать снайперку (как основное оружие), можно [прицеливаться/перестать целиться] на ПКМ , если в прицеле стрелять на ЛКМ ,
 снаряд - пуля должна быть не супер быстрой (чтобы противники могли среагировать и за доджить)
 снаряд также после выпуска может один раз сменить траекторию на противника если - (противник будет в каком-то радиусе от пули + снайпер нажмет еще раз ЛКМ)
 Ульта - просто отдельная кнопка при нажатии через несколько секунд запуляешь пули такую же просто урон больше
 Как работает:

 -Переименновация
 Снайперка кай-то инструмент(палка) ,
 переменнованный как
 ("T-742K Mori" палка) дефолтный всего их 4 ,
 ("T-742K Mori" арболет - заряженный) нужен чтобы со стороны выглядило как ты прицеливаешься (текстурки не отличаются)
 ("T-742K Mori+" арболет - заряжженный) текстурка добавляет что когда ыт прицеливаешься у тебя на прицеле показывается
 фиолетовый кружек говорящий(что пуля может поменять направление)
 ("T-742K Destroy" арболет - заряжженный) - когда снайпер ультует то выдается снайперка но текстурка просто больше и жирнее

 -Система прицеливания

 -Система выстрела

Eng:
 Features
 Aiming System:

 *Aiming is initiated by right-clicking with the sniper rifle.
 *When aiming, the player wears a carved pumpkin to simulate a scoped view and receives a slow effect for realism.
 *The rifle switches to a charged crossbow (representing the aimed state) with modified visuals.
 *Exiting the aim state removes the pumpkin and restores the original weapon.
 Shooting System:

 *Bullets are represented by invisible ArmorStands with attached "Bullet" items.
 *On left-click, the player shoots, triggering the bullet to move in the aimed direction.
 *Bullets can change direction once if a nearby enemy is detected, simulating homing behavior.
 *Collisions with players or blocks result in bullet removal and apply damage or visual effects.
 Ultimate Ability (Ult):

 *Activated by a specific item (Ultra button) via right-click.
 *Enters a zoomed state, plays a sound, and delays the shot for dramatic effect.
 *If not canceled, fires a bullet with increased damage after a short delay.
 *The Ult can be canceled by right-clicking again, restoring the original weapon state.
 Interaction and Inventory Constraints:

 *Prevents the sniper rifle from being moved or replaced in the player's inventory.
 *Enforces dedicated slots for the sniper rifle and disallows placing other items in its slot.
 *Ensures cooldowns and delays for aiming, shooting, and activating the Ult to maintain balance.
 Bullet Mechanics:

 *Bullets move at a controlled speed to allow dodging by enemies.
 *Homing behavior is triggered when a nearby player (not an ally) is detected.
 *Collisions are detected using ray tracing to ensure accuracy.
 Visual and Sound Effects:

 *Custom sounds for shooting, hitting, and activating the Ult.
 *Visual cues such as pumpkin helmets and charged crossbows for player states.


 How It Works:
 *Event Handling: Listens to various player events, such as PlayerInteractEvent, PlayerItemHeldEvent, and PlayerToggleSneakEvent.
 *Inventory Management: Controls the player's inventory to enforce gameplay rules, such as locking the sniper rifle in a specific slot.
 *Custom Mechanics: Implements custom methods for bullet summoning, trajectory updates, collision detection, and homing logic.
 *Cooldowns and Delays: Uses BukkitRunnable to handle cooldowns and delays for smooth gameplay transitions.

 Key Gameplay Logic:
 *Aiming: A player aims by right-clicking the sniper rifle. The class handles state transitions, applying effects like slowness and helmet changes.
 *Shooting: Bullets are summoned and launched as invisible entities, with collision detection ensuring they interact realistically with the world.
 *Ultimate Ability: A powerful but interruptible attack is initiated with unique effects and mechanics.
 */

public class SniperListener extends SniperGive implements Listener {

    private boolean wearingPumpkin = false;

    private ItemStack previousItemInMainHand;
    private PotionEffect slowEffect; // Store the slow effect
    private boolean delayAfterPumpkinIsDone = false;
    private static boolean isUltaCanceled = false;
    private static boolean isUlting = false;
    private boolean delayAfterShootIsDone = true;
    private boolean changedDirectionOfBullet = false;
    public Vector finalDirection;
    public ArmorStand armorStand = null;
    public Location armorLocation = null;
    private Location nextLocation;

    private JavaPlugin plugin = null;
    private CooldownManager cooldownManager = null;
    public SniperListener(JavaPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    private boolean isInteracted = false;
/*
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicked inventory belongs to a player
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            int clickedSlot = event.getSlot();
            ItemStack itemInSniperSlot = player.getInventory().getItem(sniperRifleSlot); // Slot for the sniper rifle
            ItemStack cursorItem = event.getCursor(); // Item currently held by the player’s cursor

            // Case 1: Prevent moving the sniper rifle out of the designated slot by clicking
            if (isSniperRifle(itemInSniperSlot) && clickedSlot == sniperRifleSlot) {
                // Cancel if the player is trying to move the sniper rifle from its designated slot
                if (event.getClick().isShiftClick() || cursorItem == null) {
                    event.setCancelled(true);
                    player.sendMessage("You cannot move the sniper rifle from the designated slot!");
                    player.setItemOnCursor(null); // Clear cursor to prevent duplication
                }
            }

            // Case 2: Prevent hotbar swapping with the sniper rifle slot
            if (event.getClick().isKeyboardClick() && event.getHotbarButton() == sniperRifleSlot) {
                // If a player presses a hotbar key to swap the sniper rifle out of its slot, cancel the event
                event.setCancelled(true);
                player.sendMessage("You cannot move the sniper rifle from the designated slot using hotbar keys!");
            }

            // Case 3: Prevent placing any other item into the sniper rifle slot
            if (clickedSlot == sniperRifleSlot && cursorItem != null && !isSniperRifle(cursorItem)) {
                // Cancel if attempting to place a non-sniper rifle item in the designated slot
                event.setCancelled(true);
                player.sendMessage("The sniper rifle must stay in the designated slot!");
                player.setItemOnCursor(null); // Clear cursor to prevent duplication
            }
        }
    }

 */


    /**
     * If player wanna change slot when he in zoom,
     * than we removing pumpking and giving back normal rifle
     * in slot where this riffle was
     *
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        // Проверяем, носит ли игрок тыкву и у него нет одной из снайперок
        boolean isNotSniperRifle = newItem == null ||
                !newItem.hasItemMeta() ||
                !(newItem.getItemMeta().getDisplayName().equals(SNIPER_RIFLE_NAME) ||
                        newItem.getItemMeta().getDisplayName().equals(SNIPER_RIFLE_NAME_MODIFIED) ||
                        newItem.getItemMeta().getDisplayName().equals(SNIPER_RIFLE_NAME_ULTRA));
        if (wearingPumpkin && isNotSniperRifle && player.getScoreboardTags().contains("LadyNagan")) {
            removePumpkinAndEffect(player);
            if (isUlting) {
                isUltaCanceled = true;
                isUlting = false;
            }
            ItemStack sniperRifle = player.getInventory().getItem(RIFLE_SLOT);
            returnSniperToOriginalSlot(player,sniperRifle);
            if(isUlting && !isUltaCanceled){
                isUltaCanceled = true;
            }
        }
    }
    private void returnSniperToOriginalSlot(Player player, ItemStack sniperRifle) {
        if (RIFLE_SLOT >= 0 && RIFLE_SLOT <= 8) { // Ensure the slot is within hotbar range
            player.getInventory().setItem(RIFLE_SLOT, SniperGive.getItem(player));
        } else {
            player.getInventory().addItem(SniperGive.getItem(player)); // Fallback if slot is invalid
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();


        //cancel (minecraft inside RBM) shoot if crossbow have arrow
        if (checkEventForRightClickOnCrossbow(event, player) && isInteracted) {
            event.setCancelled(true);
        }

        /**
         * Call zoom rifle system
         * if...
         */
        // Check if the event corresponds to right-click with "T-742K Mori" stick or crossbow
        if ((checkEventForRightClick(event, player, SNIPER_RIFLE_NAME, Material.STICK) ||
                checkEventForRightClick(event, player, SNIPER_RIFLE_NAME, Material.CROSSBOW) && !isInteracted) ||
                checkEventForRightClick(event, player, SNIPER_RIFLE_NAME_MODIFIED, Material.CROSSBOW) && !isInteracted ) {

            event.setCancelled(true); // Cancel the action to prevent hitting

            // we are making a delay to prevent a bug with fast reuse
            isInteracted = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    isInteracted = false;
                }
            }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds


            zoomRifleSystem(event,player);

        }

        /**
         * Check if the player has left-clicked and the shoot delay is complete,
         * and if the player is holding the crossbow. If so, trigger the shooting system.
         */
        if((event.getAction()==Action.LEFT_CLICK_AIR ||event.getAction()==Action.LEFT_CLICK_BLOCK)&&
                delayAfterPumpkinIsDone && checkEventForRightClickOnCrossbow(event, player)) {
            event.setCancelled(true);
           shootSystem(player, event);
        }

        /**
         * CANCEL ULT
         * if player click RBM in speach time zone one more time
         * then we just give isUltraCancel = true; to prevent shoot
         * and remove pumpkin from his head
         */
        //If player used ult and after click right click -> we cancel that ult
        if ((checkEventForRightClick(event, player, SNIPER_RIFLE_NAME_ULTRA, Material.CROSSBOW) && !isInteracted) && !isUltaCanceled && isUlting) {
            event.setCancelled(true);
            notInteract();

            cancelUlt(player);
        }

        /**
         * ULT - sniper riffle shooting ult bullet (more damage)
         * after RBM -> zooming -> making sound a hero speach and waiting for ~3 sec -> shooting
         *                      |<--------------------------------------------------->|
         *                           3 sec - if press again RBM u can cancel shoot
         *                                     but not the speech
         *
         * if(isUlting) than
         */

        //Check if ultra button is pressed on RBM and if we do then ult
        if ((checkEventForRightClick(event, player, NAME_OF_ULTRA_BUTTON, Material.STICK) && !isInteracted) && !isUltaCanceled && !isUlting && player.getScoreboardTags().contains("LadyNagan")) {
            event.setCancelled(true);
            notInteract();
            ultShootSystem(event, player);
        }
    }

    /**
     * Switches the player's active inventory slot to the slot containing an item with the specified name.
     * <p>
     * This method iterates through the player's inventory to find an item that has a display name matching
     * the provided `nameOfItem` parameter. If such an item is found, the player's held item slot is set to
     * that slot, effectively switching the active item. The search stops as soon as a match is found.
     *
     * @param player     The player whose active slot will be switched.
     * @param nameOfItem The display name of the item to search for in the player's inventory.
     */
    private void switchActiveSlotTo(Player player, String nameOfItem) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);

            // Check if the slot has an item and if that item has metadata (like a display name)
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();

                // Check if the item has a display name that matches the target name
                if (meta.hasDisplayName() && meta.getDisplayName().equals(nameOfItem)) {
                    // Set the player's active slot to this item slot and exit the method
                    player.getInventory().setHeldItemSlot(slot);
                    return;
                }
            }
        }
    }

    // Helper method to check if an item is the sniper rifle
    private boolean isSniperRifle(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                (item.getItemMeta().getDisplayName().equals(SNIPER_RIFLE_NAME) ||
                        item.getItemMeta().getDisplayName().equals(SNIPER_RIFLE_NAME_MODIFIED) ||
                        item.getItemMeta().getDisplayName().equals(SNIPER_RIFLE_NAME_ULTRA));
    }

    private void delayForUlta(Player player, String nameOfAbilitySpecific , int inventorySlot, long delayInSeconds) {
        // Start the cooldown using the CooldownManager
        cooldownManager.startCooldown(player, nameOfAbilitySpecific, inventorySlot, delayInSeconds, true);

        // Восстановим предмет чуть позже окончания визуального кулдауна
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cooldownManager.isCooldownComplete(player, nameOfAbilitySpecific)) {
                    isUltaCanceled = false;
                    player.getInventory().setItem(inventorySlot, UltraGive.getItem());
                }
            }
        }.runTaskLater(plugin, (delayInSeconds + 1) * 20L); // добавили 1 секунду
    }



    //Shoot System Shoot SystemShoot SystemShoot SystemShoot System Shoot System
    private void shoot(Player player, ArmorStand armorStand, Vector direction, boolean isUlting, PlayerInteractEvent event) {

        // Play the custom shoot sound for all nearby players
        // Play thunder sound for 3 seconds

        player.getWorld().playSound(player.getLocation(),"ladynagan.shoot",1.0f, 1.0f);

        final boolean[] directionChanged = {false}; // Declare an array with one element to make it effectively final

        // Schedule the removal of the ArmorStand after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, armorStand::remove, REMOVE_BULLET_AFTER); // 3 seconds (20 ticks per second)

        // Variable to store the task ID of ArmorStand movement task
        int armorStandTaskId = -1;
        finalDirection = direction.clone(); // Clone the original direction vector to modify it
        // Schedule a task to move the ArmorStand forward every 0.1 second
        armorStandTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!armorStand.isDead()) {
                updateNextLocation(finalDirection);

                // Check for collisions
                //System.out.println("is Ulting A1 : " + isUlting);
                checkBulletCollision(armorStand, isUlting, player);

                // Add a slight upward velocity to counteract gravity
                finalDirection.setY(finalDirection.getY() + 0.001); // Adjust this value as needed
                //finalDirection = finalDirection.normalize().multiply(2.0); // Increases the speed

                //armorStand.setVelocity(finalDirection.normalize().multiply(0.2).setY(0.04)); // Ensure Y is slightly positive

                //System.out.println("C1" + finalDirection);
                if (changedDirectionOfBullet && !directionChanged[0]) { // Check if the direction of the bullet needs to be changed
                    Player nearestPlayer = GetPlayerNearbyOfBullet(armorStand.getLocation(), player);
                    if (nearestPlayer != null) { // If there is a nearest player
                        // Calculate the new direction vector towards the nearest player
                        Vector newDirection = nearestPlayer.getLocation().subtract(armorStand.getLocation()).toVector();

                        // Nullify the final direction to prepare for the new direction
                        finalDirection = new Vector(0, 0, 0);

                        // Update the bullet's direction
                        finalDirection = newDirection;
                        finalDirection.setY(finalDirection.getY() + 0.5); // was 0.5
                        // Update the bullet's next location (optional, if needed)
                        updateNextLocation(newDirection);

                        // Indicate that the direction has been changed
                        directionChanged[0] = true;

                        // Update the rotation of the armor stand to face the new direction
                        updateArmorStandRotation(armorStand, newDirection);
                    }
                }
                armorStand.setVelocity(finalDirection.normalize().multiply(2.0)); // Set weak motion forward
                checkBulletCollision(armorStand, isUlting, player);
            }
        }, 0L, 2L).getTaskId(); // 0 tick delay, 2 tick interval (10 ticks per second)

        // Schedule a task to mark the direction as unchanged after a delay
        int finalArmorStandTaskId = armorStandTaskId;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            directionChanged[0] = true; // Reset the direction changed flag after a delay
            // Cancel the movement task

            //Give first sniper arbalet if bullet is donne and if
            ItemStack newItem = player.getInventory().getItem(RIFLE_SLOT);
            if(newItem.getItemMeta().getDisplayName().equals(SNIPER_RIFLE_NAME_MODIFIED)){
                player.getInventory().setItemInMainHand(createT742KMoriCrossbow());
            }

            Bukkit.getScheduler().cancelTask(finalArmorStandTaskId);
        }, REMOVE_BULLET_AFTER); // 3 seconds (20 ticks per second)

        // Delay after shoot
        delayAfterShootIsDone = false;
        new BukkitRunnable() {
            @Override
            public void run() {
                delayAfterShootIsDone = true;
            }
        }.runTaskLater(plugin, DELAY_AFTER_SHOOT);
    }


    private void updateNextLocation(Vector direction) {
        if (nextLocation != null && direction != null) {
            nextLocation.add(direction);
        }
    }

    // Method to update the rotation of the armor stand to face a given direction
    private void updateArmorStandRotation(ArmorStand armorStand, Vector direction) {
        double x = direction.getX();
        double z = direction.getZ();
        double theta = Math.atan2(-x, z); // Calculate the angle between z-axis and direction vector
        theta += Math.PI / 2; // Adjust the angle to align with Bukkit's coordinate system
        theta *= -180 / Math.PI; // Convert radians to degrees
        Location loc = armorStand.getLocation();
        loc.setYaw((float) theta); // Set the yaw (horizontal rotation)
        armorStand.teleport(loc); // Teleport to apply rotation
    }


    private void checkBulletCollision(ArmorStand bullet, boolean isUlting, Player shooter) {
        Location bulletLocation = bullet.getLocation();
        Location nextLocation = bulletLocation.clone().add(bullet.getVelocity());

        // Slightly offset the bullet's ray trace start position upwards to avoid floor clipping
        Location rayStart = bulletLocation.clone().add(0, 0.1, 0);
        bulletLocation = bulletLocation.clone().add(0, -0.4, 0);
        // Create a ray from the adjusted start position to the next location
        RayTraceResult result = rayStart.getWorld().rayTrace(rayStart, bullet.getVelocity(), bullet.getVelocity().length() + 1, FluidCollisionMode.NEVER, true, 0, null);

        // Check for players nearby the bullet's next location
        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            // Skip the shooter
            if (nearbyPlayer.equals(shooter)) {
                continue; // Ignore the shooter
            }

            // Check if the player is within collision range
            if (nearbyPlayer.getLocation().distance(bulletLocation) <= 1.0) { // Adjust the value based on the bullet's speed
                if (!nearbyPlayer.isInvulnerable()) {
                    // Apply damage based on whether it's an ult
                    if (isUlting) {
                        System.out.println("is Ulting B2 : " + isUlting);
                        nearbyPlayer.damage(DAMAGE_OF_BULLET_ULTA);
                        nearbyPlayer.getWorld().playSound(nearbyPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                    } else {
                        nearbyPlayer.damage(DAMAGE_OF_BULLET);
                        nearbyPlayer.getWorld().playSound(nearbyPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                    }
                }
                bullet.remove(); // Remove the bullet
                return;
            }
        }

        // Check if the ray hits a block
        if (result != null && result.getHitBlock() != null) {
            // Block is hit, remove the bullet
            bullet.remove();
            // Play a sound indicating the collision with the block
            bulletLocation.getWorld().playSound(bulletLocation, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
            return;
        }
    }



    private Player GetPlayerNearbyOfBullet(Location location, Player owner) {
        //System.out.println("B1");
        for (Player player : Bukkit.getOnlinePlayers()) {
            //System.out.println("B3");
            if (player.getLocation().distance(location) <= DISTANCE_DETECT_FROM_BULLET && player != null && !isAlly(player, owner)) {
                //System.out.println("B3");
                return player;
            }
        }
        return null;
    }

    private boolean isPlayerNearbyOfBullet(Location location, Player owner) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(location) <= DISTANCE_DETECT_FROM_BULLET && player != null && !isAlly(player, owner)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlly(Player player, Player placer) {
        // Check if the player is in the same team as the placer
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        Team placerTeam = placer.getScoreboard().getPlayerTeam(placer);
        Team playerTeam = player.getScoreboard().getPlayerTeam(player);
        return placerTeam != null && playerTeam != null && placerTeam.equals(playerTeam);
    }

    private static ArmorStand SummonArmorStand(Player player, Location eyeLocation, Vector direction) {
        // Spawn an ArmorStand at eye level in front of the player
        ArmorStand armorStand = player.getWorld().spawn(eyeLocation.add(direction), ArmorStand.class);
        armorStand.setVisible(false); // Make the ArmorStand invisible
        armorStand.setGravity(true); // Disable gravity for the ArmorStand
        armorStand.setSmall(true);

        // armorStand.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, 255, true, false));

        // Give the ArmorStand a red dye renamed as "Bullet"
        ItemStack bullet = new ItemStack(Material.RED_DYE);
        ItemMeta bulletMeta = bullet.getItemMeta();
        bulletMeta.displayName(Component.text("Bullet"));
        bullet.setItemMeta(bulletMeta);
        armorStand.getEquipment().setItemInMainHand(bullet);

        // Add the tag "bullet" to the ArmorStand
        armorStand.addScoreboardTag("bullet");

        return armorStand;
    }


    //Aiming System Aiming System Aiming System Aiming System Aiming System
    private ItemStack createT742KMoriCrossbow() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        crossbow.setDurability((short) crossbow.getType().getMaxDurability());
        ItemMeta meta = crossbow.getItemMeta();
        meta.displayName(Component.text(SNIPER_RIFLE_NAME));
        crossbow.setItemMeta(meta);
        ItemStack arrow = new ItemStack(Material.ARROW);
        CrossbowMeta crossbowMeta = (CrossbowMeta) crossbow.getItemMeta();
        crossbowMeta.addChargedProjectile(arrow);
        crossbow.setItemMeta(crossbowMeta);

        return crossbow;
    }

    private ItemStack createT742KMoriCrossbowModified() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        crossbow.setDurability((short) crossbow.getType().getMaxDurability());
        ItemMeta meta = crossbow.getItemMeta();
        meta.displayName(Component.text(SNIPER_RIFLE_NAME_MODIFIED));
        crossbow.setItemMeta(meta);
        ItemStack arrow = new ItemStack(Material.ARROW);
        CrossbowMeta crossbowMeta = (CrossbowMeta) crossbow.getItemMeta();
        crossbowMeta.addChargedProjectile(arrow);
        crossbow.setItemMeta(crossbowMeta);

        return crossbow;
    }

    private ItemStack createT742KMoriCrossbowUltra() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        crossbow.setDurability((short) crossbow.getType().getMaxDurability());
        ItemMeta meta = crossbow.getItemMeta();
        meta.displayName(Component.text(SNIPER_RIFLE_NAME_ULTRA));
        crossbow.setItemMeta(meta);
        ItemStack arrow = new ItemStack(Material.ARROW);
        CrossbowMeta crossbowMeta = (CrossbowMeta) crossbow.getItemMeta();
        crossbowMeta.addChargedProjectile(arrow);
        crossbow.setItemMeta(crossbowMeta);

        return crossbow;
    }


    private void removePumpkinAndEffect(Player player) {
        if (wearingPumpkin) {
            player.getInventory().setHelmet(new ItemStack(Material.AIR));
            wearingPumpkin = false;
            if (slowEffect != null) {
                player.removePotionEffect(slowEffect.getType());
                slowEffect = null;
            }
        }
    }

    //Finding items which wanna change, making it active slot, and replacing it
    private void switchItemTo(Player player, String which, ItemStack to) {
        switchActiveSlotTo(player, which);
        player.getInventory().setItemInMainHand(to);
    }


    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (wearingPumpkin && event.isSneaking() && player.getScoreboardTags().contains("LadyNagan")) {
            setSlowEffect(player, 5);
        }
        if (wearingPumpkin && !event.isSneaking() && slowEffect != null) {
            player.removePotionEffect(slowEffect.getType());
            setSlowEffect(player, 3);
        }
    }


    public void setSlowEffect(Player player, int level) {
        if(player.getScoreboardTags().contains("LadyNagan")){
            slowEffect = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, level);
            player.addPotionEffect(slowEffect);
        }
    }

    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player, String nameOfItem, Material material) {
        return ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == material &&
                player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(nameOfItem));
    }

    private boolean checkEventForRightClickOnCrossbow(PlayerInteractEvent event, Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand != null && itemInMainHand.getType() == Material.CROSSBOW && itemInMainHand.hasItemMeta()) {
            ItemMeta meta = itemInMainHand.getItemMeta();
            return meta.hasDisplayName() && (meta.getDisplayName().equals(SNIPER_RIFLE_NAME) || meta.getDisplayName().equals(SNIPER_RIFLE_NAME_MODIFIED));
        }
        return false;
    }

    private boolean checkEventForRightClickOnCrossbowUltra(PlayerInteractEvent event, Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand != null && itemInMainHand.getType() == Material.CROSSBOW && itemInMainHand.hasItemMeta()) {
            ItemMeta meta = itemInMainHand.getItemMeta();
            return meta.hasDisplayName() && meta.getDisplayName().equals(SNIPER_RIFLE_NAME_ULTRA);
        }
        return false;
    }

    /**
     * Handle right-click action with the T-742K Mori sniper rifle or modified crossbow.
     * This activates the zoom rifle system, putting on the pumpkin helmet and removing it.
     * while preventing fast re-use by introducing a short delay.
     **/
    private void zoomRifleSystem(PlayerInteractEvent event, Player player) {

        //Checking if in zoom or not
        if (!wearingPumpkin) { // if not in zoom
            // Equip a pumpkin as a helmet for the player
            ItemStack pumpkinHelmet = new ItemStack(Material.CARVED_PUMPKIN);
            player.getInventory().setHelmet(pumpkinHelmet);
            wearingPumpkin = true;
            setSlowEffect(player, 3);

                // Replace the stick with a loaded crossbow
                ItemStack crossbow = createT742KMoriCrossbow();
                player.getInventory().setItemInMainHand(crossbow);
        } else { // if in zoom
            removePumpkinAndEffect(player); // wearingPumpkin = false;
            //changed
            if (player.getScoreboardTags().contains("LadyNagan")) {
                player.getInventory().setItemInMainHand(SniperGive.getItem(player));
            }
        }
        //delay after pumpkin for shoot that u cannot shoot instantly

        new BukkitRunnable() {
            @Override
            public void run() {
                delayAfterPumpkinIsDone = true;
            }
        }.runTaskLater(plugin, DELAY_AFTER_PUMPKIN);
    }

    /**
     * Handles the shooting action for the player, triggering the weapon change and bullet direction.
     * The system checks for nearby bullet interactions and applies a direction change if necessary.
     * If the shoot delay has passed, it summons an armor stand and initiates the shooting action,
     * followed by switching the player's crossbow to the modified version.
     */
    private void shootSystem(Player player, PlayerInteractEvent event){
        event.setCancelled(true);

        changedDirectionOfBullet = false;
        //System.out.println("A1");
        if (!delayAfterShootIsDone) {
            armorLocation = armorStand.getLocation();
            if (isPlayerNearbyOfBullet(armorLocation, player) && armorLocation != null) {
                changedDirectionOfBullet = true;
                ItemStack crossbow = createT742KMoriCrossbow();
                player.getInventory().setItemInMainHand(crossbow);
                //System.out.println("A3");
            }

        }
        //System.out.println("A4" + armorLocation);
        if (delayAfterShootIsDone) {
            //System.out.println("A2");
            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();
            armorStand = SummonArmorStand(player, eyeLocation, direction);
            shoot(player, armorStand, direction, isUlting, event);
            ItemStack crossbow = createT742KMoriCrossbowModified();
            player.getInventory().setItemInMainHand(crossbow);
        }
    }
    private void cancelUlt(Player player){
        //System.out.println("UltraCanceled   A1????");
        //Cancelling ult
        isUltaCanceled = true;

        //Pumpkin remove
        removePumpkinAndEffect(player);

        //giving back mainSniperRiffle
        switchItemTo(player, SNIPER_RIFLE_NAME_ULTRA, SniperGive.getItem(player));

        isUlting = false;
    }

    /**
     * ULT - sniper riffle shooting ult bullet (more damage)
     * after RBM -> zooming -> making sound a hero speach and waiting for ~3 sec -> shooting
     *                      |<--------------------------------------------------->|
     *                           3 sec - if press again RBM u can cancel shoot
     *                                     but not the speech
     *
     * if(isUlting) than
     */
    private void ultShootSystem(PlayerInteractEvent event, Player player){
        if (!wearingPumpkin) {
            player.getWorld().playSound(player.getLocation(),"ladynagan.ultaln",1.0f, 1.0f);
            switchActiveSlotTo(player, SNIPER_RIFLE_NAME);
            ItemStack pumpkinHelmet = new ItemStack(Material.CARVED_PUMPKIN);
            player.getInventory().setHelmet(pumpkinHelmet);
            wearingPumpkin = true;
            player.getInventory().setItemInMainHand(createT742KMoriCrossbowUltra());
            event.setCancelled(true);
            isUlting = true;
            System.out.println(" - is ulting ? = " + isUltaCanceled);
            System.out.println(" -hand  ? = " + player.getInventory().getItemInMainHand());
            delayForUlta(player,"UltaBulletLadyNagan",ULT_SLOT,DELAY_AFTER_ULTA);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isUltaCanceled) {
                        //shoot system
                        Location eyeLocation = player.getEyeLocation();
                        Vector direction = eyeLocation.getDirection();
                        armorStand = SummonArmorStand(player, eyeLocation, direction);
                        shoot(player, armorStand, direction, isUlting, event);
                        ItemStack crossbow = createT742KMoriCrossbowModified();
                        player.getInventory().setItemInMainHand(crossbow);
                        isUltaCanceled = true;
                        isUlting = false;
                    }
                    //ult was canceled
                    else {
                        isUltaCanceled = false;
                    }

                }
            }.runTaskLater(plugin, 40L);
        }
    }

    /**
     * Prevents multiple interactions from occurring in quick succession by setting a flag.
     * The flag `isInteracted` is set to `true` to block further interactions temporarily,
     * and is reset to `false` after a short delay of 2 ticks (0.1 seconds) to allow for future interactions.
     */
    private void notInteract(){
        isInteracted = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                isInteracted = false;
            }
        }.runTaskLater(plugin, 2); // 2 ticks = 0.1 seconds
    }
}

