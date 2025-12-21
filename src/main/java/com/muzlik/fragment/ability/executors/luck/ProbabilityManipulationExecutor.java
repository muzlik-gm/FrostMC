package com.muzlik.fragment.ability.executors.luck;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.util.PotionEffectHelper;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Probability Manipulation - Luck Fragment Mastery Ability
 * Influences random outcomes (fishing, enchanting, loot)
 * 
 * Stats:
 * - Mana Cost: 55 - 5*rank
 * - Cooldown: 50 - 4*rank seconds
 * - Duration: 20 + 3*rank seconds
 * - Luck Amplifier: rank/2 (rounded down)
 * - Fishing Treasure Chance: +15% + 5%*rank
 * - Enchantment Level Bonus: +1 per 2 ranks
 * 
 * VFX: Green probability wave particles
 * Sound: BLOCK_ENCHANTMENT_TABLE_USE
 */
public class ProbabilityManipulationExecutor implements AbilityExecutor {
    
    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final java.util.Map<UUID, Integer> playerRanks = new java.util.HashMap<>();
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate duration and amplifier based on rank
        int durationTicks = (20 + (3 * rank)) * 20; // Convert to ticks
        int luckAmplifier = rank / 2; // Rounded down
        
        // Apply Luck effect with hidden particles
        player.addPotionEffect(PotionEffectHelper.createHiddenEffect(
            PotionEffectType.LUCK,
            durationTicks,
            luckAmplifier
        ));
        
        // Store player in active set with rank
        activePlayers.add(player.getUniqueId());
        playerRanks.put(player.getUniqueId(), rank);
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Spawn green probability wave particles
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationTicks;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline() || player.isDead()) {
                    // Remove player from active set
                    activePlayers.remove(player.getUniqueId());
                    playerRanks.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                
                // Spawn green probability wave particles
                Location playerLoc = player.getLocation().add(0, 1, 0);
                
                // Create wave effect expanding outward
                double waveRadius = (ticks % 40) * 0.1;
                int points = 16;
                
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i) / points;
                    double x = playerLoc.getX() + Math.cos(angle) * waveRadius;
                    double z = playerLoc.getZ() + Math.sin(angle) * waveRadius;
                    
                    Location particleLoc = new Location(
                        playerLoc.getWorld(),
                        x,
                        playerLoc.getY(),
                        z
                    );
                    
                    // Green dust particles (RGB: 0, 255, 0)
                    playerLoc.getWorld().spawnParticle(
                        Particle.REDSTONE,
                        particleLoc,
                        1,
                        0, 0, 0,
                        0,
                        new Particle.DustOptions(
                            org.bukkit.Color.fromRGB(0, 255, 0),
                            0.9f
                        )
                    );
                    
                    // Add enchantment particles for probability effect
                    if (i % 4 == 0) {
                        playerLoc.getWorld().spawnParticle(
                            Particle.ENCHANTMENT_TABLE,
                            particleLoc,
                            2,
                            0.1, 0.3, 0.1,
                            0.5
                        );
                    }
                }
                
                ticks += 8;
            }
        }.runTaskTimer(plugin, 0L, 8L);
        
        // Schedule removal from active set
        new BukkitRunnable() {
            @Override
            public void run() {
                activePlayers.remove(player.getUniqueId());
                playerRanks.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, durationTicks);
        
        // Play activation sound
        player.getWorld().playSound(
            player.getLocation(),
            Sound.BLOCK_ENCHANTMENT_TABLE_USE,
            1.0f,
            1.0f
        );
        
        player.sendMessage("§aProbability Manipulation activated!");
    }
    
    /**
     * Listener for handling probability manipulation effects
     */
    public static class ProbabilityManipulationListener implements Listener {
        
        @EventHandler
        public void onPlayerFish(PlayerFishEvent event) {
            Player player = event.getPlayer();
            
            // Check if player has Probability Manipulation active
            if (!activePlayers.contains(player.getUniqueId())) {
                return;
            }
            
            // Only affect successful catches
            if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
                return;
            }
            
            int rank = playerRanks.getOrDefault(player.getUniqueId(), 1);
            
            // Calculate treasure chance bonus
            double treasureChanceBonus = 0.15 + (0.05 * rank);
            
            // Roll for treasure upgrade
            if (Math.random() < treasureChanceBonus) {
                // Spawn bonus particles
                Location hookLoc = event.getHook().getLocation();
                hookLoc.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    hookLoc,
                    20,
                    0.3, 0.3, 0.3,
                    0,
                    new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(0, 255, 0),
                        1.2f
                    )
                );
                
                hookLoc.getWorld().spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    hookLoc,
                    10,
                    0.3, 0.3, 0.3,
                    0.1
                );
                
                player.sendMessage("§a§l✦ LUCKY CATCH! §7(Enhanced by probability manipulation)");
            }
        }
    }
    
    /**
     * Check if a player has Probability Manipulation active
     * @param playerId The player's UUID
     * @return True if active
     */
    public static boolean isActive(UUID playerId) {
        return activePlayers.contains(playerId);
    }
    
    /**
     * Get the rank of a player's active Probability Manipulation
     * @param playerId The player's UUID
     * @return The rank, or 0 if not active
     */
    public static int getRank(UUID playerId) {
        return playerRanks.getOrDefault(playerId, 0);
    }
    
    /**
     * Get the enchantment level bonus for a player
     * @param playerId The player's UUID
     * @return The enchantment level bonus
     */
    public static int getEnchantmentBonus(UUID playerId) {
        if (!activePlayers.contains(playerId)) {
            return 0;
        }
        int rank = playerRanks.getOrDefault(playerId, 1);
        return rank / 2; // +1 per 2 ranks
    }
}
