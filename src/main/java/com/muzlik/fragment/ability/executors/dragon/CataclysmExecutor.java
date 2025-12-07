package com.muzlik.fragment.ability.executors.dragon;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CataclysmExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        Location loc = player.getLocation();
        int rank = context.getRank();
        double baseDuration = 10.0;
        int duration = (int) (context.getScalingEngine().scaleDuration(baseDuration, rank) * 20);
        
        // Balanced buffs - keep under level 3 (level 0 = I, level 1 = II, level 2 = III)
        int strengthLevel = Math.min(2, 1 + (rank / 4)); // Strength II → IV at rank 8, capped at IV
        int resistanceLevel = Math.min(2, 1 + (rank / 4)); // Resistance II → IV at rank 8, capped at IV
        int speedLevel = Math.min(1, rank / 5); // Speed I → III at rank 10, capped at III
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, strengthLevel, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, resistanceLevel, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, speedLevel, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 1, false, true, true)); // Regen II
        
        // Minimal VFX - clean and visible
        int flameCount = 15 + (rank * 2); // 15 → 31 at rank 8
        int lavaCount = 10 + rank; // 10 → 18 at rank 8
        int soulFireCount = 12 + (rank * 2); // 12 → 28 at rank 8
        
        // Particle effects wrapped in try-catch to prevent crashes
        try {
            // Flame aura
            player.getWorld().spawnParticle(Particle.FLAME, loc, flameCount, 2.0, 1.5, 2.0, 0.1);
            
            // Lava particles
            player.getWorld().spawnParticle(Particle.LAVA, loc, lavaCount, 1.5, 1.0, 1.5, 0.05);
            
            // Soul fire for mystical effect (replaces dragon breath)
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, soulFireCount, 2.0, 2.0, 2.0, 0.15);
            
            // End rod for power
            player.getWorld().spawnParticle(Particle.END_ROD, loc, 8 + rank, 1.5, 1.5, 1.5, 0.1);
        } catch (Exception ignored) {}
        
        // Sound - more intense at higher ranks
        float volume = 2.0f + (rank * 0.15f);
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_AMBIENT, volume, 0.5f);
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, volume * 0.8f, 0.6f);
        player.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, volume * 0.6f, 0.7f);
        
        // Cinematic effect
        if (rank >= 5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false, false));
        }
        
    }
}
