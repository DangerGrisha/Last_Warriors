package greg.pirat1c.humiliation.StaticInventory;

import java.util.*;

public enum PlayerClass {
    LADY_NAGAN("LadyNagan", Arrays.asList(0, 2, 4, 6, 7, 8)),
    SWORDSMAN("Clear", List.of()),
    SASKE("Saske", Arrays.asList(0, 2, 4, 6, 7, 8));

    private final String tag;
    private final List<Integer> staticSlots;

    PlayerClass(String tag, List<Integer> staticSlots) {
        this.tag = tag;
        this.staticSlots = staticSlots;
    }

    public String getTag() {
        return tag;
    }

    public List<Integer> getStaticSlots() {
        return staticSlots;
    }

    public static PlayerClass fromTag(String tag) {
        for (PlayerClass clazz : values()) {
            if (clazz.getTag().equalsIgnoreCase(tag)) {
                return clazz;
            }
        }
        return null;
    }
}
