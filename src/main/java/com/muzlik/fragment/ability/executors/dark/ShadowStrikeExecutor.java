package com.muzlik.fragment.ability.executors.dark;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class ShadowStrikeExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = context.getDirection().clone().normalize();
        int rank = context.getRank();
        double baseDamage = 3.0;
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        // Launch wither skull from eye location - EXTREMELY FAST
        WitherSkull skull = player.launchProjectile(WitherSkull.class);
        skull.setVelocity(direction.multiply(4.0 + (rank * 0.4))); // VERY FAST: 4.0-5.2 speed
        skull.setCharged(rank >= 3); // Charged (blue) at rank 3+
        
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // SUBTLE DARK VFX - shadows should be mysterious, not overwhelming
        // Further reduced by VFXLayerBuilder's rank multiplier at low ranks
        int coreCount = 12 + (rank * 4);       // 12 → 28 at rank 4 (was 30 → 90)
        int secondaryCount = 8 + (rank * 3);   // 8 → 20 at rank 4 (was 20 → 68)
        int ambientCount = 6 + (rank * 2);     // 6 → 14 at rank 4 (was 15 → 55)
        int impactCount = 10 + (rank * 3);     // 10 → 22 at rank 4 (was 25 → 85)
        
        double spread = 0.2 + (rank * 0.06);   // Focused spread (was 0.3 + rank * 0.12)
        
        // 5-Layer VFX System - Subtle, sinister shadows
        com.muzlik.vfx.VFXLayerBuilder vfxBuilder = new com.muzlik.vfx.VFXLayerBuilder(plugin, eyeLoc, rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: SMOKE_LARGE - wisping shadow tendrils
            .core(Particle.SMOKE_LARGE, coreCount, com.muzlik.vfx.ParticlePattern.SPIRAL, spread, spread, spread, 0.03 + (rank * 0.01), null)
            // Secondary: SQUID_INK - subtle dark accent
            .secondary(Particle.SQUID_INK, secondaryCount, com.muzlik.vfx.ParticlePattern.POINT, spread * 0.5, spread * 0.5, spread * 0.5, 0.02, null)
            // Ambient: SMOKE_NORMAL - faint haze
            .ambient(Particle.SMOKE_NORMAL, ambientCount, com.muzlik.vfx.ParticlePattern.SPHERE, spread * 0.8, spread * 0.8, spread * 0.8, 0.01, null)
            // Impact: Dark burst - controlled, ominous
            .impact(Particle.SMOKE_LARGE, impactCount, com.muzlik.vfx.ParticlePattern.BURST, spread, spread, spread, 0.04 + (rank * 0.01), null);
        
        // Cinematic: Darkness pulse at rank 2+, more intense at higher ranks
        if (rank >= 2) {
            vfxBuilder.cinematic(0.15 + (rank * 0.03), com.muzlik.vfx.CinematicEffect.DARKNESS_PULSE);
        }
        
        vfxBuilder.spawn();
        
        float pitch = 1.5f - (rank * 0.05f); // Lower pitch at higher ranks (more ominous)
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_WITHER_SHOOT, 1.0f + (rank * 0.15f), pitch);
    }
}
