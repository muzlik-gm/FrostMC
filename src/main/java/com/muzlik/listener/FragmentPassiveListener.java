package com.muzlik.listener;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles fragment-specific passive effects:
 * - Fire Fragment: Permanent Fire Resistance
 * - Water Fragment: Water Breathing
 * - Air Fragment: Slow Falling while sneaking
 * - Void Fragment: Teleport to spawn when falling into void
 */
public class FragmentPassiveListener implements Listener {
    
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final Map<UUID, Boolean> sneakingPlayers = new HashMap<>();
    
    public FragmentPassiveListener(JavaPlugin plugin, FragmentManager fragmentManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        
        // Start passive effect task
        startPassiveEffectTask();
    }
    
    /**
     * Apply passive effects every second
     */
    private void startPassiveEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    FragmentType activeFragment = fragmentManager.getActiveFragment(player);
                    if (activeFragment == null) continue;
                    
                    switch (activeFragment) {
                        case FIRE:
                            // Permanent Fire Resistance
                            player.addPotionEffect(new PotionEffect(
                                PotionEffectType.FIRE_RESISTANCE, 
                                60, // 3 seconds duration (refreshed every second)
                                0, 
                                true, // ambient
                                false, // show particles
                                true // show icon
                            ));
                            break;
                            
                        case WATER:
                            // Water Breathing
                            player.addPotionEffect(new PotionEffect(
                                PotionEffectType.WATER_BREATHING,
                                60,
                                0,
                                true,
                                false,
                                true
                            ));
                            break;
                            
                        case AIR:
                            // Slow Falling only while sneaking
                            if (sneakingPlayers.getOrDefault(player.getUniqueId(), false)) {
                                player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.SLOW_FALLING,
                                    60,
                                    0,
                                    true,
                                    false,
                                    true
                                ));
                            }
                            break;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    /**
     * Track sneaking state for Air Fragment slow falling
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == FragmentType.AIR) {
            sneakingPlayers.put(player.getUniqueId(), event.isSneaking());
            
            // Remove slow falling immediately when stop sneaking
            if (!event.isSneaking()) {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            }
        }
    }
    
    /**
     * Void Fragment: Teleport to spawn when falling into void
     */
    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        
        Player player = (Player) event.getEntity();
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == FragmentType.VOID) {
            event.setCancelled(true);
            
            // Teleport to spawn
            player.teleport(player.getWorld().getSpawnLocation());
            player.sendMessage("§5✦ Void Fragment saved you from the void!");
            
            // VFX
            player.getWorld().spawnParticle(
                org.bukkit.Particle.PORTAL,
                player.getLocation(),
                50,
                0.5, 0.5, 0.5,
                0.5
            );
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        }
    }
}
