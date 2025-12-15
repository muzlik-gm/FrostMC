package com.muzlik.util;

import com.muzlik.fragment.FragmentType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class FragmentUtil {

    private FragmentUtil() {
        // Private constructor to prevent instantiation
    }

    public static FragmentType getFragmentTypeFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return null;
        }

        int modelData = meta.getCustomModelData();

        for (FragmentType type : FragmentType.values()) {
            if (type.getCustomModelData() == modelData) {
                return type;
            }
        }

        return null;
    }
}
