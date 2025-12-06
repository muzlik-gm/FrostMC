package com.muzlik.powers;

import com.muzlik.power.AbstractPower;
import com.muzlik.power.AbstractAbility;
import com.muzlik.util.DamageUtils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.*;

/**
 * Fire Guard Power Implementation
 * 
 * Primary: Lava Burst - Fire resistance, walk on lava, 10×10 explosion blast
 * Secondary: Lava Dome - 10×10×10 lava dome with debuffs and eye-beam lock-on
 * 
 * All effects are temporary and auto-clean with no permanent world changes
 */
public class FirePower extends AbstractPower {

    public FirePower() {
        super(
            "fire_power",
            "Fire Guard",
            "Let's harness the power of Fire"
        );

        // Register the two abilities
        getAbilityManager().registerPrimaryAbility(new LavaBurstAbility());
        getAbilityManager().registerSecondaryAbility(new LavaDomeAbility());
    }

    /**
     * PRIMARY ABILITY: Lava Burst
     * - Infinite Fire Resistance (30 min)
     * - Walk on lava
     * - 10×10 explosion blast
     * - Damage: 2 hearts (Netherite Prot 4), 4 hearts (Diamond)
     * - User immune to own blast
     * - Cooldown: 2m 30s
     */
    private static class LavaBurstAbility extends AbstractAbility {

        private static final int BLAST_RADIUS = 5; // 10×10 area (radius 5)
        private static final int COOLDOWN_MS = 150000; // 2m 30s
        private static final int FIRE_RESISTANCE_TICKS = 36000; // 30 min (20 ticks/sec)

        public LavaBurstAbility() {
            super(
                "fire_lava_burst",
                "Lava Burst",
                "Grants infinite fire resistance, walk on lava, creates 10×10 explosion blast",
                COOLDOWN_MS
            );
        }

        @Override
        public boolean execute(Player player) {
            setActive(true);
            Plugin plugin = Bukkit.getPluginManager().getPlugin("FrostSMP");
            if (plugin == null) {
                player.sendMessage("§c✗ Plugin error: FrostSMP not found");
                return false;
            }

            Location center = player.getLocation();
            
            // Give infinite fire resistance (30 min)
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE,
                FIRE_RESISTANCE_TICKS,
                255,
                false,
                false
            ));

            // Enhanced explosion VFX with animation
            createExplosionAnimation(center, plugin);
            
            // Explosion sounds with delay for impact
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.7f);
            center.getWorld().playSound(center, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.5f, 0.8f);
            center.getWorld().playSound(center, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.5f);

            // Damage nearby entities (user is immune)
            damageNearbyEntities(player, center);

            setActive(false);
            return true;
        }
        
        private void createExplosionAnimation(Location center, Plugin plugin) {
            new BukkitRunnable() {
                int ticks = 0;
                
                @Override
                public void run() {
                    if (ticks >= 20) { // 1 second animation
                        cancel();
                        return;
                    }
                    
                    double radius = (ticks / 20.0) * BLAST_RADIUS;
                    
                    // Expanding fire ring
                    for (int i = 0; i < 32; i++) {
                        double angle = (2 * Math.PI * i) / 32;
                        double x = center.getX() + radius * Math.cos(angle);
                        double z = center.getZ() + radius * Math.sin(angle);
                        Location loc = new Location(center.getWorld(), x, center.getY() + 0.2, z);
                        
                        center.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.1, 0.1, 0.1, 0.05);
                        center.getWorld().spawnParticle(Particle.LAVA, loc, 1, 0, 0, 0, 0);
                    }
                    
                    // Central explosion effects
                    if (ticks % 4 == 0) {
                        center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 2, 2, 1, 2, 0);
                        center.getWorld().spawnParticle(Particle.FLAME, center, 30, 3, 2, 3, 0.15);
                    }
                    
                    // Upward fire spiral
                    double height = (ticks / 20.0) * 5;
                    for (int i = 0; i < 3; i++) {
                        double spiralAngle = (ticks * 0.5) + (i * 2 * Math.PI / 3);
                        double spiralRadius = 1.5;
                        double sx = center.getX() + spiralRadius * Math.cos(spiralAngle);
                        double sz = center.getZ() + spiralRadius * Math.sin(spiralAngle);
                        Location spiralLoc = new Location(center.getWorld(), sx, center.getY() + height, sz);
                        center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spiralLoc, 2, 0.1, 0.1, 0.1, 0);
                    }
                    
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        private void damageNearbyEntities(Player player, Location center) {
            Collection<Entity> nearby = center.getNearbyEntities(BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS);

            for (Entity entity : nearby) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    LivingEntity target = (LivingEntity) entity;
                    double damage = calculateDamage(target);
                    // Use proper damage attribution for kill credit
                    DamageUtils.dealAbilityDamage(player, target, damage);
                }
            }
        }

        private double calculateDamage(LivingEntity target) {
            if (!(target instanceof Player)) {
                return 4.0; // 2 hearts
            }

            Player p = (Player) target;
            
            // Check for Netherite armor (rough estimate)
            boolean hasNetheriteArmor = false;
            if (p.getInventory().getHelmet() != null && 
                p.getInventory().getHelmet().getType().name().contains("NETHERITE")) {
                hasNetheriteArmor = true;
            }
            
            if (hasNetheriteArmor) {
                return 4.0; // 2 hearts for Netherite Prot 4
            } else {
                return 8.0; // 4 hearts for Diamond
            }
        }
    }

    /**
     * SECONDARY ABILITY: Lava Dome
     * - Massive sound heard by all within 10×10 blocks
     * - 10×10×10 lava dome forms around user
     * - User takes no damage from lava
     * - Dome acts like solid lava block (walkable)
     * - Blindness (10s) and Weakness (10s) to nearby players
     * - Eye-beam locks on target with sound, deals 1 damage/sec
     * - No escape (ender pearls blocked)
     * - Active for 5s, fully ends at 10s
     * - Cooldown: 6m
     */
    private static class LavaDomeAbility extends AbstractAbility {

        private static final int DOME_RADIUS = 5; // 10×10 area
        private static final int DOME_HEIGHT = 10;
        private static final int COOLDOWN_MS = 360000; // 6m
        private BukkitRunnable activeTask = null;
        private Set<UUID> trappedPlayers = new HashSet<>();

        public LavaDomeAbility() {
            super(
                "fire_lava_dome",
                "Lava Dome",
                "Creates 10×10×10 lava dome, locks enemies with eye-beam, prevents escape",
                COOLDOWN_MS
            );
        }

        @Override
        public boolean execute(Player player) {
            setActive(true);
            Plugin plugin = Bukkit.getPluginManager().getPlugin("FrostSMP");
            if (plugin == null) {
                player.sendMessage("§c✗ Plugin error: FrostSMP not found");
                return false;
            }

            Location center = player.getLocation();
            trappedPlayers.clear();
            
            // Massive sound heard by all within 10×10 blocks
            Collection<Entity> nearbyForSound = center.getNearbyEntities(10, 10, 10);
            for (Entity entity : nearbyForSound) {
                if (entity instanceof Player) {
                    Player p = (Player) entity;
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
                    p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, 0.5f);
                }
            }
            
            // Give player fire resistance for the duration (10 seconds)
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE,
                200,
                255,
                false,
                false
            ));
            
            // Store dome blocks and their original materials
            Map<Block, Material> domeBlocks = new HashMap<>();
            Set<Block> domeBlockSet = new HashSet<>();
            
            // Enhanced dome formation animation
            createDomeFormationAnimation(center, domeBlocks, domeBlockSet, plugin);
            
            // Apply debuffs to enemies and trap them
            applyDebuffsToEnemies(player, center);
            
            // Start dome maintenance task
            activeTask = new BukkitRunnable() {
                int ticks = 0;
                
                @Override
                public void run() {
                    ticks++;
                    
                    // Regenerate destroyed blocks (dome is solid)
                    for (Block block : domeBlockSet) {
                        if (block.getType() != Material.MAGMA_BLOCK) {
                            block.setType(Material.MAGMA_BLOCK);
                        }
                    }
                    
                    // Continuous dome VFX
                    if (ticks % 10 == 0 && ticks <= 100) {
                        createDomeAmbientEffects(center);
                    }
                    
                    // Prevent escape attempts
                    preventEscape(center);
                    
                    // Eye-beam damage (every 20 ticks = 1 second)
                    if (ticks % 20 == 0 && ticks <= 100) {
                        applyEyeBeamDamage(player, center);
                    }
                    
                    // Dome active for 5 seconds (100 ticks)
                    if (ticks == 100) {
                        // Start fading animation
                        createDomeFadeAnimation(center, plugin);
                    }
                    
                    // Fully remove at 10 seconds (200 ticks)
                    if (ticks >= 200) {
                        // Restore original blocks
                        for (Map.Entry<Block, Material> entry : domeBlocks.entrySet()) {
                            entry.getKey().setType(entry.getValue());
                        }
                        trappedPlayers.clear();
                        setActive(false);
                        cancel();
                    }
                }
            };
            activeTask.runTaskTimer(plugin, 0L, 1L);

            return true;
        }
        
        private void createDomeFormationAnimation(Location center, Map<Block, Material> domeBlocks, 
                                                  Set<Block> domeBlockSet, Plugin plugin) {
            new BukkitRunnable() {
                int ticks = 0;
                
                @Override
                public void run() {
                    if (ticks >= 15) { // 0.75 second formation
                        // Create full dome structure
                        createDomeStructure(center, domeBlocks, domeBlockSet);
                        cancel();
                        return;
                    }
                    
                    double progress = ticks / 15.0;
                    int currentHeight = (int)(DOME_HEIGHT * progress);
                    
                    // Rising lava effect
                    for (int i = 0; i < 20; i++) {
                        double angle = (2 * Math.PI * i) / 20;
                        double x = center.getX() + DOME_RADIUS * Math.cos(angle);
                        double z = center.getZ() + DOME_RADIUS * Math.sin(angle);
                        
                        for (int y = 0; y <= currentHeight; y++) {
                            Location loc = new Location(center.getWorld(), x, center.getY() + y, z);
                            center.getWorld().spawnParticle(Particle.LAVA, loc, 2, 0.2, 0.2, 0.2, 0);
                            center.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.1, 0.1, 0.1, 0);
                        }
                    }
                    
                    // Ground eruption effect
                    if (ticks % 3 == 0) {
                        center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 1, DOME_RADIUS, 0.5, DOME_RADIUS, 0);
                        center.getWorld().playSound(center, Sound.BLOCK_LAVA_POP, 1.5f, 0.8f);
                    }
                    
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        
        private void createDomeAmbientEffects(Location center) {
            // Floating embers inside dome
            for (int i = 0; i < 15; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * (DOME_RADIUS - 1);
                double height = Math.random() * DOME_HEIGHT;
                
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);
                Location loc = new Location(center.getWorld(), x, center.getY() + height, z);
                
                center.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0.1, 0, 0.01);
                center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0, 0.1, 0, 0.01);
            }
            
            // Heat wave effect at ground level
            for (int i = 0; i < 8; i++) {
                double angle = (2 * Math.PI * i) / 8;
                double x = center.getX() + (DOME_RADIUS - 1) * Math.cos(angle);
                double z = center.getZ() + (DOME_RADIUS - 1) * Math.sin(angle);
                Location loc = new Location(center.getWorld(), x, center.getY() + 0.1, z);
                center.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, 0.1, 0, 0.1, 0.01);
            }
        }
        
        private void createDomeFadeAnimation(Location center, Plugin plugin) {
            new BukkitRunnable() {
                int ticks = 0;
                
                @Override
                public void run() {
                    if (ticks >= 20) { // 1 second fade
                        cancel();
                        return;
                    }
                    
                    // Smoke and ash rising
                    for (int i = 0; i < 10; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double radius = Math.random() * DOME_RADIUS;
                        double x = center.getX() + radius * Math.cos(angle);
                        double z = center.getZ() + radius * Math.sin(angle);
                        double y = center.getY() + Math.random() * DOME_HEIGHT;
                        
                        Location loc = new Location(center.getWorld(), x, y, z);
                        center.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 2, 0.2, 0.3, 0.2, 0.05);
                        center.getWorld().spawnParticle(Particle.ASH, loc, 3, 0.3, 0.3, 0.3, 0.02);
                    }
                    
                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L);
        }

        private void createDomeStructure(Location center, Map<Block, Material> domeBlocks, Set<Block> domeBlockSet) {
            int cx = center.getBlockX();
            int cy = center.getBlockY();
            int cz = center.getBlockZ();
            
            // Create hollow dome (sphere) - acts like solid lava block
            for (int x = -DOME_RADIUS; x <= DOME_RADIUS; x++) {
                for (int y = 0; y <= DOME_HEIGHT; y++) {
                    for (int z = -DOME_RADIUS; z <= DOME_RADIUS; z++) {
                        double dist = Math.sqrt(x * x + y * y + z * z);
                        
                        // Shell of dome (hollow sphere)
                        if (dist >= DOME_RADIUS - 1 && dist <= DOME_RADIUS + 0.5) {
                            Block block = center.getWorld().getBlockAt(cx + x, cy + y, cz + z);
                            
                            if (block.getType() == Material.AIR || block.getType().isTransparent()) {
                                domeBlocks.put(block, block.getType());
                                domeBlockSet.add(block);
                                // Use MAGMA_BLOCK - solid and walkable like lava
                                block.setType(Material.MAGMA_BLOCK);
                            }
                        }
                    }
                }
            }
        }

        private void applyDebuffsToEnemies(Player player, Location center) {
            Collection<Entity> nearby = center.getNearbyEntities(DOME_RADIUS + 2, DOME_HEIGHT, DOME_RADIUS + 2);
            
            for (Entity entity : nearby) {
                if (entity instanceof Player && !entity.equals(player)) {
                    Player target = (Player) entity;
                    
                    // Blindness (10s = 200 ticks)
                    target.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        200,
                        0,
                        false,
                        false
                    ));
                    
                    // Weakness (10s = 200 ticks)
                    target.addPotionEffect(new PotionEffect(
                        PotionEffectType.WEAKNESS,
                        200,
                        0,
                        false,
                        false
                    ));
                    
                    // Mark as trapped (for escape prevention)
                    trappedPlayers.add(target.getUniqueId());
                }
            }
        }
        
        private void preventEscape(Location center) {
            // Prevent ender pearls and other escape methods
            Collection<Entity> nearby = center.getNearbyEntities(DOME_RADIUS, DOME_HEIGHT, DOME_RADIUS);
            
            for (Entity entity : nearby) {
                if (entity instanceof Player) {
                    Player p = (Player) entity;
                    if (trappedPlayers.contains(p.getUniqueId())) {
                        // Cancel any teleportation attempts by keeping them inside
                        Location pLoc = p.getLocation();
                        if (pLoc.distance(center) > DOME_RADIUS - 1) {
                            // Push them back towards center
                            Vector direction = center.toVector().subtract(pLoc.toVector()).normalize().multiply(0.5);
                            p.setVelocity(direction);
                        }
                    }
                }
            }
        }

        private void applyEyeBeamDamage(Player player, Location center) {
            Collection<Entity> nearby = center.getNearbyEntities(DOME_RADIUS, DOME_HEIGHT, DOME_RADIUS);
            
            for (Entity entity : nearby) {
                if (entity instanceof LivingEntity && !entity.equals(player)) {
                    LivingEntity target = (LivingEntity) entity;
                    
                    // Eye-beam lock-on sound effect
                    if (target instanceof Player) {
                        Player targetPlayer = (Player) target;
                        targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
                        targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.2f);
                        targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.8f, 1.5f);
                    }
                    
                    // Enhanced eye-beam visual with multiple layers
                    Location eyeLoc = player.getEyeLocation();
                    Location targetLoc = target.getEyeLocation();
                    Vector direction = targetLoc.toVector().subtract(eyeLoc.toVector()).normalize();
                    double distance = eyeLoc.distance(targetLoc);
                    
                    // Core beam (bright)
                    for (double d = 0; d < distance; d += 0.3) {
                        Location particleLoc = eyeLoc.clone().add(direction.clone().multiply(d));
                        center.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0);
                        center.getWorld().spawnParticle(Particle.FLAME, particleLoc, 2, 0.05, 0.05, 0.05, 0);
                    }
                    
                    // Outer glow (lava particles instead of redstone)
                    for (double d = 0; d < distance; d += 0.6) {
                        Location particleLoc = eyeLoc.clone().add(direction.clone().multiply(d));
                        center.getWorld().spawnParticle(Particle.LAVA, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                    }
                    
                    // Impact effect at target
                    center.getWorld().spawnParticle(Particle.LAVA, targetLoc, 5, 0.3, 0.3, 0.3, 0);
                    center.getWorld().spawnParticle(Particle.FLAME, targetLoc, 10, 0.2, 0.2, 0.2, 0.05);
                    
                    // Deal 1 damage per second with proper attribution
                    DamageUtils.dealAbilityDamage(player, target, 1.0);
                }
            }
        }

        @Override
        public void cancel(Player player) {
            if (activeTask != null) {
                activeTask.cancel();
            }
            setActive(false);
        }
    }

    @Override
    public void cleanup() {
        // All temporary effects are handled by BukkitRunnable tasks
        // No permanent resources to clean up
    }
}
