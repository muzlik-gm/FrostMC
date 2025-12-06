package com.muzlik.fragment.ability.executors.mob;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Iron Golem Guardian - Mob Fragment Secondary (Slot 1)
 * Summons Iron Golem that protects the caster
 * FIXED: Proper ownership, won't attack player, balanced stats, auto-despawn
 */
public class IronGolemGuardianExecutor implements AbilityExecutor {
    
    private static final String SUMMON_OWNER_KEY = "summonOwner";
    private static final int SUMMON_DURATION = 45 * 20; // 45 seconds in ticks
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        Location loc = player.getLocation();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Spawn Iron Golem
        IronGolem golem = (IronGolem) loc.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
        golem.setPlayerCreated(true); // Makes it friendly to players
        
        // Scale health with rank (reduced from vanilla 100 HP)
        double baseHealth = 60.0; // Reduced from 100
        double health = baseHealth + (rank * 5); // 60 → 85 at rank 5
        golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        golem.setHealth(health);
        
        // Reduce damage (vanilla is 7-21 damage)
        double baseDamage = 5.0; // Reduced from 7-21
        double damage = baseDamage + (rank * 1.0); // 5 → 10 at rank 5
        golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
        
        // Mark as summon with metadata
        golem.setMetadata(SUMMON_OWNER_KEY, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        
        // Custom name
        golem.setCustomName("§7" + player.getName() + "'s Guardian");
        golem.setCustomNameVisible(true);
        
        // Auto-despawn after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                if (golem.isValid() && !golem.isDead()) {
                    golem.getWorld().spawnParticle(Particle.BLOCK_CRACK, golem.getLocation(), 100, 1, 1, 1, Material.IRON_BLOCK.createBlockData());
                    golem.getWorld().spawnParticle(Particle.SMOKE_LARGE, golem.getLocation(), 30, 0.5, 1, 0.5, 0.1);
                    golem.remove();
                }
            }
        }.runTaskLater(plugin, SUMMON_DURATION);
        
        // Spawn VFX
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 100, 1, 1, 1, Material.IRON_BLOCK.createBlockData());
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 50, 1, 1, 1, 0.1);
        player.getWorld().spawnParticle(Particle.CRIT, loc, 30, 1, 1, 1, 0.2);
        
        // Sound
        player.getWorld().playSound(loc, Sound.BLOCK_IRON_DOOR_CLOSE, 2.0f, 0.5f);
        player.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 1.5f, 1.0f);
        player.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.8f);
        
    }
}
