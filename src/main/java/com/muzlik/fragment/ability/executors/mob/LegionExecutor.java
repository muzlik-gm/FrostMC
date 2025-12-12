package com.muzlik.fragment.ability.executors.mob;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Legion - Mob Fragment Ultimate Ability
 * Summons 5 skeleton warriors + 2 zombie brutes that fight for 60s
 * 
 * VFX: 5-Layer System
 * - Core: SOUL summoning circles
 * - Secondary: SPELL_WITCH particles
 * - Ambient: SMOKE_LARGE wisps
 * - Impact: Mass summoning burst
 * - Cinematic: Pack aura at rank 6+
 */
public class LegionExecutor implements AbilityExecutor {

    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location playerLoc = player.getLocation();
        int rank = context.getRank();
        
        // Scale army size with rank
        int skeletonCount = 5 + (rank / 2); // 5-8 skeletons
        int zombieCount = 2 + (rank / 3);   // 2-4 zombies
        int duration = 1200 + (rank * 120); // 60s + 6s per rank (in ticks)
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Summon skeletons in outer circle
        for (int i = 0; i < skeletonCount; i++) {
            double angle = (i * Math.PI * 2) / skeletonCount;
            double x = Math.cos(angle) * 5.0;
            double z = Math.sin(angle) * 5.0;
            Location summonLoc = playerLoc.clone().add(x, 0, z);
            summonLoc.setY(summonLoc.getWorld().getHighestBlockYAt(summonLoc) + 1);
            
            summonSkeleton(plugin, summonLoc, rank, player, duration);
        }
        
        // Summon zombies in inner circle
        for (int i = 0; i < zombieCount; i++) {
            double angle = (i * Math.PI * 2) / zombieCount;
            double x = Math.cos(angle) * 3.0;
            double z = Math.sin(angle) * 3.0;
            Location summonLoc = playerLoc.clone().add(x, 0, z);
            summonLoc.setY(summonLoc.getWorld().getHighestBlockYAt(summonLoc) + 1);
            
            summonZombie(plugin, summonLoc, rank, player, duration);
        }
        
        // Mass summoning VFX
        createLegionVFX(plugin, playerLoc.clone().add(0, 1, 0), rank, player);
        
        // Sound effects
        float pitch = 0.8f + (rank * 0.1f);
        playerLoc.getWorld().playSound(playerLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f + (rank * 0.2f), pitch);
        playerLoc.getWorld().playSound(playerLoc, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 0.8f, pitch);
    }
    
    private void summonSkeleton(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player owner, int duration) {
        // Summoning VFX
        createSummonVFX(plugin, loc, rank, owner);
        
        // Spawn skeleton
        Skeleton skeleton = loc.getWorld().spawn(loc, Skeleton.class);
        skeleton.setCustomName("§6" + owner.getName() + "'s Warrior");
        skeleton.setCustomNameVisible(true);
        
        // Equip skeleton with bow and armor
        skeleton.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        skeleton.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        skeleton.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        
        // Scale skeleton health with rank
        double maxHealth = 20.0 + (rank * 3.0); // 10 hearts + 1.5 hearts per rank
        skeleton.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        skeleton.setHealth(maxHealth);
        
        // Remove after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skeleton.isValid()) {
                    createDespawnVFX(plugin, skeleton.getLocation(), rank, owner);
                    skeleton.remove();
                }
            }
        }.runTaskLater(plugin, duration);
    }
    
    private void summonZombie(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player owner, int duration) {
        // Summoning VFX
        createSummonVFX(plugin, loc, rank, owner);
        
        // Spawn zombie
        Zombie zombie = loc.getWorld().spawn(loc, Zombie.class);
        zombie.setCustomName("§6" + owner.getName() + "'s Brute");
        zombie.setCustomNameVisible(true);
        zombie.setBaby(false);
        
        // Equip zombie with sword and armor
        zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        
        // Scale zombie health with rank (stronger than skeletons)
        double maxHealth = 30.0 + (rank * 5.0); // 15 hearts + 2.5 hearts per rank
        zombie.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        zombie.setHealth(maxHealth);
        
        // Remove after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isValid()) {
                    createDespawnVFX(plugin, zombie.getLocation(), rank, owner);
                    zombie.remove();
                }
            }
        }.runTaskLater(plugin, duration);
    }
    
    private void createLegionVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int coreCount = 40 + (rank * 8);
        int secondaryCount = 30 + (rank * 6);
        int ambientCount = 25 + (rank * 5);
        int impactCount = 35 + (rank * 7);
        
        double spread = 3.0 + (rank * 0.5);
        
        VFXLayerBuilder legionVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.SOUL, coreCount, ParticlePattern.SPIRAL, spread, spread * 2, spread, 0.15, null)
            .secondary(Particle.SPELL_WITCH, secondaryCount, ParticlePattern.SPHERE, spread * 0.8, spread * 1.5, spread * 0.8, 0.12, null)
            .ambient(Particle.SMOKE_LARGE, ambientCount, ParticlePattern.POINT, spread * 1.2, spread, spread * 1.2, 0.08, null)
            .impact(Particle.EXPLOSION_LARGE, impactCount, ParticlePattern.BURST, spread * 1.5, spread * 2.5, spread * 1.5, 0.2, null);
        
        // Cinematic layer: Pack aura at rank 6+
        if (rank >= 6) {
            legionVfx.cinematic(0.4 + (rank * 0.1), CinematicEffect.PACK_AURA);
        }
        
        legionVfx.spawn();
    }
    
    private void createSummonVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int coreCount = 12 + (rank * 2);
        
        VFXLayerBuilder summonVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .core(Particle.SOUL, coreCount, ParticlePattern.SPIRAL, 0.8, 1.2, 0.8, 0.06, null);
        
        summonVfx.spawn();
    }
    
    private void createDespawnVFX(com.muzlik.FrostSMPPlugin plugin, Location loc, int rank, Player player) {
        int impactCount = 8 + rank;
        
        VFXLayerBuilder despawnVfx = new VFXLayerBuilder(plugin, loc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            .impact(Particle.SOUL, impactCount, ParticlePattern.BURST, 0.6, 0.6, 0.6, 0.04, null);
        
        despawnVfx.spawn();
    }
}