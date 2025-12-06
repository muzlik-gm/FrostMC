package com.muzlik.fragment.ability.executors.light;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.*;

public class RadiantLanceExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        double baseDamage = 3.0; // 1.5 hearts, primary piercing beam
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(context.getDirection().multiply(3.0));
        arrow.setDamage(damage);
        arrow.setGlowing(true);
        
        // Get plugin reference
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // CLEAN LIGHT VFX - focused, holy aesthetic
        // Reduced counts for primary ability + VFXLayerBuilder multiplier
        int coreCount = 8 + (rank * 2);        // 8 → 14 at rank 3 (was 50 raw)
        int secondaryCount = 4 + (rank * 1);   // 4 → 7 at rank 3
        int impactCount = 6 + (rank * 2);      // 6 → 12 at rank 3
        
        double spread = 0.15 + (rank * 0.03);  // Tight, precise light beam
        
        new com.muzlik.vfx.VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: END_ROD - clean light trail
            .core(Particle.END_ROD, coreCount, com.muzlik.vfx.ParticlePattern.LINE, spread, spread, spread, 0.06, null)
            // Secondary: FIREWORKS_SPARK - subtle shimmer
            .secondary(Particle.FIREWORKS_SPARK, secondaryCount, com.muzlik.vfx.ParticlePattern.POINT, spread * 0.5, spread * 0.5, spread * 0.5, 0.03, null)
            // Impact: Gentle light burst
            .impact(Particle.END_ROD, impactCount, com.muzlik.vfx.ParticlePattern.BURST, spread, spread, spread, 0.05, null)
            .spawn();
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 2.0f);
    }
}
