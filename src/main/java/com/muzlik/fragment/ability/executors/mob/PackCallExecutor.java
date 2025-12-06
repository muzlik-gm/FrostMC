package com.muzlik.fragment.ability.executors.mob;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Pack Call - Mob Fragment Slot 3 Ability
 * 
 * Buffs all nearby summons owned by the player.
 * +50% damage, +30% speed, Regeneration II for 15 seconds.
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5
 */
public class PackCallExecutor implements AbilityExecutor {
    
    private final FrostSMPPlugin plugin;
    private static final double RADIUS = 12.0;
    private static final int DURATION_TICKS = 300; // 15 seconds
    
    public PackCallExecutor(FrostSMPPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        Location center = player.getLocation();
        int buffedCount = 0;
        
        // Find all summons within radius
        for (Entity entity : center.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            
            LivingEntity living = (LivingEntity) entity;
            
            // Check if this is a summon owned by the player
            if (isSummonOwnedBy(living, player)) {
                buffSummon(living, rank);
                buffedCount++;
                
                // Spawn green particle motes around summon
                living.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, 
                    living.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        if (buffedCount == 0) {
            player.sendMessage("§2✗ No summons nearby!");
            return;
        }
        
        // Spawn VFX at player location
        VFXLayerBuilder vfx = new VFXLayerBuilder(plugin, center, rank, player);
        
        vfx.core(Particle.VILLAGER_HAPPY, 40, ParticlePattern.RING, 
                RADIUS, 1.0, RADIUS, 0.1, null);
        vfx.secondary(Particle.COMPOSTER, 30, ParticlePattern.SPHERE,
                     RADIUS * 0.8, RADIUS * 0.8, RADIUS * 0.8, 0.05, null);
        vfx.ambient(Particle.SLIME, 20, ParticlePattern.BURST,
                   2.0, 2.0, 2.0, 0.02, null);
        
        if (rank >= 6) {
            vfx.cinematic(0.2, com.muzlik.vfx.CinematicEffect.PACK_AURA);
        }
        
        vfx.spawn();
        
        // Play sound
        player.getWorld().playSound(center, Sound.ENTITY_WOLF_HOWL, 2.0f, 1.0f);
        player.getWorld().playSound(center, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 1.2f);
        
        player.sendMessage("§2⚔ Pack Call! §7[" + buffedCount + " summons buffed]");
    }
    
    /**
     * Check if entity is a summon owned by the player
     */
    private boolean isSummonOwnedBy(LivingEntity entity, Player player) {
        // Check for summon metadata
        if (!entity.hasMetadata("fragmentOwner")) {
            return false;
        }
        
        try {
            String ownerUUID = entity.getMetadata("fragmentOwner").get(0).asString();
            return ownerUUID.equals(player.getUniqueId().toString());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Apply buffs to a summon
     */
    private void buffSummon(LivingEntity summon, int rank) {
        // +50% damage buff
        AttributeInstance damageAttr = summon.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            UUID modifierId = UUID.randomUUID();
            AttributeModifier damageMod = new AttributeModifier(
                modifierId,
                "pack_call_damage",
                0.5, // 50% increase
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            damageAttr.addModifier(damageMod);
            
            // Remove after duration
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (summon.isValid()) {
                    damageAttr.removeModifier(damageMod);
                }
            }, DURATION_TICKS);
        }
        
        // +30% speed buff
        AttributeInstance speedAttr = summon.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            UUID modifierId = UUID.randomUUID();
            AttributeModifier speedMod = new AttributeModifier(
                modifierId,
                "pack_call_speed",
                0.3, // 30% increase
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            speedAttr.addModifier(speedMod);
            
            // Remove after duration
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (summon.isValid()) {
                    speedAttr.removeModifier(speedMod);
                }
            }, DURATION_TICKS);
        }
        
        // Regeneration II for 15 seconds
        summon.addPotionEffect(new PotionEffect(
            PotionEffectType.REGENERATION,
            DURATION_TICKS,
            1, // Level II (amplifier 1)
            false,
            true,
            true
        ));
    }
}
