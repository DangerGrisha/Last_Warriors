package greg.pirat1c.humiliation.entity;

import org.bukkit.Material;

import java.util.Objects;

public class CustomEntity {

    protected Material material;
    protected String lore;
    protected String displayName;

    public CustomEntity(Material material, String lore, String displayName) {
        this.material = material;
        this.lore = lore;
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomEntity that = (CustomEntity) o;
        return material == that.material && Objects.equals(lore, that.lore) && Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, lore, displayName);
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public String getLore() {
        return lore;
    }

    public String getDisplayName() {
        return displayName;
    }
}
