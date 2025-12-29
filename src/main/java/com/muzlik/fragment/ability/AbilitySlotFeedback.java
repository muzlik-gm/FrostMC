package com.muzlik.fragment.ability;

import com.muzlik.fragment.FragmentType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Provides visual and audio feedback when ability slots are unlocked
 */
public class AbilitySlotFeedback {

    /**
     * Play unlock feedback for a player
     */
    public static void playUnlockFeedback(Player player, FragmentType fragmentType, int slotIndex) {
        // Title message
        player.sendTitle(
            "§6§l✦ ABILITY UNLOCKED ✦",
            "§7Slot " + slotIndex + " §8| §b" + fragmentType.getDisplayName(),
            10, 40, 10
        );

        // Sound effects
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);

        // Particle effects
        Location loc = player.getLocation().add(0, 1, 0);
        
        // TOTEM particles (golden burst)
        player.getWorld().spawnParticle(
            Particle.TOTEM,
            loc,
            50,
            0.5, 0.5, 0.5,
            0.1
        );

        // END_ROD particles (white trails)
        player.getWorld().spawnParticle(
            Particle.END_ROD,
            loc,
            30,
            0.3, 0.5, 0.3,
            0.05
        );

        // Chat message
        player.sendMessage("§6§l✦ §eAbility Slot " + slotIndex + " Unlocked!");
        player.sendMessage("§7You can now use this ability with your " + fragmentType.getDisplayName() + " Fragment");
    }
}
