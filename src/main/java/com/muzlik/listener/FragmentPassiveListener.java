package com.muzlik.listener;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
 * - Dark Fragment: Invisibility while standing still (3s delay)
 * - Light Fragment: Health regeneration when not in combat (10s)
 * - Void Fragment: Teleport to spawn when falling into void
 * - Storm Fragment: Speed boost during rain/storms
 * - Luck Fragment: Luck VI effect
 */
public class FragmentPassiveListener implements Listener {
    
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final Map<UUID, Boolean> sneakingPlayers = new HashMap<>();
    private final Map<UUID, Long> lastMovement = new HashMap<>();
    private final Map<UUID, Long> lastCombat = new HashMap<>();
    
    private static final long INVISIBILITY_DELAY = 3000L; // 3 seconds standing still
    private static final long COMBAT_DELAY = 10000L; // 10 seconds out of combat
    private static final long REGEN_INTERVAL = 2000L; // Regenerate every 2 seconds
    
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
                long currentTime = System.currentTimeMillis();
                
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    FragmentType activeFragment = fragmentManager.getActiveFragment(player);
                    if (activeFragment == null) continue;
                    
                    UUID playerId = player.getUniqueId();
                    
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
                            if (sneakingPlayers.getOrDefault(playerId, false)) {
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
                            
                        case DARK:
                            // Invisibility while standing still (3s delay)
                            Long lastMove = lastMovement.get(playerId);
                            if (lastMove != null && (currentTime - lastMove) >= INVISIBILITY_DELAY) {
                                player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.INVISIBILITY,
                                    60,
                                    0,
                                    true,
                                    false,
                                    true
                                ));
                            } else {
                                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                            }
                            break;
                            
                        case LIGHT:
                            // Health regeneration when not in combat (10s)
                            Long lastHit = lastCombat.get(playerId);
                            if (lastHit == null || (currentTime - lastHit) >= COMBAT_DELAY) {
                                // Regenerate 1 HP every 2 seconds
                                if (lastHit == null || (currentTime - lastHit) % REGEN_INTERVAL < 1000) {
                                    double currentHealth = player.getHealth();
                                    double maxHealth = player.getMaxHealth();
                                    if (currentHealth < maxHealth) {
                                        player.setHealth(Math.min(maxHealth, currentHealth + 1.0));
                                        
                                        // Subtle healing particles
                                        player.getWorld().spawnParticle(
                                            org.bukkit.Particle.HEART,
                                            player.getLocation().add(0, 2, 0),
                                            1,
                                            0.3, 0.3, 0.3,
                                            0
                                        );
                                    }
                                }
                            }
                            break;
                            
                        case STORM:
                            // Speed boost during rain/storms
                            if (player.getWorld().hasStorm() || player.getWorld().isThundering()) {
                                player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.SPEED,
                                    60,
                                    1, // Speed II
                                    true,
                                    false,
                                    true
                                ));
                            }
                            break;
                            
                        case LUCK:
                            // Luck VI effect
                            player.addPotionEffect(new PotionEffect(
                                PotionEffectType.LUCK,
                                60,
                                5, // Luck VI (level 6)
                                true,
                                false,
                                true
                            ));
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
     * Track movement for Dark Fragment invisibility
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == FragmentType.DARK) {
            // Check if player actually moved (not just head movement)
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {
                
                lastMovement.put(player.getUniqueId(), System.currentTimeMillis());
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }
    
    /**
     * Track combat for Light Fragment regeneration
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == FragmentType.LIGHT) {
            lastCombat.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
    
    /**
     * Track combat when player damages entity
     */
    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == FragmentType.LIGHT) {
            lastCombat.put(player.getUniqueId(), System.currentTimeMillis());
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
