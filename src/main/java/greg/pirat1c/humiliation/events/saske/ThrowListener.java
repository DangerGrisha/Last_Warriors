package greg.pirat1c.humiliation.events.saske;

import greg.pirat1c.humiliation.command.SnowShuriken;
import greg.pirat1c.humiliation.events.ladynagan.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;


import static greg.pirat1c.humiliation.events.saske.SaskeConstants.SHURICKEN_NAME;

public class ThrowListener implements Listener {

    private final JavaPlugin plugin;
    private final CooldownManager cooldownManager;

    private static final String ABILITY_NAME = "SHURIKEN";
    private static final int SHURIKEN_SLOT = 2; // слот, где лежит снежок
    private static final long COOLDOWN_SECONDS = 25; // кд между сюрикенами

    public ThrowListener(JavaPlugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItem(SHURIKEN_SLOT);
        if (item == null || item.getType() != Material.SNOWBALL) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        if (!name.equals(SHURICKEN_NAME)) return;

        System.out.println("[DEBUG] Matched shuriken");

        // Задержка в 1 тик, чтобы Minecraft успел убрать снежок из инвентаря
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasShuriken(player)) {
                    System.out.println("[DEBUG] No more shuriken — starting cooldown");
                    coolDownShuriken(player, ABILITY_NAME, SHURIKEN_SLOT, (int) COOLDOWN_SECONDS);
                } else {
                    System.out.println("[DEBUG] Still has shurikens");
                }
            }
        }.runTaskLater(plugin, 1L);
    }



    private void coolDownShuriken(Player player, String abilityId, int inventorySlot, int cooldownSeconds) {
        long endTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);

        // Вставляем тёмное стекло
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Перезарядка...", NamedTextColor.GRAY));
            glass.setItemMeta(meta);
        }
        player.getInventory().setItem(inventorySlot, glass);

        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long remaining = endTime - now;

                if (remaining <= 0) {
                    // ✅ Вернуть сразу 3 сюрикена
                    ItemStack stack = SnowShuriken.getShuriken();
                    stack.setAmount(3);
                    player.getInventory().setItem(inventorySlot, stack);

                    System.out.println("[DEBUG] Вернули 3 сюрикена после кд");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // раз в секунду
    }



    private boolean hasShuriken(Player player) {
        ItemStack item = player.getInventory().getItem(SHURIKEN_SLOT);
        if (item == null || item.getType() != Material.SNOWBALL) return false;

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.displayName().equals(Component.text(SHURICKEN_NAME));
    }
}
