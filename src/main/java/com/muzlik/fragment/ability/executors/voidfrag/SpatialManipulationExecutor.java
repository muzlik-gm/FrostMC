package com.muzlik.fragment.ability.executors.voidfrag;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Spatial Manipulation - Void Fragment Slot 3 Ability
 * 
 * Creates portal pairs for teleportation with 20 block max range.
 * Max 2 portals per player, 10 second duration.
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5
 */
public class SpatialManipulationExecutor implements AbilityExecutor {
    
    private final FrostSMPPlugin plugin;
    
    // Track active portals per player (max 2)
    private static final Map<UUID, List<Portal>> activePortals = new HashMap<>();
    
    private static final int MAX_PORTALS_PER_PLAYER = 2;
    private static final double MAX_RANGE = 20.0;
    private static final int DURATION_TICKS = 200; // 10 seconds
    
    public SpatialManipulationExecutor(FrostSMPPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Get target location (where player is looking) - FIXED: Prioritize entity targeting
        Location targetLoc = com.muzlik.fragment.ability.TargetingUtil.getGroundTargetLocation(player, MAX_RANGE);
        
        // Check range
        if (player.getLocation().distance(targetLoc) > MAX_RANGE) {
            player.sendMessage("§5✗ Target too far! Max range: " + MAX_RANGE + " blocks");
            return;
        }
        
        // Get or create portal list for player
        List<Portal> portals = activePortals.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        
        // Check if player already has max portals
        if (portals.size() >= MAX_PORTALS_PER_PLAYER) {
            // Remove oldest portal
            Portal oldest = portals.remove(0);
            oldest.cancel();
        }
        
        // Create new portal
        Location portalLoc = targetLoc.clone().add(0.5, 1, 0.5);
        Portal portal = new Portal(player, portalLoc, rank);
        portals.add(portal);
        
        // If this is the second portal, link them
        if (portals.size() == 2) {
            Portal portal1 = portals.get(0);
            Portal portal2 = portals.get(1);
            portal1.setLinkedPortal(portal2);
            portal2.setLinkedPortal(portal1);
            
        }
        
        // Start portal
        portal.start();
        
        // Spawn VFX
        VFXLayerBuilder vfx = new VFXLayerBuilder(plugin, portalLoc, rank, player);
        
        vfx.core(Particle.PORTAL, 50, ParticlePattern.RING, 1.0, 1.0, 1.0, 0.1, null);
        vfx.secondary(Particle.REVERSE_PORTAL, 30, ParticlePattern.SPIRAL, 0.8, 2.0, 0.8, 0.05, null);
        vfx.ambient(Particle.SMOKE_NORMAL, 10, ParticlePattern.SPHERE, 1.5, 1.5, 1.5, 0.01, null);
        
        if (rank >= 8) {
            vfx.cinematic(0.25, com.muzlik.vfx.CinematicEffect.REALITY_WARP);
        }
        
        vfx.spawn();
        
        // Play sound
        player.getWorld().playSound(portalLoc, Sound.BLOCK_PORTAL_TRIGGER, 1.5f, 1.2f);
        
    }
    
    /**
     * Portal class
     */
    private class Portal {
        private final Player owner;
        private final Location location;
        private final int rank;
        private Portal linkedPortal;
        private BukkitRunnable task;
        private final Set<UUID> recentlyTeleported;
        
        public Portal(Player owner, Location location, int rank) {
            this.owner = owner;
            this.location = location;
            this.rank = rank;
            this.recentlyTeleported = new HashSet<>();
        }
        
        public void setLinkedPortal(Portal portal) {
            this.linkedPortal = portal;
        }
        
        public void start() {
            task = new BukkitRunnable() {
                private int ticks = 0;
                
                @Override
                public void run() {
                    ticks++;
                    
                    // Check for entities near portal
                    for (Entity entity : location.getWorld().getNearbyEntities(location, 1.5, 2.0, 1.5)) {
                        if (entity instanceof Player) {
                            Player p = (Player) entity;
                            
                            // Don't teleport if recently teleported (prevent loop)
                            if (recentlyTeleported.contains(p.getUniqueId())) {
                                continue;
                            }
                            
                            // Teleport if linked portal exists
                            if (linkedPortal != null) {
                                teleport(p);
                            }
                        }
                    }
                    
                    // Spawn portal particles
                    location.getWorld().spawnParticle(Particle.PORTAL, location, 20, 0.5, 1.0, 0.5, 0.1);
                    
                    // Expire after duration
                    if (ticks >= DURATION_TICKS) {
                        cancel();
                        removePortal();
                    }
                }
            };
            
            task.runTaskTimer(plugin, 0L, 1L);
        }
        
        private void teleport(Player player) {
            if (linkedPortal == null) {
                return;
            }
            
            // Teleport to linked portal
            Location dest = linkedPortal.location.clone();
            dest.setYaw(player.getLocation().getYaw());
            dest.setPitch(player.getLocation().getPitch());
            
            player.teleport(dest);
            
            // Mark as recently teleported
            recentlyTeleported.add(player.getUniqueId());
            linkedPortal.recentlyTeleported.add(player.getUniqueId());
            
            // Clear after 2 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                recentlyTeleported.remove(player.getUniqueId());
                linkedPortal.recentlyTeleported.remove(player.getUniqueId());
            }, 40L);
            
            // Effects
            player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.PORTAL, dest, 50, 0.5, 1.0, 0.5, 0.2);
            
        }
        
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }
        
        private void removePortal() {
            List<Portal> portals = activePortals.get(owner.getUniqueId());
            if (portals != null) {
                portals.remove(this);
                if (portals.isEmpty()) {
                    activePortals.remove(owner.getUniqueId());
                }
            }
        }
    }
}
