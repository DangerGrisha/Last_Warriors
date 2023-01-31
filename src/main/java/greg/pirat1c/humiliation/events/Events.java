package greg.pirat1c.humiliation.events;

import org.bukkit.Material;
import org.bukkit.entity.Entity;

import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import javax.swing.*;


public class Events implements Listener {

    @EventHandler

    public void onSpawn(EntitySpawnEvent event) {

        if (event.getEntity() instanceof Zombie) {
            Zombie zombie = (Zombie) event.getEntity();

            zombie.getEquipment().setHelmet(new ItemStack((Material.DIAMOND_HELMET)));
            zombie.getEquipment().setItemInMainHand(new ItemStack((Material.DIAMOND_SWORD)));
            zombie.getEquipment().setHelmetDropChance(0.1F);
            zombie.getEquipment().setItemInMainHandDropChance(0.5F);
            zombie.getEquipment().setItemInOffHandDropChance(0);
            zombie.getEquipment().setItemInOffHand(new ItemStack((Material.SHIELD)));


        }

    }

//    public void onRightClick(PlayerInteractEvent event) {
//        if (event.getAction() == Action.)
//
//
//    }


}
