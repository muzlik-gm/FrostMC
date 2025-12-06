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
        
        // Powerful buffs - scale with rank
        int strengthLevel = 4 + (rank / 3); // Strength V → VI at rank 8
        int resistanceLevel = 3 + (rank / 3); // Resistance IV → V at rank 8
        int speedLevel = 2 + (rank / 4); // Speed III → IV at rank 8
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, strengthLevel, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, resistanceLevel, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, speedLevel, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 1, false, true, true));
        
        // MASSIVE VFX - MORE CHAOTIC AT HIGHER RANKS
        int dragonBreathCount = 300 + (rank * 50); // 300 → 700 at rank 8
        int flameCount = 200 + (rank * 40); // 200 → 520 at rank 8
        int lavaCount = 150 + (rank * 30); // 150 → 390 at rank 8
        
        // Dragon breath aura
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, dragonBreathCount, 3 + (rank * 0.3), 3, 3 + (rank * 0.3), 0.3 + (rank * 0.05));
        
        // Flame aura
        player.getWorld().spawnParticle(Particle.FLAME, loc, flameCount, 2.5 + (rank * 0.25), 2, 2.5 + (rank * 0.25), 0.2);
        
        // Lava particles
        player.getWorld().spawnParticle(Particle.LAVA, loc, lavaCount, 2 + (rank * 0.2), 1.5, 2 + (rank * 0.2), 0.1);
        
        // Soul fire for mystical effect
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 100 + (rank * 20), 2, 1, 2, 0.15);
        
        // End rod for power
        player.getWorld().spawnParticle(Particle.END_ROD, loc, 80 + (rank * 15), 2, 2, 2, 0.2);
        
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
