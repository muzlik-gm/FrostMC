package com.muzlik.fragment.ability.executors.time;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Temporal Rewind - Time Fragment Advanced Ability
 * Restores player health and position from 5 seconds ago
 * 
 * Stats:
 * - Mana Cost: 60 - 5*rank
 * - Cooldown: 45 - 3*rank seconds
 * - Rewind Time: 5 seconds
 * - Health Restoration: 100% of health from 5 seconds ago
 * - Position Restoration: Exact coordinates from 5 seconds ago
 * 
 * VFX: Yellow spiral particles showing rewind path
 * Sound: BLOCK_PORTAL_TRAVEL at high pitch (2.0f) for reversed effect
 */
public class TemporalRewindExecutor implements AbilityExecutor {
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Get plugin instance
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Get or create temporal data for this player
        TemporalRewindData temporalData = TemporalRewindData.getOrCreate(player);
        
        // Start recording if not already recording
        if (temporalData.getHistorySize() == 0) {
            temporalData.startRecording(player, plugin);
            player.sendMessage("§eStarting temporal recording... Use this ability again in 5+ seconds.");
            return;
        }
        
        // Check if we have sufficient history (5 seconds)
        PlayerState rewindState;
        if (temporalData.hasSufficientHistory(5)) {
            rewindState = temporalData.getStateFromSecondsAgo(5);
        } else {
            // Fallback: use most recent state or spawn
            rewindState = temporalData.getMostRecentState();
            if (rewindState == null) {
                player.sendMessage("§cInsufficient temporal history!");
                return;
            }
            player.sendMessage("§eInsufficient history, rewinding to earliest recorded state...");
        }
        
        // Store current location for particle trail
        Location currentLoc = player.getLocation().clone();
        Location rewindLoc = rewindState.getLocation();
        
        // Restore health
        double newHealth = Math.min(rewindState.getHealth(), player.getMaxHealth());
        player.setHealth(newHealth);
        
        // Teleport player to past location
        player.teleport(rewindLoc);
        
        // Spawn yellow spiral particles showing rewind path
        new BukkitRunnable() {
            int step = 0;
            final int maxSteps = 20;
            
            @Override
            public void run() {
                if (step >= maxSteps) {
                    cancel();
                    return;
                }
                
                // Interpolate between current and rewind location
                double progress = (double) step / maxSteps;
                double x = currentLoc.getX() + (rewindLoc.getX() - currentLoc.getX()) * progress;
                double y = currentLoc.getY() + (rewindLoc.getY() - currentLoc.getY()) * progress;
                double z = currentLoc.getZ() + (rewindLoc.getZ() - currentLoc.getZ()) * progress;
                
                // Create spiral effect
                double angle = step * 0.5;
                double radius = 0.5;
                double spiralX = x + Math.cos(angle) * radius;
                double spiralY = y + 0.5;
                double spiralZ = z + Math.sin(angle) * radius;
                
                Location particleLoc = new Location(currentLoc.getWorld(), spiralX, spiralY, spiralZ);
                
                // Yellow dust particles (RGB: 255, 255, 0)
                currentLoc.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    particleLoc,
                    3,
                    0.1, 0.1, 0.1,
                    0,
                    new Particle.DustOptions(
                        org.bukkit.Color.fromRGB(255, 255, 0),
                        1.2f
                    )
                );
                
                // Add portal particles for time effect
                currentLoc.getWorld().spawnParticle(
                    Particle.PORTAL,
                    particleLoc,
                    5,
                    0.2, 0.2, 0.2,
                    0.5
                );
                
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Play time-reversal sound effect
        player.getWorld().playSound(
            currentLoc,
            Sound.BLOCK_PORTAL_TRAVEL,
            1.0f,
            2.0f // High pitch for reversed effect
        );
        
        player.getWorld().playSound(
            rewindLoc,
            Sound.BLOCK_PORTAL_TRAVEL,
            1.0f,
            2.0f
        );
        
        player.sendMessage("§eRewound to " + (temporalData.hasSufficientHistory(5) ? "5" : "~" + temporalData.getHistorySize()) + " seconds ago!");
    }
}
