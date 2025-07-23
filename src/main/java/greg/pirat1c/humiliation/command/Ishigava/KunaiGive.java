package greg.pirat1c.humiliation.command.Ishigava;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class KunaiGive implements CommandExecutor {
    private static final Material material = Material.BOW;
    private static final String displayName = "Kunai";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        player.getInventory().addItem(getItem());
        return true;
    }

    public static ItemStack getItem() {
        ItemStack bow = new ItemStack(material, 1);
        ItemMeta meta = bow.getItemMeta();
        if (meta != null) {
            // Название предмета
            meta.displayName(Component.text(displayName));
            meta.setUnbreakable(true);

            // Урон как у IRON_SWORD (6.0)
            AttributeModifier damage = new AttributeModifier(
                    UUID.randomUUID(),
                    "generic.attack_damage",
                    6.0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damage);

            // Скорость атаки как у IRON_SWORD (1.6)
            AttributeModifier speed = new AttributeModifier(
                    UUID.randomUUID(),
                    "generic.attack_speed",
                    1.6 - 4.0, // базовая скорость руки 4.0, надо уменьшить до 1.6
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speed);

            bow.setItemMeta(meta);
        }
        return bow;
    }
}
