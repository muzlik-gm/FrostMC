package com.muzlik.fragment.ability.executors.mob;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Summon - Mob Fragment Primary Ability
 * Summons wolf pack (3 wolves) that fight for 30s, scales with rank
 * 
 * VFX: 5-Layer System
 * - Core: CLOUD summoning circles
 * - Secondary: SPELL_MOB particles
 * - Ambient: SMOKE_NORMAL wisps
 * - Impact: Summoning burst
 * - Cinematic: Pack aura at rank 6+
 */
public class SummonExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location playerLoc = player.getLocation();
        int rank = context.getRank();
        
        // Number of wolves scales with rank
        int wolfCount = 3 + (rank / 2); // 3-6 wolves depending on rank
        int duration = 600 + (rank * 60); // 30s + 3s per rank (in ticks)
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Summon wolves in circle around player
        for (int i = 0; i < wolfCount; i++) {
            double angle = (i * Math.PI * 2) / wolfCount;
            double x = Math.cos(angle) * 3.0;
            double z = Math.sin(angle) * 3.0;
            Location summonLoc = playerLoc.clone().add(x, 0, z);
            
            // Find safe spawn location
            summonLoc.setY(summonLoc.getWorld().getHighestBlockYAt(summonLoc) + 1);
            
            // Summoning VFX
            createSummonVFX(plugin, summonLoc, rank, player);
            
            // Spawn wolf
            Wolf wolf = summonLoc.getWorld().spawn(summonLoc, Wolf.class);
            wolf.setOwner(player);
            wolf.setTamed(true);
            wolf.setAngry(true);
            wolf.setCustomName("§6" + player.getName() + "'s Wolf");
            wolf.setCustomNameVisible(true);
            
            // Scale wolf health with rank
            double maxHealth = 20.0 + (rank * 5.0); // 10 hearts + 2.5 hearts per rank
            wolf.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            wolf.setHealth(maxHealth);
            
            // Remove wolf after duration
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (wolf.isValid()) {
                        // Despawn VFX
                        createDespawnVFX(plugin, wolf.getLocation(), rank, player);
                        wolf.remove();
                    }
                }
            }.runTaskLater(plugin, duration);
        }
        
        // Sound effect
        float pitch = 1.0f + (rank * 0.1f);
        playerLoc.getWorld().playSound(playerLoc, Sound.ENTITY_WOLF_HOWL, 1.0f + (rank * 0.2f), pitch);
        
        // Cinematic effect at rank 6+
        if (rank >= 6) {
            VFXLayerBuilder cinematicVfx = new VFXLayerBuilder(plugin, playerLoc.clone().add(0, 1, 0), rank, player)
                .withPerformanceManager(plugin.getVFXPerformanceManager())
                .cinematic(0.3 + (rank * 0.05), CinematicEffect.PACK_AURA);
            cinematicVfx.spawn();
        }
    }
    
    private void createSummonVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int coreCount = 15 + (rank * 3);
        int secondaryCount = 10 + (rank * 2);
        int ambientCount = 8 + rank;
        
        double spread = 1.0 + (rank * 0.1);
        
        VFXLayerBuilder summonVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.CLOUD, coreCount, ParticlePattern.SPIRAL, spread, spread * 1.5, spread, 0.08, null)
            .secondary(Particle.SPELL_MOB, secondaryCount, ParticlePattern.POINT, spread * 0.6, spread, spread * 0.6, 0.05, null)
            .ambient(Particle.SMOKE_NORMAL, ambientCount, ParticlePattern.POINT, spread * 0.4, spread * 0.8, spread * 0.4, 0.03, null);
        
        summonVfx.spawn();
    }
    
    private void createDespawnVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int impactCount = 12 + (rank * 2);
        
        VFXLayerBuilder despawnVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.CLOUD, impactCount, ParticlePattern.BURST, 0.8, 0.8, 0.8, 0.06, null);
        
        despawnVfx.spawn();
        
        // Despawn sound
        loc.getWorld().playSound(loc, Sound.ENTITY_WOLF_WHINE, 0.5f, 1.2f);
    }
}