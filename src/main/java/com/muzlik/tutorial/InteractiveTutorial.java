package com.muzlik.tutorial;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interactive Tutorial System - Learning by Doing
 * 
 * This system teaches players through actual gameplay actions rather than boring text.
 * Players learn by completing real tasks that naturally introduce game mechanics.
 */
public class InteractiveTutorial implements Listener {
    
    private final FrostSMPPlugin plugin;
    private final Map<UUID, TutorialState> playerStates;
    private final Map<UUID, BukkitTask> activeTimers;
    private final Set<UUID> completedPlayers;
    
    public InteractiveTutorial(FrostSMPPlugin plugin) {
        this.plugin = plugin;
        this.playerStates = new ConcurrentHashMap<>();
        this.activeTimers = new ConcurrentHashMap<>();
        this.completedPlayers = new HashSet<>();
        
        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // Load completed players from data
        loadCompletedPlayers();
    }
    
    /**
     * Tutorial phases that players progress through naturally
     */
    public enum TutorialPhase {
        WELCOME(0, "Welcome to FrostSMP!"),
        FIRST_KILL(1, "Combat Basics"),
        FRAGMENT_DISCOVERY(2, "Fragment Discovery"),
        FIRST_FRAGMENT(3, "Your First Fragment"),
        ABILITY_USAGE(4, "Using Abilities"),
        FRAGMENT_LEVELING(5, "Fragment Progression"),
        ADVANCED_COMBAT(6, "Advanced Combat"),
        MASTERY(7, "Fragment Mastery"),
        COMPLETED(8, "Tutorial Complete");
        
        private final int order;
        private final String displayName;
        
        TutorialPhase(int order, String displayName) {
            this.order = order;
            this.displayName = displayName;
        }
        
        public int getOrder() { return order; }
        public String getDisplayName() { return displayName; }
        
        public TutorialPhase getNext() {
            TutorialPhase[] phases = values();
            return order + 1 < phases.length ? phases[order + 1] : COMPLETED;
        }
    }
    
    /**
     * Player's current tutorial state
     */
    public static class TutorialState {
        public TutorialPhase currentPhase;
        public long phaseStartTime;
        public Map<String, Object> phaseData;
        public boolean isActive;
        public int hintsShown;
        
        public TutorialState() {
            this.currentPhase = TutorialPhase.WELCOME;
            this.phaseStartTime = System.currentTimeMillis();
            this.phaseData = new HashMap<>();
            this.isActive = true;
            this.hintsShown = 0;
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        plugin.getLogger().info("Player " + player.getName() + " joined - checking tutorial status");
        
        // Skip if player already completed tutorial
        if (completedPlayers.contains(playerId)) {
            plugin.getLogger().info("Player " + player.getName() + " has already completed tutorial");
            return;
        }
        
        // Skip if player is already in tutorial
        if (playerStates.containsKey(playerId)) {
            plugin.getLogger().info("Player " + player.getName() + " is already in tutorial");
            return;
        }
        
        // Delay tutorial check to ensure player is fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online
                if (!player.isOnline()) {
                    return;
                }
                
                // Start tutorial for new players OR existing players without fragment data
                boolean isNewPlayer = !player.hasPlayedBefore();
                boolean hasFragmentData = hasAnyFragmentData(player);
                
                plugin.getLogger().info("Player " + player.getName() + " - New: " + isNewPlayer + ", Has fragment data: " + hasFragmentData);
                
                if (isNewPlayer || !hasFragmentData) {
                    plugin.getLogger().info("Starting tutorial for player: " + player.getName());
                    startTutorial(player);
                } else {
                    plugin.getLogger().info("Player " + player.getName() + " doesn't need tutorial");
                }
            }
        }.runTaskLater(plugin, 40L); // 2 second delay to ensure full loading
    }
    
    /**
     * Start the interactive tutorial for a new player
     */
    public void startTutorial(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Initialize tutorial state
        TutorialState state = new TutorialState();
        playerStates.put(playerId, state);
        
        // Welcome the player with style
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                switch (count) {
                    case 0:
                        sendStyledMessage(player, "&d&l✦ &5Welcome to FrostSMP! &d&l✦");
                        break;
                    case 20:
                        sendStyledMessage(player, "&7You're about to discover the power of &bFragments&7...");
                        break;
                    case 40:
                        sendStyledMessage(player, "&eFirst, let's see what you're made of!");
                        sendStyledMessage(player, "&6➤ &eKill any mob to begin your journey");
                        // Advance to FIRST_KILL phase
                        advancePhase(player, TutorialPhase.FIRST_KILL);
                        cancel();
                        break;
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;
        
        Player player = event.getEntity().getKiller();
        UUID playerId = player.getUniqueId();
        TutorialState state = playerStates.get(playerId);
        
        if (state == null || !state.isActive) return;
        
        plugin.getLogger().info("DEBUG: Player " + player.getName() + " killed " + event.getEntity().getType() + " in phase " + state.currentPhase);
        
        if (state.currentPhase == TutorialPhase.FIRST_KILL) {
            // Player killed their first mob
            plugin.getLogger().info("DEBUG: Completing FIRST_KILL phase for " + player.getName());
            completePhase(player, "Great! You've proven yourself in combat.");
            
            // Move to fragment discovery
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendStyledMessage(player, "&a&l✓ &aCombat skills confirmed!");
                    sendStyledMessage(player, "&7The world holds ancient powers called &bFragments&7...");
                    sendStyledMessage(player, "&6➤ &eOpen your crafting table and look for fragment recipes");
                    
                    // Give them materials for their first fragment
                    giveFragmentMaterials(player);
                    advancePhase(player, TutorialPhase.FRAGMENT_DISCOVERY);
                }
            }.runTaskLater(plugin, 40L);
            
        } else if (state.currentPhase == TutorialPhase.WELCOME) {
            // Fallback: If somehow still in WELCOME phase, advance to FIRST_KILL and then complete it
            plugin.getLogger().info("DEBUG: Player " + player.getName() + " killed mob while in WELCOME phase, advancing and completing FIRST_KILL");
            state.currentPhase = TutorialPhase.FIRST_KILL;
            completePhase(player, "Great! You've proven yourself in combat.");
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendStyledMessage(player, "&a&l✓ &aCombat skills confirmed!");
                    sendStyledMessage(player, "&7The world holds ancient powers called &bFragments&7...");
                    sendStyledMessage(player, "&6➤ &eOpen your crafting table and look for fragment recipes");
                    
                    giveFragmentMaterials(player);
                    advancePhase(player, TutorialPhase.FRAGMENT_DISCOVERY);
                }
            }.runTaskLater(plugin, 40L);
            
        } else if (state.currentPhase == TutorialPhase.FRAGMENT_LEVELING) {
            // Track kills for fragment leveling phase - be more generous
            int kills = (int) state.phaseData.getOrDefault("leveling_kills", 0) + 1;
            state.phaseData.put("leveling_kills", kills);
            
            plugin.getLogger().info("DEBUG: Fragment leveling kill " + kills + "/3 for " + player.getName());
            
            if (kills >= 3) { // Reduced from 5 to 3 for faster progression
                completePhase(player, "Excellent! Your fragment is growing stronger!");
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sendStyledMessage(player, "&a&l✓ &aFragment leveling mastered!");
                        sendStyledMessage(player, "&7You're ready for advanced combat...");
                        sendStyledMessage(player, "&6➤ &eKill 5 more mobs to complete your training");
                        advancePhase(player, TutorialPhase.ADVANCED_COMBAT);
                    }
                }.runTaskLater(plugin, 20L);
            } else {
                sendActionBar(player, "&6Fragment Training: &e" + kills + "/3 kills");
            }
            
        } else if (state.currentPhase == TutorialPhase.ADVANCED_COMBAT) {
            // Track kills for advanced combat phase
            int kills = (int) state.phaseData.getOrDefault("kills", 0) + 1;
            state.phaseData.put("kills", kills);
            
            plugin.getLogger().info("DEBUG: Advanced combat kill " + kills + "/5 for " + player.getName());
            
            if (kills >= 5) {
                completePhase(player, "Excellent combat skills! You're ready for mastery.");
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sendStyledMessage(player, "&a&l✓ &aAdvanced combat mastered!");
                        sendStyledMessage(player, "&7You've learned the fundamentals of Fragment power...");
                        sendStyledMessage(player, "&6➤ &eExplore and experiment to become a true Fragment Master!");
                        advancePhase(player, TutorialPhase.MASTERY);
                        
                        // Complete tutorial after a short delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                completeTutorial(player);
                            }
                        }.runTaskLater(plugin, 60L); // 3 seconds
                    }
                }.runTaskLater(plugin, 20L);
            } else {
                sendActionBar(player, "&6Combat Progress: &e" + kills + "/5 kills");
            }
        }
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        TutorialState state = playerStates.get(playerId);
        
        if (state == null || !state.isActive) return;
        
        ItemStack result = event.getRecipe().getResult();
        plugin.getLogger().info("DEBUG: Player " + player.getName() + " crafted " + result.getType() + " in phase " + state.currentPhase);
        
        // Check if they crafted a fragment (more flexible detection)
        boolean isFragmentCraft = false;
        
        // Check by result item
        if (isFragmentItem(result) || result.getType() == Material.NETHER_STAR) {
            isFragmentCraft = true;
            plugin.getLogger().info("DEBUG: Fragment craft detected by result item");
        }
        
        // Check by display name
        if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
            String displayName = result.getItemMeta().getDisplayName().toLowerCase();
            if (displayName.contains("fragment") && displayName.contains("creation")) {
                isFragmentCraft = true;
                plugin.getLogger().info("DEBUG: Fragment craft detected by display name: " + displayName);
            }
        }
        
        // Check by recipe key (if available)
        try {
            if (event.getRecipe() instanceof org.bukkit.Keyed) {
                org.bukkit.Keyed keyedRecipe = (org.bukkit.Keyed) event.getRecipe();
                String key = keyedRecipe.getKey().getKey();
                if (key.contains("fragment_creation")) {
                    isFragmentCraft = true;
                    plugin.getLogger().info("DEBUG: Fragment craft detected by recipe key: " + key);
                }
            }
        } catch (Exception e) {
            // Ignore if recipe key check fails
        }
        
        if (isFragmentCraft && state.currentPhase == TutorialPhase.FRAGMENT_DISCOVERY) {
            plugin.getLogger().info("DEBUG: Completing FRAGMENT_DISCOVERY phase for " + player.getName());
            completePhase(player, "Amazing! You've created your first Fragment!");
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendStyledMessage(player, "&a&l✓ &aFragment created successfully!");
                    sendStyledMessage(player, "&7Now you need to &bactivate &7it...");
                    sendStyledMessage(player, "&6➤ &eRight-click with the fragment to activate it");
                    advancePhase(player, TutorialPhase.FIRST_FRAGMENT);
                }
            }.runTaskLater(plugin, 20L);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        TutorialState state = playerStates.get(playerId);
        
        if (state == null || !state.isActive) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            
            plugin.getLogger().info("DEBUG: Player " + player.getName() + " right-clicked with " + 
                (item != null ? item.getType() : "null") + " in phase " + state.currentPhase);
            
            if (item != null && isFragmentItem(item)) {
                if (state.currentPhase == TutorialPhase.FIRST_FRAGMENT) {
                    plugin.getLogger().info("DEBUG: Completing FIRST_FRAGMENT phase for " + player.getName());
                    // They're trying to activate their first fragment
                    completePhase(player, "Perfect! Your fragment is now active!");
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sendStyledMessage(player, "&a&l✓ &aFragment activated!");
                            sendStyledMessage(player, "&7You can now use &babilities&7!");
                            sendStyledMessage(player, "&6➤ &eUse any ability activation method");
                            sendStyledMessage(player, "&7   (Sneak+Click, Double-Sneak+Click, F+Click, or just Click)");
                            advancePhase(player, TutorialPhase.ABILITY_USAGE);
                            startAbilityHints(player);
                        }
                    }.runTaskLater(plugin, 20L);
                }
            }
        }
        
        // Check for ability usage - be more flexible about detection
        if (state.currentPhase == TutorialPhase.ABILITY_USAGE) {
            // Check if player has an active fragment
            FragmentType activeFragment = plugin.getFragmentManager().getActiveFragment(player);
            if (activeFragment != null) {
                // Any interaction while having an active fragment counts as ability attempt
                boolean isAbilityAttempt = false;
                
                // Check various ability activation patterns
                if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    isAbilityAttempt = true;
                    plugin.getLogger().info("DEBUG: Detected sneak+right-click ability attempt");
                } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    isAbilityAttempt = true;
                    plugin.getLogger().info("DEBUG: Detected left-click ability attempt");
                } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    isAbilityAttempt = true;
                    plugin.getLogger().info("DEBUG: Detected right-click ability attempt");
                }
                
                if (isAbilityAttempt) {
                    plugin.getLogger().info("DEBUG: Completing ABILITY_USAGE phase for " + player.getName());
                    completePhase(player, "Incredible! You've mastered ability usage!");
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sendStyledMessage(player, "&a&l✓ &aAbility mastered!");
                            sendStyledMessage(player, "&7Fragments grow stronger as you use them...");
                            sendStyledMessage(player, "&6➤ &eKill more mobs to level up your fragment");
                            advancePhase(player, TutorialPhase.FRAGMENT_LEVELING);
                        }
                    }.runTaskLater(plugin, 20L);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerAnimation(org.bukkit.event.player.PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        TutorialState state = playerStates.get(playerId);
        
        if (state == null || !state.isActive) return;
        
        // Only handle arm swing animation
        if (event.getAnimationType() != org.bukkit.event.player.PlayerAnimationType.ARM_SWING) {
            return;
        }
        
        if (state.currentPhase == TutorialPhase.ABILITY_USAGE) {
            // Check if player has an active fragment
            FragmentType activeFragment = plugin.getFragmentManager().getActiveFragment(player);
            if (activeFragment != null) {
                plugin.getLogger().info("DEBUG: Player " + player.getName() + " used arm swing with active fragment " + activeFragment);
                plugin.getLogger().info("DEBUG: Completing ABILITY_USAGE phase for " + player.getName());
                
                completePhase(player, "Incredible! You've mastered ability usage!");
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sendStyledMessage(player, "&a&l✓ &aAbility mastered!");
                        sendStyledMessage(player, "&7Fragments grow stronger as you use them...");
                        sendStyledMessage(player, "&6➤ &eKill more mobs to level up your fragment");
                        advancePhase(player, TutorialPhase.FRAGMENT_LEVELING);
                    }
                }.runTaskLater(plugin, 20L);
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        TutorialState state = playerStates.get(player.getUniqueId());
        
        if (state == null || !state.isActive) return;
        
        // Provide contextual hints based on location and phase
        if (state.currentPhase == TutorialPhase.FRAGMENT_DISCOVERY) {
            // Hint about crafting if they're near a crafting table
            if (isNearCraftingTable(player.getLocation())) {
                if (state.hintsShown < 1) {
                    sendStyledMessage(player, "&7💡 &eHint: Look for fragment recipes in your crafting interface!");
                    state.hintsShown++;
                }
            }
        }
    }
    
    /**
     * Complete current phase and show celebration
     */
    private void completePhase(Player player, String message) {
        TutorialState state = playerStates.get(player.getUniqueId());
        if (state == null) return;
        
        // Cancel any active timers
        BukkitTask timer = activeTimers.remove(player.getUniqueId());
        if (timer != null) {
            timer.cancel();
        }
        
        // Show completion effects
        showCompletionEffects(player);
        sendStyledMessage(player, "&a&l✓ &a" + message);
        
        // Play success sound
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }
    
    /**
     * Advance to next tutorial phase
     */
    private void advancePhase(Player player, TutorialPhase nextPhase) {
        TutorialState state = playerStates.get(player.getUniqueId());
        if (state == null) return;
        
        state.currentPhase = nextPhase;
        state.phaseStartTime = System.currentTimeMillis();
        state.phaseData.clear();
        state.hintsShown = 0;
        
        if (nextPhase == TutorialPhase.COMPLETED) {
            completeTutorial(player);
        } else {
            startPhaseTimer(player, nextPhase);
        }
    }
    
    /**
     * Complete the entire tutorial
     */
    private void completeTutorial(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Mark as completed
        completedPlayers.add(playerId);
        playerStates.remove(playerId);
        
        // Cancel any timers
        BukkitTask timer = activeTimers.remove(playerId);
        if (timer != null) {
            timer.cancel();
        }
        
        // Grand finale
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                switch (count) {
                    case 0:
                        sendStyledMessage(player, "");
                        sendStyledMessage(player, "&d&l✦ &5&lTUTORIAL COMPLETE! &d&l✦");
                        sendStyledMessage(player, "");
                        break;
                    case 20:
                        sendStyledMessage(player, "&7You've mastered the basics of &bFragment Power&7!");
                        break;
                    case 40:
                        sendStyledMessage(player, "&eYour journey has just begun...");
                        break;
                    case 60:
                        sendStyledMessage(player, "&6Explore, experiment, and become a &bFragment Master&6!");
                        showGrandFinaleEffects(player);
                        cancel();
                        break;
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        
        // Save completion status
        saveCompletedPlayer(playerId);
    }
    
    /**
     * Start helpful hints and timers for phases
     */
    private void startPhaseTimer(Player player, TutorialPhase phase) {
        UUID playerId = player.getUniqueId();
        
        // Cancel existing timer
        BukkitTask existingTimer = activeTimers.get(playerId);
        if (existingTimer != null) {
            existingTimer.cancel();
        }
        
        // Start new timer for hints
        BukkitTask timer = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                
                // Provide helpful hints every 30 seconds
                if (ticks % 600 == 0) {
                    providePhaseHint(player, phase, ticks / 600);
                }
                
                // Auto-advance if stuck for too long (5 minutes)
                if (ticks >= 6000) {
                    sendStyledMessage(player, "&7⏰ Let's move on to keep things interesting!");
                    autoAdvancePhase(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        activeTimers.put(playerId, timer);
    }
    
    /**
     * Provide contextual hints for each phase
     */
    private void providePhaseHint(Player player, TutorialPhase phase, int hintNumber) {
        switch (phase) {
            case FIRST_KILL:
                if (hintNumber == 1) {
                    sendStyledMessage(player, "&7💡 &eTip: Any mob will do - even a chicken!");
                } else if (hintNumber == 2) {
                    sendStyledMessage(player, "&7💡 &eTip: Look around for animals or monsters to fight");
                }
                break;
                
            case FRAGMENT_DISCOVERY:
                if (hintNumber == 1) {
                    sendStyledMessage(player, "&7💡 &eTip: Open a crafting table and look for new recipes");
                } else if (hintNumber == 2) {
                    sendStyledMessage(player, "&7💡 &eTip: Fragment recipes use the materials I gave you");
                }
                break;
                
            case ABILITY_USAGE:
                if (hintNumber == 1) {
                    sendStyledMessage(player, "&7💡 &eTip: Hold Shift and right-click to use abilities");
                } else if (hintNumber == 2) {
                    sendStyledMessage(player, "&7💡 &eTip: Make sure you have your fragment selected");
                }
                break;
        }
    }
    
    /**
     * Auto-advance if player gets stuck
     */
    private void autoAdvancePhase(Player player) {
        TutorialState state = playerStates.get(player.getUniqueId());
        if (state == null) return;
        
        TutorialPhase nextPhase = state.currentPhase.getNext();
        
        // Give them what they need to continue
        switch (state.currentPhase) {
            case FRAGMENT_DISCOVERY:
                // Give them a fragment directly
                giveFirstFragment(player);
                sendStyledMessage(player, "&7I've given you a fragment to keep things moving!");
                break;
                
            case FIRST_FRAGMENT:
                // Activate fragment for them
                activateFirstFragment(player);
                sendStyledMessage(player, "&7I've activated your fragment for you!");
                break;
        }
        
        advancePhase(player, nextPhase);
    }
    
    /**
     * Give materials needed for fragment creation
     */
    private void giveFragmentMaterials(Player player) {
        // Give materials for FIRE fragment (simplest recipe)
        // Recipe: Netherrack + Fire Charge + Diamond in OIO/ICI/OIO pattern
        player.getInventory().addItem(new ItemStack(Material.NETHERRACK, 4)); // 4 for outer positions
        player.getInventory().addItem(new ItemStack(Material.FIRE_CHARGE, 4)); // 4 for inner positions  
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1)); // 1 for center
        
        sendStyledMessage(player, "&7📦 &eI've given you materials to create a &cFire Fragment&e!");
        sendStyledMessage(player, "&7Recipe: &6Netherrack &7(corners), &6Fire Charge &7(sides), &6Diamond &7(center)");
        sendStyledMessage(player, "&7Pattern: &6N F N");
        sendStyledMessage(player, "&7         &6F D F");
        sendStyledMessage(player, "&7         &6N F N");
    }
    
    /**
     * Visual effects for phase completion
     */
    private void showCompletionEffects(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Use safe particles that work reliably
        try {
            player.spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, loc, 15, 0.5, 0.5, 0.5, 0.1);
            player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, loc, 10, 1, 1, 1, 0);
            player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, loc, 8, 0.8, 0.8, 0.8, 0);
        } catch (Exception e) {
            // Fallback to basic particles if any fail
            plugin.getLogger().warning("Failed to show completion effects for " + player.getName() + ": " + e.getMessage());
            try {
                player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, loc, 5, 1, 1, 1, 0);
            } catch (Exception fallbackError) {
                // If even basic particles fail, just skip effects
                plugin.getLogger().warning("All particle effects failed for " + player.getName());
            }
        }
    }
    
    /**
     * Grand finale effects
     */
    private void showGrandFinaleEffects(Player player) {
        Location loc = player.getLocation();
        
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 20) {
                    cancel();
                    return;
                }
                
                // Epic particle display with safe particles
                Location effectLoc = loc.clone().add(
                    Math.cos(count * 0.3) * 2,
                    count * 0.1,
                    Math.sin(count * 0.3) * 2
                );
                
                try {
                    // Use safe particles that don't require additional data
                    player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, effectLoc, 5, 0.1, 0.1, 0.1, 0);
                    player.spawnParticle(org.bukkit.Particle.END_ROD, effectLoc, 3, 0.2, 0.2, 0.2, 0.05);
                    player.spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, effectLoc, 2, 0.3, 0.3, 0.3, 0.1);
                    player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, effectLoc, 1, 0.1, 0.1, 0.1, 0);
                    
                    if (count % 5 == 0) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.0f + count * 0.1f);
                    }
                } catch (Exception e) {
                    // If particles fail, just play sound
                    if (count % 5 == 0) {
                        try {
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f + count * 0.1f);
                        } catch (Exception soundError) {
                            // Even sound failed, just continue
                        }
                    }
                }
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Start ability usage hints
     */
    private void startAbilityHints(Player player) {
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 15) { // Show hints for longer
                    cancel();
                    return;
                }
                
                // Cycle through different hint messages
                switch (count % 4) {
                    case 0:
                        sendActionBar(player, "&6Try: &e&lSHIFT &6+ &e&lRIGHT CLICK &6to use abilities!");
                        break;
                    case 1:
                        sendActionBar(player, "&6Or try: &e&lLEFT CLICK &6or &e&lRIGHT CLICK &6alone!");
                        break;
                    case 2:
                        sendActionBar(player, "&6Advanced: &e&lF &6+ &e&lCLICK &6or &e&lDOUBLE SNEAK &6+ &e&lCLICK&6!");
                        break;
                    case 3:
                        sendActionBar(player, "&6Any interaction with an active fragment works!");
                        break;
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }
    
    // Utility methods
    private void sendStyledMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    private void sendActionBar(Player player, String message) {
        player.sendActionBar(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    private boolean hasPlayedBefore(Player player) {
        // Check if player has any fragment data or has played for more than 5 minutes
        return plugin.getDataPersistence().hasPlayerData(player.getUniqueId()).join() ||
               player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) > 6000; // 5 minutes in ticks
    }
    
    /**
     * Check if player has any fragment-related data (simpler check for existing players)
     */
    private boolean hasAnyFragmentData(Player player) {
        try {
            // Check if player has any existing fragment data
            boolean hasData = plugin.getDataPersistence().hasPlayerData(player.getUniqueId()).join();
            plugin.getLogger().info("DEBUG: Player " + player.getName() + " has fragment data: " + hasData);
            
            // Also check if they have any fragment-related items in inventory
            if (!hasData) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && isFragmentItem(item)) {
                        plugin.getLogger().info("DEBUG: Player " + player.getName() + " has fragment item in inventory");
                        return true;
                    }
                }
            }
            
            return hasData;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check fragment data for " + player.getName() + ": " + e.getMessage());
            // If we can't check, assume they don't have data to be safe
            return false;
        }
    }
    
    /**
     * Offer tutorial to existing players who don't have fragment data
     */
    private void offerTutorialToExistingPlayer(Player player) {
        sendStyledMessage(player, "");
        sendStyledMessage(player, "&d&l✦ &5Welcome to the Fragment System! &d&l✦");
        sendStyledMessage(player, "&7It looks like you haven't used Fragments yet.");
        sendStyledMessage(player, "&7Would you like to learn how? Type &e/tutorial start &7to begin!");
        sendStyledMessage(player, "");
    }
    
    private boolean isFragmentItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        
        // Check display name first (most reliable)
        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName().toLowerCase();
            if (displayName.contains("fragment") && !displayName.contains("creation") && !displayName.contains("changer")) {
                plugin.getLogger().info("DEBUG: Fragment detected by display name: " + displayName);
                return true;
            }
        }
        
        // Check for fragment activator lore pattern
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                String cleanLine = ChatColor.stripColor(line).toLowerCase();
                if (cleanLine.contains("fragment_activator") || 
                    cleanLine.contains("fragment activator") ||
                    cleanLine.contains("right-click to activate") ||
                    cleanLine.contains("fragment power") ||
                    cleanLine.contains("elemental fragment")) {
                    plugin.getLogger().info("DEBUG: Fragment detected by lore: " + cleanLine);
                    return true;
                }
            }
        }
        
        // Check for custom model data (backup check)
        if (meta.hasCustomModelData()) {
            int cmd = meta.getCustomModelData();
            // Fragment textures are typically in specific ranges
            if (cmd >= 1000 && cmd <= 2000) { // Adjust range based on your texture registry
                plugin.getLogger().info("DEBUG: Fragment detected by custom model data: " + cmd);
                return true;
            }
        }
        
        // Check for specific materials that might be fragments
        boolean isMaterialMatch = item.getType() == Material.NETHER_STAR || 
                                 item.getType() == Material.PAPER ||
                                 item.getType() == Material.ENDER_EYE;
        
        if (isMaterialMatch) {
            plugin.getLogger().info("DEBUG: Fragment detected by material: " + item.getType());
        }
        
        return isMaterialMatch;
    }
    
    private boolean isNearCraftingTable(Location location) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (location.clone().add(x, y, z).getBlock().getType() == Material.CRAFTING_TABLE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void giveFirstFragment(Player player) {
        // Use the plugin's fragment system to give a basic fragment
        try {
            plugin.getFragmentManager().grantFragment(player, FragmentType.FIRE);
        } catch (Exception e) {
            // Fallback: give a basic item
            ItemStack fragment = new ItemStack(Material.NETHER_STAR);
            fragment.getItemMeta().setDisplayName(ChatColor.RED + "Fire Fragment");
            player.getInventory().addItem(fragment);
        }
    }
    
    private void activateFirstFragment(Player player) {
        // Use the plugin's fragment system to activate the first available fragment
        try {
            plugin.getFragmentManager().setActiveFragment(player, FragmentType.FIRE);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not auto-activate fragment for tutorial player: " + player.getName());
        }
    }
    
    private void loadCompletedPlayers() {
        // Load from plugin data system
        try {
            // Use the plugin's data persistence system to check for completed tutorials
            // This is a simple implementation - in a full system you'd store this in player data
            plugin.getLogger().info("Loading tutorial completion data...");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load tutorial completion data: " + e.getMessage());
        }
    }
    
    private void saveCompletedPlayer(UUID playerId) {
        // Save to plugin data system
        try {
            // In a full implementation, this would be saved to the player's data file
            // For now, we'll rely on the in-memory set
            plugin.getLogger().info("Tutorial completed for player: " + playerId);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save tutorial completion for " + playerId + ": " + e.getMessage());
        }
    }
    
    /**
     * Force start tutorial for a player (public method for commands)
     */
    public void forceStartTutorial(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Check if player already has an active tutorial
        TutorialState existingState = playerStates.get(playerId);
        if (existingState != null && existingState.isActive) {
            // Don't restart, just provide current phase hint
            sendStyledMessage(player, "&7Tutorial is already active!");
            provideCurrentPhaseHint(player);
            return;
        }
        
        // Remove from completed if they were there (admin reset)
        completedPlayers.remove(playerId);
        
        // Cancel any existing tutorial state
        if (existingState != null) {
            BukkitTask timer = activeTimers.remove(playerId);
            if (timer != null) {
                timer.cancel();
            }
            playerStates.remove(playerId);
        }
        
        // Start fresh tutorial
        startTutorial(player);
        sendStyledMessage(player, "&7Tutorial started!");
    }
    
    /**
     * Check if player has completed tutorial
     */
    public boolean hasCompletedTutorial(Player player) {
        return completedPlayers.contains(player.getUniqueId());
    }
    
    /**
     * Check if player is currently in tutorial
     */
    public boolean isInTutorial(Player player) {
        TutorialState state = playerStates.get(player.getUniqueId());
        return state != null && state.isActive;
    }
    
    /**
     * Get player's current tutorial phase
     */
    public String getCurrentPhase(Player player) {
        TutorialState state = playerStates.get(player.getUniqueId());
        if (state != null && state.isActive) {
            return state.currentPhase.getDisplayName();
        }
        return "Not in tutorial";
    }
    
    /**
     * Provide a helpful hint for the player's current phase
     */
    public void provideCurrentPhaseHint(Player player) {
        TutorialState state = playerStates.get(player.getUniqueId());
        if (state == null || !state.isActive) {
            return;
        }
        
        switch (state.currentPhase) {
            case WELCOME:
                sendStyledMessage(player, "&6➤ &eWait for the tutorial to begin...");
                break;
                
            case FIRST_KILL:
                sendStyledMessage(player, "&6➤ &eKill any mob to prove your combat skills!");
                break;
                
            case FRAGMENT_DISCOVERY:
                sendStyledMessage(player, "&6➤ &eOpen your crafting table and look for fragment recipes");
                sendStyledMessage(player, "&7Use the materials I gave you to craft a Fire Fragment");
                break;
                
            case FIRST_FRAGMENT:
                sendStyledMessage(player, "&6➤ &eRight-click with your fragment to activate it");
                break;
                
            case ABILITY_USAGE:
                sendStyledMessage(player, "&6➤ &eUse any ability activation method:");
                sendStyledMessage(player, "&7   Try Shift+Click, Left/Right Click, F+Click, or Double-Sneak+Click");
                break;
                
            case FRAGMENT_LEVELING:
                int levelingKills = (int) state.phaseData.getOrDefault("leveling_kills", 0);
                sendStyledMessage(player, "&6➤ &eKill more mobs to level up your fragment");
                sendStyledMessage(player, "&7Progress: " + levelingKills + "/3 kills");
                break;
                
            case ADVANCED_COMBAT:
                int combatKills = (int) state.phaseData.getOrDefault("kills", 0);
                sendStyledMessage(player, "&6➤ &eKill more mobs to complete your training");
                sendStyledMessage(player, "&7Progress: " + combatKills + "/5 kills");
                break;
                
            case MASTERY:
                sendStyledMessage(player, "&6➤ &eAlmost done! The tutorial will complete shortly...");
                break;
                
            default:
                sendStyledMessage(player, "&6➤ &eFollow the tutorial instructions to continue");
                break;
        }
    }
    
    /**
     * Force complete tutorial for a player (admin command)
     */
    public void forceCompleteTutorial(Player player) {
        completeTutorial(player);
    }
    
    /**
     * Reset tutorial for a player (admin command)
     */
    public void resetTutorial(Player player) {
        UUID playerId = player.getUniqueId();
        completedPlayers.remove(playerId);
        playerStates.remove(playerId);
        
        BukkitTask timer = activeTimers.remove(playerId);
        if (timer != null) {
            timer.cancel();
        }
        
        sendStyledMessage(player, "&7Tutorial reset! Rejoin to start over.");
    }
    
    /**
     * Get tutorial statistics
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("completed_players", completedPlayers.size());
        stats.put("active_tutorials", playerStates.size());
        
        // Phase distribution
        Map<String, Integer> phaseDistribution = new HashMap<>();
        for (TutorialState state : playerStates.values()) {
            String phase = state.currentPhase.name();
            phaseDistribution.put(phase, phaseDistribution.getOrDefault(phase, 0) + 1);
        }
        stats.put("phase_distribution", phaseDistribution);
        
        return stats;
    }
    
    /**
     * Shutdown and cleanup
     */
    public void shutdown() {
        // Cancel all active timers
        for (BukkitTask timer : activeTimers.values()) {
            timer.cancel();
        }
        activeTimers.clear();
        
        // Save all completion data
        for (UUID playerId : completedPlayers) {
            saveCompletedPlayer(playerId);
        }
    }
}