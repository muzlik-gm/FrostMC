package com.muzlik.fragment.ability.executors.earth;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.block.PacketBlockCollider;
import com.muzlik.block.PacketBlockManager;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.fragment.ability.AbilityScaling;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Terra Shaper - Earth Fragment Slot 3 Ability
 * 
 * Creates temporary ramps and ridges using packet-only blocks with armor stand colliders.
 * 
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
public class TerraShaperExecutor implements AbilityExecutor {
    
    private final FrostSMPPlugin plugin;
    private final PacketBlockManager packetBlockManager;
    private final PacketBlockCollider colliderSystem;
    
    public TerraShaperExecutor(FrostSMPPlugin plugin, PacketBlockManager packetBlockManager, 
                              PacketBlockCollider colliderSystem) {
        this.plugin = plugin;
        this.packetBlockManager = packetBlockManager;
        this.colliderSystem = colliderSystem;
    }
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate scaled parameters
        // Length: 4 + (rank / 2) blocks
        int length = 4 + (rank / 2);
        
        // Duration: 6 + (rank * 1) seconds
        double durationSeconds = 6.0 + rank;
        int durationTicks = (int) (durationSeconds * 20);
        
        // Get player facing direction
        Vector direction = player.getLocation().getDirection();
        direction.setY(0);
        direction.normalize();
        
        Location startLoc = player.getLocation().clone();
        
        // Launch block projectiles that rise up to form the ramp
        com.muzlik.block.BlockManipulationEngine blockEngine = plugin.getBlockManipulationEngine();
        int blocksLaunched = 0;
        int maxBlocks = 3 + rank; // 3-6 blocks based on rank
        
        for (int i = 0; i < Math.min(length, maxBlocks); i++) {
            Location blockSpawn = startLoc.clone().add(direction.clone().multiply(i));
            blockSpawn.setY(blockSpawn.getY() - 1); // Start below ground
            
            // Calculate upward direction with slight forward motion
            Vector upDirection = new Vector(direction.getX() * 0.2, 1.0, direction.getZ() * 0.2).normalize();
            
            // Create block controller config
            com.muzlik.block.BlockControllerConfig config = new com.muzlik.block.BlockControllerConfig.Builder()
                .ownerUUID(player.getUniqueId())
                .abilityId("earth_terra_shaper")
                .startLocation(blockSpawn)
                .direction(upDirection)
                .baseSpeed(0.2)
                .accelerationFactor(1.02)
                .maxSpeed(0.6)
                .maxLifeTicks(30) // 1.5 seconds
                .shellType(com.muzlik.block.ShellType.FALLING_BLOCK)
                .blockType(Material.STONE)
                .collisionRadius(0.5)
                .baseDamage(0) // No damage, just visual
                .fragmentRank(rank)
                .build();
            
            // Spawn block projectile with slight delay
            int delay = i * 2; // Stagger spawns
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                blockEngine.spawnController(config);
            }, delay);
            
            blocksLaunched++;
        }
        
        // Create packet block group
        UUID groupId = packetBlockManager.createBlockGroup(player.getUniqueId(), durationTicks);
        
        // Build ramp
        List<Location> rampLocations = new ArrayList<>();
        BlockData stoneData = Material.STONE.createBlockData();
        
        for (int i = 0; i < length; i++) {
            // Calculate position along ramp
            Location blockLoc = startLoc.clone().add(direction.clone().multiply(i));
            
            // Calculate height (gradual incline)
            int height = i / 2;
            
            // Place blocks at this position
            for (int h = 0; h <= height; h++) {
                Location placeLoc = blockLoc.clone().add(0, h, 0);
                placeLoc.setX(Math.floor(placeLoc.getX()));
                placeLoc.setY(Math.floor(placeLoc.getY()));
                placeLoc.setZ(Math.floor(placeLoc.getZ()));
                
                // Add to packet block group
                packetBlockManager.addBlock(groupId, placeLoc, stoneData);
                rampLocations.add(placeLoc);
            }
        }
        
        // Send packet blocks to nearby players
        packetBlockManager.sendToNearbyPlayers(groupId, startLoc, 32.0);
        
        // Create armor stand colliders
        colliderSystem.createColliders(groupId, rampLocations);
        
        // Spawn 5-layer VFX
        VFXLayerBuilder vfx = new VFXLayerBuilder(plugin, startLoc, rank, player);
        
        // Core: BLOCK_CRACK particles
        vfx.core(Particle.BLOCK_CRACK, 30, ParticlePattern.LINE, 
                direction.getX() * length, 0.5, direction.getZ() * length, 0.0, 
                Material.STONE.createBlockData());
        
        // Secondary: BLOCK_DUST trail
        vfx.secondary(Particle.BLOCK_DUST, 20, ParticlePattern.SPIRAL,
                     0.5, 2.0, 0.5, 0.05, Material.STONE.createBlockData());
        
        // Ambient: Ground rumble
        vfx.ambient(Particle.SMOKE_NORMAL, 15, ParticlePattern.RING,
                   2.0, 0.1, 2.0, 0.01, null);
        
        // Impact: Debris burst
        vfx.impact(Particle.BLOCK_CRACK, 40, ParticlePattern.BURST,
                  1.0, 1.0, 1.0, 0.1, Material.STONE.createBlockData());
        
        // Cinematic: Ground shake at rank 4+
        if (rank >= 4) {
            vfx.cinematic(0.2, com.muzlik.vfx.CinematicEffect.SCREEN_SHAKE);
        }
        
        vfx.spawn();
        
        // Play sound
        player.getWorld().playSound(startLoc, Sound.BLOCK_STONE_PLACE, 2.0f, 0.8f);
        player.getWorld().playSound(startLoc, Sound.BLOCK_GRAVEL_BREAK, 1.5f, 0.6f);
        
        // Send message

        
        // Schedule cleanup
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            packetBlockManager.revertGroup(groupId);
            colliderSystem.removeColliders(groupId);
        }, durationTicks);
    }
}
