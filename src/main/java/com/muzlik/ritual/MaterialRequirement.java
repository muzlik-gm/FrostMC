package com.muzlik.ritual;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a material requirement for rituals.
 * Specifies the material type and quantity needed.
 */
public class MaterialRequirement {
    private final Material material;
    private final int quantity;
    private final String displayName;

    public MaterialRequirement(Material material, int quantity) {
        this.material = material;
        this.quantity = quantity;
        this.displayName = null;
    }

    public MaterialRequirement(Material material, int quantity, String displayName) {
        this.material = material;
        this.quantity = quantity;
        this.displayName = displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : material.name();
    }

    /**
     * Create an ItemStack from this requirement
     * @return ItemStack with the specified material and quantity
     */
    public ItemStack toItemStack() {
        return new ItemStack(material, quantity);
    }

    @Override
    public String toString() {
        return quantity + "x " + getDisplayName();
    }
}
