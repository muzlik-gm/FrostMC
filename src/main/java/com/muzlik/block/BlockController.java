package com.muzlik.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Controller for block manipulation projectiles.
 * Uses armor stand as controller with particle or falling block shell.
 */
public class BlockController {
    private final UUID controllerId;
    private final UUID ownerUUID;
    private final String abilityId;
    private final ArmorStand controllerEntity;
    private final ShellType shellType;
    private final Material blockType;
    
    private Vector velocity;
    private Vector direction;
    private double baseSpeed;
    private double accelerationFactor;
    private double maxSpeed;
    private int ticksActive;
    private int maxLifeTicks;
    
    private double collisionRadius;
    private boolean hasCollided;
    private double baseDamage;
    private int fragmentRank;
    
    private FallingBlock fallingBlock;
    private ParticleShellRenderer particleRenderer;
    private ImpactHandler impactHandler;
    
    public BlockController(UUID controllerId, ArmorStand controllerEntity, BlockControllerConfig config) {
        this.controllerId = controllerId;
        this.ownerUUID = config.getOwnerUUID();
        this.abilityId = config.getAbilityId();
        this.controllerEntity = controllerEntity;
        this.shellType = config.getShellType();
        this.blockType = config.getBlockType();
        
        this.direction = config.getDirection().normalize();
        this.baseSpeed = config.getBaseSpeed();
        this.accelerationFactor = config.getAccelerationFactor();
        this.maxSpeed = config.getMaxSpeed();
        this.maxLifeTicks = config.getMaxLifeTicks();
        this.collisionRadius = config.getCollisionRadius();
        this.baseDamage = config.getBaseDamage();
        this.fragmentRank = config.getFragmentRank();
        
        this.ticksActive = 0;
        this.hasCollided = false;
        this.particleRenderer = config.getParticleRenderer();
        this.impactHandler = config.getImpactHandler();
        
        updateVelocity();
    }
    
    public void update() {
        ticksActive++;
        
        if (ticksActive >= maxLifeTicks || hasCollided) {
            return;
        }
        
        updateVelocity();
        
        if (shellType == ShellType.FALLING_BLOCK && fallingBlock != null && fallingBlock.isValid()) {
            // Apply velocity to the falling block for smooth movement
            fallingBlock.setVelocity(velocity);
            
            // Update controller position to match falling block
            controllerEntity.teleport(fallingBlock.getLocation());
            
            // Spawn subtle particle trail for visibility (only every 3 ticks to reduce spam)
            if (ticksActive % 3 == 0) {
                Location blockLoc = fallingBlock.getLocation();
                if (blockLoc.getWorld() != null) {
                    // Different particles based on block type
                    org.bukkit.Particle trailParticle = getTrailParticle(blockType);
                    blockLoc.getWorld().spawnParticle(trailParticle, blockLoc, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
        } else if (shellType == ShellType.PARTICLE_SHELL && particleRenderer != null) {
            // For particle shell, teleport the controller
            Location newLoc = controllerEntity.getLocation().add(velocity);
            controllerEntity.teleport(newLoc);
            // Render particle shell
            particleRenderer.renderShell(newLoc, blockType);
        }
        
        checkCollision();
    }
    
    public void updateVelocity() {
        double speed = Math.min(baseSpeed + (ticksActive * accelerationFactor), maxSpeed);
        velocity = direction.clone().multiply(speed);
    }
    
    public boolean checkCollision() {
        if (hasCollided) return true;
        
        // Use falling block location if available, otherwise controller location
        Location loc = (fallingBlock != null && fallingBlock.isValid()) 
            ? fallingBlock.getLocation() 
            : controllerEntity.getLocation();
        
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, collisionRadius, collisionRadius, collisionRadius)) {
            if (entity.getUniqueId().equals(ownerUUID)) continue;
            if (entity.equals(controllerEntity)) continue;
            if (fallingBlock != null && entity.equals(fallingBlock)) continue;
            
            if (entity instanceof LivingEntity) {
                onImpact(entity);
                return true;
            }
        }
        
        if (loc.getBlock().getType().isSolid()) {
            onImpactBlock();
            return true;
        }
        
        return false;
    }
    
    public void onImpact(Entity target) {
        if (hasCollided) return;
        hasCollided = true;
        
        // Use falling block location if available
        Location impactLoc = (fallingBlock != null && fallingBlock.isValid()) 
            ? fallingBlock.getLocation() 
            : controllerEntity.getLocation();
        
        // Use impact handler if available
        if (impactHandler != null) {
            impactHandler.onEntityImpact(this, target, impactLoc);
        } else {
            // Fallback to basic damage calculation
            double damage = baseDamage * (1 + 0.15 * fragmentRank);
            
            if (target instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) target;
                living.damage(damage);
                
                // Apply knockback
                Vector knockback = target.getLocation().toVector()
                    .subtract(impactLoc.toVector())
                    .normalize()
                    .multiply(0.5);
                knockback.setY(0.3);
                target.setVelocity(knockback);
            }
            
            // Spawn VFX
            if (particleRenderer != null) {
                particleRenderer.renderImpact(impactLoc, blockType);
            }
        }
    }
    
    public void onImpactBlock() {
        if (hasCollided) return;
        hasCollided = true;
        
        // Use falling block location if available
        Location impactLoc = (fallingBlock != null && fallingBlock.isValid()) 
            ? fallingBlock.getLocation() 
            : controllerEntity.getLocation();
        
        // Use impact handler if available
        if (impactHandler != null) {
            impactHandler.onBlockImpact(this, impactLoc);
        } else {
            // Fallback to basic VFX
            if (particleRenderer != null) {
                particleRenderer.renderImpact(impactLoc, blockType);
            }
        }
    }
    
    public void remove() {
        if (controllerEntity != null && controllerEntity.isValid()) {
            controllerEntity.remove();
        }
        
        if (fallingBlock != null && fallingBlock.isValid()) {
            fallingBlock.remove();
        }
    }
    
    // Getters
    
    public UUID getControllerId() {
        return controllerId;
    }
    
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    public String getAbilityId() {
        return abilityId;
    }
    
    public ArmorStand getControllerEntity() {
        return controllerEntity;
    }
    
    public ShellType getShellType() {
        return shellType;
    }
    
    public Material getBlockType() {
        return blockType;
    }
    
    public int getTicksActive() {
        return ticksActive;
    }
    
    public int getMaxLifeTicks() {
        return maxLifeTicks;
    }
    
    public boolean hasCollided() {
        return hasCollided;
    }
    
    public boolean isExpired() {
        return ticksActive >= maxLifeTicks || hasCollided;
    }
    
    public void setFallingBlock(FallingBlock fallingBlock) {
        this.fallingBlock = fallingBlock;
    }
    
    public FallingBlock getFallingBlock() {
        return fallingBlock;
    }
    
    public double getBaseDamage() {
        return baseDamage;
    }
    
    public int getFragmentRank() {
        return fragmentRank;
    }
    
    public ImpactHandler getImpactHandler() {
        return impactHandler;
    }
    
    public void setImpactHandler(ImpactHandler impactHandler) {
        this.impactHandler = impactHandler;
    }
    
    /**
     * Get appropriate trail particle for block type
     */
    private org.bukkit.Particle getTrailParticle(Material blockType) {
        return switch (blockType) {
            case MAGMA_BLOCK, NETHERRACK -> org.bukkit.Particle.FLAME;
            case OBSIDIAN, BLACKSTONE -> org.bukkit.Particle.SMOKE_LARGE;
            case ICE, PACKED_ICE -> org.bukkit.Particle.SNOWFLAKE;
            case STONE, COBBLESTONE -> org.bukkit.Particle.SMOKE_NORMAL; // Changed from BLOCK_CRACK
            default -> org.bukkit.Particle.CLOUD;
        };
    }
}
