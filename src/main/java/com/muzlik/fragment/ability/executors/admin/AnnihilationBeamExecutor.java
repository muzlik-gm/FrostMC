package com.muzlik.fragment.ability.executors.admin;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Annihilation Beam - Admin Fragment Slot 4
 * Continuous laser beam from eyes
 * 10 damage per tick, 50 block range
 * Optional block destruction
 */
public class AnnihilationBeamExecutor implements AbilityExecutor {
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        double range = 50.0;
        int duration = 100; // 5 seconds
        
        FrostSMPPlugin plugin = (FrostSMPPlugin) player.getServer().getPluginManager().getPlugin("FrostSMP");
        boolean breakBlocks = plugin.getConfig().getBoolean("admin_fragment.break_blocks", false);
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= duration || !player.isOnline() || !player.isSneaking()) {
                    cancel();
                    return;
                }
                
                Vector direction = player.getEyeLocation().getDirection();
                Location origin = player.getEyeLocation();
                
                // Beam damage and effects
                for (double d = 0; d < range; d += 0.5) {
                    Location beamLoc = origin.clone().add(direction.clone().multiply(d));
                    
                    // Damage entities
                    for (Entity entity : beamLoc.getWorld().getNearbyEntities(beamLoc, 0.5, 0.5, 0.5)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            ((LivingEntity) entity).damage(10.0, player);
                            entity.setFireTicks(20);
                        }
                    }
                    
                    // Optional: Destroy blocks
                    if (breakBlocks && ticks % 5 == 0) {
                        Block block = beamLoc.getBlock();
                        if (block.getType().isSolid() && block.getType() != Material.BEDROCK) {
                            block.setType(Material.AIR);
                        }
                    }
                    
                    // Beam particles - thin line, not blocking view
                    if (d % 0.5 == 0) {
                        beamLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, beamLoc, 1, 0.05, 0.05, 0.05, 0);
                        beamLoc.getWorld().spawnParticle(Particle.END_ROD, beamLoc, 1, 0.02, 0.02, 0.02, 0);
                    }
                }
                
                // Sound every 10 ticks
                if (ticks % 10 == 0) {
                    player.getWorld().playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 2.0f);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        player.sendMessage("§4§lAnnihilation Beam activated! §cSneak to maintain beam.");
    }
}
