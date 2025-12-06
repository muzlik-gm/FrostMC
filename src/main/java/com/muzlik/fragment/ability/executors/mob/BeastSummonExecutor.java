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
 * Beast Summon - Mob Fragment Primary (Slot 0)
 * Summons wolf pack with proper limits and tracking
 * FIXED: Added mob limits, proper ownership, auto-despawn
 */
public class BeastSummonExecutor implements AbilityExecutor {
    
    private static final String SUMMON_OWNER_KEY = "summonOwner";
    private static final String SUMMON_TIME_KEY = "summonTime";
    private static final int MAX_SUMMONS_PER_PLAYER = 10; // Global limit
    private static final int SUMMON_DURATION = 30 * 20; // 30 seconds in ticks
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        Location loc = player.getLocation();
        
        // Count existing summons for this player
        int existingSummons = 0;
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.hasMetadata(SUMMON_OWNER_KEY)) {
                UUID ownerId = UUID.fromString(entity.getMetadata(SUMMON_OWNER_KEY).get(0).asString());
                if (ownerId.equals(player.getUniqueId())) {
                    existingSummons++;
                }
            }
        }
        
        // Check limit
        if (existingSummons >= MAX_SUMMONS_PER_PLAYER) {
            player.sendMessage("§c✗ Summon limit reached! §7(" + existingSummons + "/" + MAX_SUMMONS_PER_PLAYER + ")");
            return;
        }
        
        // Calculate how many wolves to summon
        int wolvesToSummon = Math.min(3, MAX_SUMMONS_PER_PLAYER - existingSummons);
        
        if (wolvesToSummon == 0) {
            player.sendMessage("§c✗ Cannot summon more creatures!");
            return;
        }
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Summon wolves
        for (int i = 0; i < wolvesToSummon; i++) {
            // Spawn location with offset
            double angle = (Math.PI * 2 * i) / wolvesToSummon;
            double offsetX = Math.cos(angle) * 2;
            double offsetZ = Math.sin(angle) * 2;
            Location spawnLoc = loc.clone().add(offsetX, 0, offsetZ);
            
            Wolf wolf = (Wolf) loc.getWorld().spawnEntity(spawnLoc, EntityType.WOLF);
            wolf.setOwner(player);
            wolf.setAngry(true);
            wolf.setAdult();
            
            // Scale health with rank
            double baseHealth = 20.0;
            double health = baseHealth + (rank * 2);
            wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            wolf.setHealth(health);
            
            // Mark as summon with metadata
            wolf.setMetadata(SUMMON_OWNER_KEY, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            wolf.setMetadata(SUMMON_TIME_KEY, new FixedMetadataValue(plugin, System.currentTimeMillis()));
            
            // Custom name
            wolf.setCustomName("§2" + player.getName() + "'s Wolf");
            wolf.setCustomNameVisible(true);
            
            // Auto-despawn after duration
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (wolf.isValid() && !wolf.isDead()) {
                        wolf.getWorld().spawnParticle(Particle.SMOKE_LARGE, wolf.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
                        wolf.remove();
                    }
                }
            }.runTaskLater(plugin, SUMMON_DURATION);
            
            // Spawn VFX
            wolf.getWorld().spawnParticle(Particle.SMOKE_LARGE, spawnLoc, 30, 0.5, 0.5, 0.5, 0.1);
            wolf.getWorld().spawnParticle(Particle.CLOUD, spawnLoc, 20, 0.5, 0.5, 0.5, 0.05);
        }
        
        // Sound - FIXED: ENTITY_WOLF_HOWL doesn't exist in 1.21
        player.getWorld().playSound(loc, Sound.ENTITY_WOLF_AMBIENT, 1.5f, 0.8f);
        player.getWorld().playSound(loc, Sound.ENTITY_WOLF_GROWL, 1.2f, 1.0f);
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.2f);
        
    }
}
