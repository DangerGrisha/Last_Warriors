package greg.pirat1c.humiliation.entity;

import org.bukkit.event.block.Action;

/**
 * this enum contains the naming for the Action events
 */
public enum MouseButton {
    RIGHT_CLICK, LEFT_CLICK;

    public boolean equalAction(Action action) {
        return action.name().contains(this.name());
    }

}
