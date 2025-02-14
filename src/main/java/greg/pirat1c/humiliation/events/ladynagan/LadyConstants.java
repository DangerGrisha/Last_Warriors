package greg.pirat1c.humiliation.events.ladynagan;

import org.bukkit.Material;

public interface LadyConstants {
    String LADY_TAG = "LadyNagan";

    //SNIPER RIFFLE
    String SNIPER_RIFLE_NAME= "T-742K Mori";
    String SNIPER_RIFLE_NAME_MODIFIED = "T-742K Mori+";
    int DISTANCE_DETECT_FROM_BULLET = 10; // distance at which bullet can detect u and rotate direction to u
    double DAMAGE_OF_BULLET = 5.0;
    long REMOVE_BULLET_AFTER = 80L; //removeBullet after some seconds after shoot 20L - 1s
    long DELAY_AFTER_SHOOT = 80L;
    long DELAY_AFTER_PUMPKIN = 10L; // delay after u get pumpkin for shoot
    //ULTA
    String SNIPER_RIFLE_NAME_ULTRA = "T-742K Destroy";
    String NAME_OF_ULTRA_BUTTON = "Ultra Bullet";
    double DAMAGE_OF_BULLET_ULTA = 19;
    int DELAY_AFTER_ULTA = 6; //SECONDS after ulta how mich time do need to refresh ult

    //FLYING ABILITY
    int FLY_COUNTER = 7;
    long FLY_TIME = 400L;
    int COOLDOWN_FLY = 20; //SECONDS

    //TRAP
    double DETECT_RADIUS = 1.5; //detect enemy within radius
    String TRAP_NAME = "Trap";
    float EXPLOSION_POWER = 4f;
    boolean DAMAGE_TERRAIN = false; // Set to true if you want terrain damage
    double DAMAGE_RADIUS = 3.0; // Radius of damage around the explosion
    double DAMAGE_AMOUNT = 10.0; // Amount of damage dealt to enemies
    Material SET_UP_BLOCK = Material.PURPLE_CONCRETE;
    int SLOT_OF_TRAPS = 6;
    int COOLDOWN_SECONDS = 15; // Cooldown for each mine in seconds/

    //SELF EXPLOSION
    float EXPLOSION_RADIUS_SE = 6.0f; // Radius of the explosion
    double DAMAGE_AMOUNT_SE = 10.0; // Amount of damage dealt to entities
    boolean DAMAGE_TERRAIN_SE = true; // Set to true if you want terrain damage
    int DELAY_PERK_SE = 20; //Seconds

    //Static Slots 0-8

    int RIFLE_SLOT = 0;
    int FLY_SLOT = 5;
    int TRAPS_SLOT = 6;
    int EXPLOSION_SLOT = 7;
    int ULT_SLOT = 8;

}
