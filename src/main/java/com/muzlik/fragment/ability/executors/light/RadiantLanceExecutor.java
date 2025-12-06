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
        double baseDamage = 3.0; // EXTREME NERF: 12.0 → 3.0 (1.5 hearts, primary piercing beam)
        double damage = context.getScalingEngine().scaleDamage(baseDamage, rank);
        
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(context.getDirection().multiply(3.0));
        arrow.setDamage(damage);
        arrow.setGlowing(true);
        
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 50, 0.3, 0.3, 0.3, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 2.0f);
        player.sendMessage("§e✨ Radiant Lance!");
    }
}
