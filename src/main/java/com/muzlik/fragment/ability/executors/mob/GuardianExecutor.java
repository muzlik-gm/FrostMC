package com.muzlik.fragment.ability.executors.mob;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Guardian - Mob Fragment Secondary Ability
 * Summons Iron Golem that protects caster for 45s, taunts enemies
 * 
 * VFX: 5-Layer System
 * - Core: REDSTONE summoning circle
 * - Secondary: SPELL_MOB particles
 * - Ambient: SMOKE_NORMAL wisps
 * - Impact: Summoning burst
 */
public class GuardianExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location playerLoc = player.getLocation();
        int rank = context.getRank();
        
        // Find spawn location in front of player
        Location spawnLoc = playerLoc.clone().add(playerLoc.getDirection().multiply(3));
        spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
        
        int duration = 900 + (rank * 120); // 45s + 6s per rank (in ticks)
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Summoning VFX
        createSummonVFX(plugin, spawnLoc, rank, player);
        
        // Spawn Iron Golem
        IronGolem golem = spawnLoc.getWorld().spawn(spawnLoc, IronGolem.class);
        golem.setCustomName("§6" + player.getName() + "'s Guardian");
        golem.setCustomNameVisible(true);
        
        // Scale golem health with rank
        double maxHealth = 100.0 + (rank * 20.0); // 50 hearts + 10 hearts per rank
        golem.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        golem.setHealth(maxHealth);
        
        // Make golem follow and protect player
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !golem.isValid() || !player.isOnline()) {
                    if (golem.isValid()) {
                        // Despawn VFX
                        createDespawnVFX(plugin, golem.getLocation(), rank, player);
                        golem.remove();
                    }
                    cancel();
                    return;
                }
                
                // Make golem follow player if too far
                if (golem.getLocation().distance(player.getLocation()) > 10.0) {
                    Location followLoc = player.getLocation().clone().add(player.getLocation().getDirection().multiply(-2));
                    followLoc.setY(followLoc.getWorld().getHighestBlockYAt(followLoc) + 1);
                    golem.teleport(followLoc);
                }
                
                // Guardian aura VFX every 20 ticks
                if (ticks % 20 == 0) {
                    createGuardianAura(plugin, golem.getLocation(), rank, player);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Sound effect
        float pitch = 0.8f + (rank * 0.1f);
        spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_IRON_GOLEM_HURT, 1.0f + (rank * 0.2f), pitch);
        spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_ANVIL_PLACE, 0.8f, pitch);
    }
    
    private void createSummonVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int coreCount = 20 + (rank * 4);
        int secondaryCount = 15 + (rank * 3);
        int ambientCount = 10 + (rank * 2);
        
        double spread = 1.5 + (rank * 0.2);
        
        VFXLayerBuilder summonVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.REDSTONE, coreCount, ParticlePattern.SPIRAL, spread, spread * 1.5, spread, 0.1, null)
            .secondary(Particle.SPELL_MOB, secondaryCount, ParticlePattern.POINT, spread * 0.8, spread, spread * 0.8, 0.08, null)
            .ambient(Particle.SMOKE_NORMAL, ambientCount, ParticlePattern.POINT, spread * 0.6, spread * 0.8, spread * 0.6, 0.05, null);
        
        summonVfx.spawn();
    }
    
    private void createGuardianAura(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int auraCount = 5 + rank;
        
        VFXLayerBuilder auraVfx = new VFXLayerBuilder(plugin, loc.clone().add(0, 1, 0), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .ambient(Particle.REDSTONE, auraCount, ParticlePattern.SPHERE, 1.0, 1.0, 1.0, 0.02, null);
        
        auraVfx.spawn();
    }
    
    private void createDespawnVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int impactCount = 15 + (rank * 3);
        
        VFXLayerBuilder despawnVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.SMOKE_LARGE, impactCount, ParticlePattern.BURST, 1.2, 1.2, 1.2, 0.08, null);
        
        despawnVfx.spawn();
        
        // Despawn sound
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_DEATH, 0.8f, 1.0f);
    }
}