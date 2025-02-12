//package greg.pirat1c.humiliation.events.ishigava;
//
//import com.comphenix.protocol.PacketType;
//import com.comphenix.protocol.ProtocolLibrary;
//import com.comphenix.protocol.ProtocolManager;
//import com.comphenix.protocol.events.PacketContainer;
//import com.comphenix.protocol.wrappers.WrappedGameProfile;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.World;
//import org.bukkit.entity.*;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.block.Action;
//import org.bukkit.event.entity.ProjectileHitEvent;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.metadata.FixedMetadataValue;
//import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.util.EulerAngle;
//import org.bukkit.util.Vector;
//
//import java.util.List;
//import java.util.UUID;
//
//public class ClonesListener implements Listener {
//
//    private JavaPlugin plugin;
//
//    public ClonesListener(JavaPlugin plugin) {
//        this.plugin = plugin;
//    }
//
//    @EventHandler
//    public void onPlayerInteract(PlayerInteractEvent event) {
//        Player player = event.getPlayer();
//
//        if (checkEventForRightClick(event, player)) {
//            // Проверяем, есть ли у игрока в руках какой-то предмет (например, магическая палочка)
//            if (player.getInventory().getItemInMainHand().getType() == Material.RED_DYE) {
//                spawnCopies(player.getLocation(), player);
//            }
//        }
//    }
//    public void spawnCopies(Location location, Player player) {
//        World world = location.getWorld();
//        // Создание копий слева и справа от первоначальной позиции
//        Location left = location.clone().add(-1, 0, 0);  // Сдвиг на один блок влево
//        Location right = location.clone().add(1, 0, 0);  // Сдвиг на один блок вправо
//        spawnNPC(left, player);
//        spawnNPC(right, player);
//        System.out.println("Clones Good");
//
//    }
//
//    public void spawnNPC(Location location, Player player) {
//        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
//
//        try {
//            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
//            packet.getIntegers().write(0, 12345);  // ID NPC
//            packet.getUUIDs().write(0, UUID.randomUUID());  // Случайный UUID для NPC
//            packet.getStrings().write(0, "CustomNPCName");  // Имя NPC
//
//            protocolManager.sendServerPacket(player, packet);
//
//            PacketContainer entityPacket = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
//            entityPacket.getIntegers().write(0, 12345);  // ID того же NPC
//            entityPacket.getUUIDs().write(0, packet.getUUIDs().read(0));
//            entityPacket.getDoubles()
//                    .write(0, location.getX())
//                    .write(1, location.getY())
//                    .write(2, location.getZ());
//
//            protocolManager.sendServerPacket(player, entityPacket);
//            System.out.println("Npc good");
//        } catch (Exception e) {
//            System.out.println("Npc NOT good");
//            e.printStackTrace();  // Логгирование ошибки
//        }
//    }
//
//
//
//
//
//    private boolean checkEventForRightClick(PlayerInteractEvent event, Player player) {
//        return (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
//                player.getInventory().getItemInMainHand().hasItemMeta() &&
//                player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
//                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Clones");
//    }
//}
