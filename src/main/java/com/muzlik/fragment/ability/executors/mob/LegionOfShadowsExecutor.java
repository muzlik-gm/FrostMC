package com.muzlik.fragment.ability.executors.mob;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Legion of Shadows - Mob Fragment Ultimate (Slot 2)
 * Summons skeleton warriors and zombie brutes
 * FIXED: Proper ownership, balanced stats, won't attack player, auto-despawn
 */
public class LegionOfShadowsExecutor implements AbilityExecutor {
    
    private static final String SUMMON_OWNER_KEY = "summonOwner";
    private static final int SUMMON_DURATION = 60 * 20; // 60 seconds in ticks
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        World world = loc.getWorld();
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Scale summon count with rank (REDUCED for balance)
        int skeletonCount = 3 + (rank / 3); // 3 → 4 at rank 5 (reduced from 5-7)
        int zombieCount = 2; // Fixed at 2 (reduced from 2-3)
        
        // Spawn skeletons in a circle
        for (int i = 0; i < skeletonCount; i++) {
            double angle = (Math.PI * 2 * i) / skeletonCount;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            Location spawnLoc = loc.clone().add(x, 0, z);
            
            Skeleton skeleton = (Skeleton) world.spawnEntity(spawnLoc, EntityType.SKELETON);
            skeleton.setCustomName("§7" + player.getName() + "'s Shadow Warrior");
            skeleton.setCustomNameVisible(true);
            
            // BALANCED STATS - Reduced health and damage
            double health = 15.0 + (rank * 1.5); // 15 → 22.5 at rank 5 (vanilla is 20)
            skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            skeleton.setHealth(health);
            
            // Mark as summon with metadata
            skeleton.setMetadata(SUMMON_OWNER_KEY, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            
            // Equip skeletons better at higher ranks (but not too strong)
            if (rank >= 3) {
                skeleton.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.LEATHER_HELMET));
                skeleton.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(Material.LEATHER_CHESTPLATE));
            }
            
            // Auto-despawn after duration
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (skeleton.isValid() && !skeleton.isDead()) {
                        skeleton.getWorld().spawnParticle(Particle.SMOKE_LARGE, skeleton.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
                        skeleton.remove();
                    }
                }
            }.runTaskLater(plugin, SUMMON_DURATION);
            
            // VFX at spawn location
            world.spawnParticle(Particle.SMOKE_LARGE, spawnLoc, 20, 0.3, 0.5, 0.3, 0.05);
            world.spawnParticle(Particle.SOUL, spawnLoc, 10, 0.2, 0.3, 0.2, 0.02);
        }
        
        // Spawn zombies
        for (int i = 0; i < zombieCount; i++) {
            double angle = (Math.PI * 2 * i) / zombieCount + Math.PI / zombieCount;
            double x = Math.cos(angle) * 2.5;
            double z = Math.sin(angle) * 2.5;
            Location spawnLoc = loc.clone().add(x, 0, z);
            
            Zombie zombie = (Zombie) world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.setCustomName("§2" + player.getName() + "'s Shadow Brute");
            zombie.setCustomNameVisible(true);
            zombie.setAdult();
            
            // BALANCED STATS - Reduced health and damage
            double health = 25.0 + (rank * 2.0); // 25 → 35 at rank 5 (vanilla is 20)
            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            zombie.setHealth(health);
            
            double damage = 3.0 + (rank * 0.5); // 3 → 5.5 at rank 5 (vanilla is 2.5-3.5)
            zombie.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
            
            // Mark as summon with metadata
            zombie.setMetadata(SUMMON_OWNER_KEY, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            
            // Equip zombies better at higher ranks (but not too strong)
            if (rank >= 3) {
                zombie.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.LEATHER_HELMET));
                zombie.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(Material.LEATHER_CHESTPLATE));
                zombie.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.STONE_SWORD));
            }
            
            // Auto-despawn after duration
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (zombie.isValid() && !zombie.isDead()) {
                        zombie.getWorld().spawnParticle(Particle.SMOKE_LARGE, zombie.getLocation(), 25, 0.5, 0.5, 0.5, 0.06);
                        zombie.remove();
                    }
                }
            }.runTaskLater(plugin, SUMMON_DURATION);
            
            // VFX at spawn location
            world.spawnParticle(Particle.SMOKE_LARGE, spawnLoc, 25, 0.4, 0.6, 0.4, 0.06);
            world.spawnParticle(Particle.SOUL, spawnLoc, 15, 0.3, 0.4, 0.3, 0.03);
        }
        
        // Central VFX - MORE CHAOTIC AT HIGHER RANKS
        int particleCount = 150 + (rank * 30); // 150 → 300 at rank 5
        world.spawnParticle(Particle.SMOKE_LARGE, loc, particleCount, 2 + (rank * 0.3), 2, 2 + (rank * 0.3), 0.2);
        world.spawnParticle(Particle.SOUL, loc, 50 + (rank * 10), 1.5, 1, 1.5, 0.1);
        world.spawnParticle(Particle.SQUID_INK, loc, 30 + (rank * 8), 1, 0.5, 1, 0.05);
        
        // Sound
        world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f + (rank * 0.1f), 0.8f);
        world.playSound(loc, Sound.ENTITY_ZOMBIE_AMBIENT, 1.5f, 0.6f);
        
        player.sendMessage("§2💀 Legion of Shadows! §7(" + skeletonCount + " warriors, " + zombieCount + " brutes, " + (SUMMON_DURATION/20) + "s)");
    }
}
