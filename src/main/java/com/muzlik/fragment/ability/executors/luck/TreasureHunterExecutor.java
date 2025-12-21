package com.muzlik.fragment.ability.executors.luck;

import com.muzlik.fragment.ability.AbilityContext;
import com.muzlik.fragment.ability.AbilityExecutor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Treasure Hunter - Luck Fragment Advanced Ability
 * Increases loot drops from mobs and adds bonus rare drops
 * 
 * Stats:
 * - Mana Cost: 45 - 4*rank
 * - Cooldown: 40 - 3*rank seconds
 * - Duration: 15 + 2*rank seconds
 * - Drop Multiplier: 1.0x + 0.2x*rank
 * - Rare Drop Chance: 10% + 5%*rank
 * 
 * VFX: Green sparkle particles around player
 * Sound: VILLAGER_YES on enhanced loot
 */
public class TreasureHunterExecutor implements AbilityExecutor {
    
    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final java.util.Map<UUID, Integer> playerRanks = new java.util.HashMap<>();
    
    // Rare drop pool - useful items only
    private static final List<Material> RARE_DROPS = Arrays.asList(
        Material.DIAMOND,
        Material.EMERALD,
        Material.GOLD_INGOT,
        Material.IRON_INGOT,
        Material.ENDER_PEARL,
        Material.GOLDEN_APPLE,
        Material.EXPERIENCE_BOTTLE,
        Material.NETHERITE_SCRAP,
        Material.ANCIENT_DEBRIS
    );
    
    @Override
    public void execute(AbilityContext context) {
        Player player = context.getPlayer();
        int rank = context.getRank();
        
        // Calculate duration based on rank
        int durationTicks = (15 + (2 * rank)) * 20; // Convert to ticks
        
        // Store player in active set with rank
        activePlayers.add(player.getUniqueId());
        playerRanks.put(player.getUniqueId(), rank);
        
        // Get plugin instance for scheduling
        com.muzlik.FrostSMPPlugin plugin = (com.muzlik.FrostSMPPlugin) 
            player.getServer().getPluginManager().getPlugin("FrostSMP");
        
        // Spawn green sparkle particles around player
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = durationTicks;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline() || player.isDead()) {
                    // Remove player from active set
                    activePlayers.remove(player.getUniqueId());
                    playerRanks.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                
                // Spawn green sparkle particles around player
                Location playerLoc = player.getLocation().add(0, 1, 0);
                
                // Create sparkle effect
                for (int i = 0; i < 5; i++) {
                    double offsetX = (Math.random() - 0.5) * 2.0;
                    double offsetY = Math.random() * 2.0;
                    double offsetZ = (Math.random() - 0.5) * 2.0;
                    
                    Location particleLoc = playerLoc.clone().add(offsetX, offsetY, offsetZ);
                    
                    // Green dust particles (RGB: 0, 255, 0)
                    playerLoc.getWorld().spawnParticle(
                        Particle.REDSTONE,
                        particleLoc,
                        1,
                        0, 0, 0,
                        0,
                        new Particle.DustOptions(
                            org.bukkit.Color.fromRGB(0, 255, 0),
                            0.9f
                        )
                    );
                    
                    // Add enchantment glint particles
                    if (Math.random() < 0.4) {
                        playerLoc.getWorld().spawnParticle(
                            Particle.ENCHANTMENT_TABLE,
                            particleLoc,
                            1,
                            0.1, 0.1, 0.1,
                            0.5
                        );
                    }
                }
                
                ticks += 6;
            }
        }.runTaskTimer(plugin, 0L, 6L);
        
        // Play activation sound
        player.getWorld().playSound(
            player.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP,
            0.7f,
            1.3f
        );
        
        player.sendMessage("§aTreasure Hunter activated!");
    }
    
    /**
     * Listener for handling loot enhancement
     */
    public static class TreasureHunterListener implements Listener {
        
        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            Player killer = event.getEntity().getKiller();
            
            if (killer == null) {
                return;
            }
            
            // Check if player has Treasure Hunter active
            if (!activePlayers.contains(killer.getUniqueId())) {
                return;
            }
            
            int rank = playerRanks.getOrDefault(killer.getUniqueId(), 1);
            
            // Calculate drop multiplier
            double dropMultiplier = 1.0 + (0.2 * rank);
            
            // Multiply existing drops
            List<ItemStack> drops = event.getDrops();
            List<ItemStack> enhancedDrops = new ArrayList<>();
            
            for (ItemStack drop : drops) {
                int originalAmount = drop.getAmount();
                int newAmount = (int) Math.ceil(originalAmount * dropMultiplier);
                
                ItemStack enhancedDrop = drop.clone();
                enhancedDrop.setAmount(newAmount);
                enhancedDrops.add(enhancedDrop);
            }
            
            // Clear and add enhanced drops
            drops.clear();
            drops.addAll(enhancedDrops);
            
            // Calculate rare drop chance
            double rareDropChance = 0.10 + (0.05 * rank);
            
            // Roll for bonus rare drops
            if (Math.random() < rareDropChance) {
                Material rareDrop = RARE_DROPS.get(new Random().nextInt(RARE_DROPS.size()));
                int amount = 1 + (rank / 4); // More items at higher ranks
                
                drops.add(new ItemStack(rareDrop, amount));
                
                killer.sendMessage("§a§l✦ RARE DROP! §7" + rareDrop.name().replace("_", " "));
            }
            
            // Spawn enhanced loot particles
            Location deathLoc = event.getEntity().getLocation().add(0, 1, 0);
            deathLoc.getWorld().spawnParticle(
                Particle.REDSTONE,
                deathLoc,
                30,
                0.5, 0.5, 0.5,
                0,
                new Particle.DustOptions(
                    org.bukkit.Color.fromRGB(0, 255, 0),
                    1.3f
                )
            );
            
            deathLoc.getWorld().spawnParticle(
                Particle.VILLAGER_HAPPY,
                deathLoc,
                15,
                0.5, 0.5, 0.5,
                0.1
            );
            
            // Play enhanced loot sound
            deathLoc.getWorld().playSound(
                deathLoc,
                Sound.ENTITY_VILLAGER_YES,
                1.0f,
                1.2f
            );
        }
    }
    
    /**
     * Check if a player has Treasure Hunter active
     * @param playerId The player's UUID
     * @return True if active
     */
    public static boolean isActive(UUID playerId) {
        return activePlayers.contains(playerId);
    }
    
    /**
     * Get the rank of a player's active Treasure Hunter
     * @param playerId The player's UUID
     * @return The rank, or 0 if not active
     */
    public static int getRank(UUID playerId) {
        return playerRanks.getOrDefault(playerId, 0);
    }
}
