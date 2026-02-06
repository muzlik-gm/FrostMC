package com.muzlik.command;

import com.muzlik.config.ConfigManager;
import com.muzlik.character.CharacterLevelManager;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.mana.ManaManager;
import com.muzlik.recipe.RecipeManager;
import com.muzlik.ui.UIManager;
import com.muzlik.FrostSMPPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Command for Fragment system operations.
 */
public class FragmentCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final RecipeManager recipeManager;
    private final UIManager uiManager;
    private final ManaManager manaManager;
    private final LevelManager levelManager;
    private final RankManager rankManager;
    private final com.muzlik.cooldown.CooldownManager cooldownManager;
    private CharacterLevelManager characterLevelManager;
    private ConfigManager configManager;

    public FragmentCommand(JavaPlugin plugin, FragmentManager fragmentManager, RecipeManager recipeManager, 
                          UIManager uiManager, ManaManager manaManager, LevelManager levelManager, RankManager rankManager,
                          com.muzlik.cooldown.CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.cooldownManager = cooldownManager;
        this.recipeManager = recipeManager;
        this.uiManager = uiManager;
        this.manaManager = manaManager;
        this.levelManager = levelManager;
        this.rankManager = rankManager;
        
        // Get managers from main plugin
        if (plugin instanceof FrostSMPPlugin) {
            this.characterLevelManager = ((FrostSMPPlugin) plugin).getCharacterLevelManager();
            this.configManager = ((FrostSMPPlugin) plugin).getConfigManager();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                sendHelp((Player) sender);
            } else {
                sender.sendMessage("Usage: /fragment <subcommand>");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Commands that don't require player sender
        if (subCommand.equals("reload")) {
            handleReload(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players");
            return true;
        }

        Player player = (Player) sender;

        switch (subCommand) {
            case "gui":
                uiManager.openFragmentOverview(player);
                break;
            case "give":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                // If admin and no args, open GUI
                if (args.length == 1) {
                    uiManager.openFragmentGiveGUI(player);
                } else {
                    handleGive(player, args);
                }
                break;
            case "set":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                handleSet(player, args);
                break;
            case "reset":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                handleReset(player, args);
                break;
            case "list":
                showFragmentList(player);
                break;
            case "info":
                showFragmentInfo(player);
                break;
            case "abilities":
                showAbilitiesList(player);
                break;
            case "grant":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /fragment grant <type>");
                    return true;
                }
                grantFragment(player, args[1]);
                break;
            case "controls":
            case "control":
                handleControls(player, args);
                break;
            case "toggle":
                handleToggle(player);
                break;
            case "withdraw":
            case "deactivate":
                handleWithdraw(player);
                break;
            case "forceactivate":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /fragment forceactivate <player> <type>");
                    return true;
                }
                forceActivateFragment(player, args[1], args[2]);
                break;
            case "generatepack":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                generateResourcePack(player);
                break;
            case "level":
                showCharacterLevel(player);
                break;
            case "recipes":
                com.muzlik.ui.RecipeDiscoveryGUI.openMainGUI(player);
                break;
            case "debug":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                handleDebug(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("fragment.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command");
            return;
        }
        
        if (plugin instanceof FrostSMPPlugin) {
            FrostSMPPlugin frostPlugin = (FrostSMPPlugin) plugin;
            
            // Reload plugin config and update runtime state (including mana system)
            frostPlugin.reloadPluginConfig();
            
            // Reload block manipulation engine configuration
            com.muzlik.block.BlockManipulationEngine blockEngine = frostPlugin.getBlockManipulationEngine();
            if (blockEngine != null) {
                blockEngine.reloadConfiguration();
            }
            
            sender.sendMessage("§aConfiguration reloaded successfully");
            sender.sendMessage("§7Mana System: " + (configManager.isManaSystemEnabled() ? "§aENABLED" : "§cDISABLED"));
        } else {
            sender.sendMessage("§cPlugin not initialized properly");
        }
    }

    private void handleGive(Player player, String[] args) {
        // Usage: /fragment give <player> <fragment>
        // Or legacy: /fragment give <item> (to self)
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /fragment give <player> <fragment> OR /fragment give <item>");
            return;
        }

        // Check if args[1] is a player
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target != null && args.length >= 3) {
            // Giving to another player: /fragment give <player> <fragment>
            giveItemToPlayer(target, args[2], player);
        } else {
            // Giving to self: /fragment give <item>
            // Check if args[1] is a valid item name
            if (!player.hasPermission("fragment.admin")) {
                player.sendMessage("§cYou don't have permission to use this command");
                return;
            }
            giveItemToPlayer(player, args[1], null);
        }
    }

    private void giveItemToPlayer(Player target, String itemName, CommandSender giver) {
        ItemStack item = null;
        switch (itemName.toLowerCase()) {
            // Fragment Creation items (ritual catalysts)
            case "fire": item = createFragmentCreationItem(FragmentType.FIRE); break;
            case "water": item = createFragmentCreationItem(FragmentType.WATER); break;
            case "air": item = createFragmentCreationItem(FragmentType.AIR); break;
            case "dark": item = createFragmentCreationItem(FragmentType.DARK); break;
            case "light": item = createFragmentCreationItem(FragmentType.LIGHT); break;
            case "void": item = createFragmentCreationItem(FragmentType.VOID); break;
            case "dragon": item = createFragmentCreationItem(FragmentType.DRAGON); break;
            case "storm": item = createFragmentCreationItem(FragmentType.STORM); break;
            case "time": item = createFragmentCreationItem(FragmentType.TIME); break;
            case "luck": item = createFragmentCreationItem(FragmentType.LUCK); break;
            
            // Actual Fragment items (with textures)
            case "fire_fragment": item = recipeManager.createFragmentItem(FragmentType.FIRE); break;
            case "water_fragment": item = recipeManager.createFragmentItem(FragmentType.WATER); break;
            case "air_fragment": item = recipeManager.createFragmentItem(FragmentType.AIR); break;
            case "dark_fragment": item = recipeManager.createFragmentItem(FragmentType.DARK); break;
            case "light_fragment": item = recipeManager.createFragmentItem(FragmentType.LIGHT); break;
            case "void_fragment": item = recipeManager.createFragmentItem(FragmentType.VOID); break;
            case "dragon_fragment": item = recipeManager.createFragmentItem(FragmentType.DRAGON); break;
            case "storm_fragment": item = recipeManager.createFragmentItem(FragmentType.STORM); break;
            case "time_fragment": item = recipeManager.createFragmentItem(FragmentType.TIME); break;
            case "luck_fragment": item = recipeManager.createFragmentItem(FragmentType.LUCK); break;
            
            // Other items
            case "changer": item = createFragmentChangerItem(); break;
            case "manaflask": item = createManaFlaskItem(); break;
            default:
                if (giver != null) giver.sendMessage("§cUnknown item: " + itemName);
                else target.sendMessage("§cUnknown item: " + itemName);
                return;
        }
        
        target.getInventory().addItem(item);
        if (giver != null) {
            giver.sendMessage("§aGiven " + itemName + " to " + target.getName());
            target.sendMessage("§aReceived " + itemName + " from " + giver.getName());
        } else {
            target.sendMessage("§aGiven " + itemName);
        }
    }

    private void handleSet(Player player, String[] args) {
        // Usage: /fragment set <player> <charlevel|rank|level> <fragment|value>
        if (!player.hasPermission("fragment.admin")) {
            player.sendMessage("§cYou don't have permission to use this command");
            return;
        }
        
        if (args.length < 4) {
            player.sendMessage("§cUsage: /fragment set <player> <charlevel|rank|level> <fragment|value>");
            return;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found: " + args[1]);
            return;
        }
        
        String stat = args[2].toLowerCase();
        
        if (stat.equals("charlevel")) {
            // Character level: /fragment set <player> charlevel <value>
            int value;
            try {
                value = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid character level: " + args[3]);
                return;
            }
            
            if (characterLevelManager != null) {
                characterLevelManager.setCharacterLevel(target, value);
                double newMaxMana = characterLevelManager.getMaxMana(target);
                player.sendMessage("§aSet character level for " + target.getName() + " to " + value);
                player.sendMessage("§7New max mana: §b" + String.format("%.0f", newMaxMana));
                target.sendMessage("§6Your character level has been set to " + value + "!");
                target.sendMessage("§7Max mana is now: §b" + String.format("%.0f", newMaxMana));
            } else {
                player.sendMessage("§cCharacter level manager not available");
            }
        } else if (stat.equals("level") || stat.equals("rank")) {
            // Fragment level/rank: /fragment set <player> <level|rank> <fragment> <value>
            if (args.length < 5) {
                player.sendMessage("§cUsage: /fragment set <player> " + stat + " <fragment> <value>");
                return;
            }
            
            FragmentType type;
            try {
                type = FragmentType.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid fragment type: " + args[3]);
                return;
            }
            
            int value;
            try {
                value = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid value: " + args[4]);
                return;
            }
            
            if (stat.equals("level")) {
                levelManager.setLevel(target, type, value);
                player.sendMessage("§aSet " + type.getDisplayName() + " level for " + target.getName() + " to " + value);
            } else if (stat.equals("rank")) {
                rankManager.setRank(target, type, value);
                player.sendMessage("§aSet " + type.getDisplayName() + " rank for " + target.getName() + " to " + value);
            }
        } else {
            player.sendMessage("§cUnknown stat: " + stat + " (use charlevel, level, or rank)");
        }
    }

    private void handleReset(Player player, String[] args) {
        // Usage: /fragment reset <player> [fragment]
        if (!player.hasPermission("fragment.admin")) {
            player.sendMessage("§cYou don't have permission to use this command");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /fragment reset <player> [fragment]");
            return;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found: " + args[1]);
            return;
        }
        
        if (args.length >= 3) {
            // Reset specific fragment
            try {
                FragmentType type = FragmentType.valueOf(args[2].toUpperCase());
                
                // Clean up active abilities for this fragment
                cleanupFragmentAbilities(target, type);
                
                levelManager.setLevel(target, type, 1);
                levelManager.setXP(target, type, 0);
                rankManager.initializeRank(target, type);
                player.sendMessage("§a✓ Reset " + type.getDisplayName() + " for " + target.getName());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid fragment type: " + args[2]);
            }
        } else {
            // Reset all fragments
            // Clean up ALL active abilities first
            cleanupAllAbilities(target);
            
            for (FragmentType type : FragmentType.values()) {
                levelManager.setLevel(target, type, 1);
                levelManager.setXP(target, type, 0);
                rankManager.initializeRank(target, type);
            }
            player.sendMessage("§a✓ Reset all fragments for " + target.getName());
        }
    }
    
    /**
     * Clean up active abilities for a specific fragment
     */
    private void cleanupFragmentAbilities(Player player, FragmentType type) {
        // End flight if active for Dragon or Air fragments
        if (type == FragmentType.DRAGON || type == FragmentType.AIR) {
            FrostSMPPlugin frostPlugin = (FrostSMPPlugin) plugin;
            frostPlugin.getFlightManager().endFlight(player, false);
        }
        
        // Remove any active potion effects from fragment abilities
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS);
        
        // Ensure flight is disabled
        if (!player.getGameMode().equals(org.bukkit.GameMode.CREATIVE) && 
            !player.getGameMode().equals(org.bukkit.GameMode.SPECTATOR)) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }
    
    /**
     * Clean up ALL active abilities for a player
     */
    private void cleanupAllAbilities(Player player) {
        // End flight unconditionally
        FrostSMPPlugin frostPlugin = (FrostSMPPlugin) plugin;
        frostPlugin.getFlightManager().endFlight(player, false);
        
        // Remove all potion effects
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // Ensure flight is disabled
        if (!player.getGameMode().equals(org.bukkit.GameMode.CREATIVE) && 
            !player.getGameMode().equals(org.bukkit.GameMode.SPECTATOR)) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        
        // Extinguish fire
        player.setFireTicks(0);
    }
    /**
     * Show compact, beautiful Fragment info (FIXED VERSION)
     */
    private void showFragmentInfo(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            player.sendMessage("§c✗ No Fragment active");
            player.sendMessage("§7Use §e/fragment list §7to activate one");
            return;
        }
        
        // Get stats
        int rank = rankManager.getRank(player, activeFragment);
        int maxRank = rankManager.getMaxRank(activeFragment);
        int level = levelManager.getLevel(player, activeFragment);
        int maxLevel = levelManager.getMaxLevel(activeFragment);
        double xp = levelManager.getXP(player, activeFragment);
        double xpRequired = levelManager.getXPForNextLevel(player, activeFragment);
        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        
        // Calculate progress bars (simple version)
        double xpPercent = xpRequired > 0 ? (xp / xpRequired) : 1.0;
        double manaPercent = maxMana > 0 ? (currentMana / maxMana) : 1.0;
        
        // Build simple progress bars
        int xpFilled = (int) (xpPercent * 10);
        int manaFilled = (int) (manaPercent * 10);
        String xpBar = "§a" + "█".repeat(xpFilled) + "§8" + "░".repeat(10 - xpFilled);
        String manaBar = "§b" + "█".repeat(manaFilled) + "§8" + "░".repeat(10 - manaFilled);
        
        // Get fragment color
        String fragColor = "§c"; // Default to red, will be overridden
        switch (activeFragment) {
            case FIRE: fragColor = "§c"; break;
            case WATER: fragColor = "§b"; break;
            case AIR: fragColor = "§f"; break;
            case DARK: fragColor = "§8"; break;
            case LIGHT: fragColor = "§e"; break;
            case VOID: fragColor = "§5"; break;
            case STORM: fragColor = "§9"; break;
            case DRAGON: fragColor = "§6"; break;
            case TIME: fragColor = "§d"; break;
            case LUCK: fragColor = "§a"; break;
        }
        
        // Rank badge
        String rankBadge = rank >= maxRank ? "§6★" : "§7◆";
        
        // Compact display (6 lines total) - Fixed box borders to not overflow
        player.sendMessage("");
        player.sendMessage("§8╔═════════════════════════════════╗");
        player.sendMessage("§8║  " + fragColor + "§l" + activeFragment.getDisplayName().toUpperCase() + " FRAGMENT §r§8║");
        player.sendMessage("§8╚═════════════════════════════════╝");
        
        // Rank & Level (single line)
        player.sendMessage("§7Rank: " + rankBadge + " §f" + rank + "§8/§7" + maxRank + 
                         " §8│ §7Level: §e" + level + "§8/§7" + maxLevel);
        
        // XP Progress (compact)
        if (level < maxLevel) {
            player.sendMessage("§7XP: " + xpBar + " §e" + String.format("%.0f", xp) + "§8/§e" + String.format("%.0f", xpRequired));
        } else {
            player.sendMessage("§7XP: §6§l✦ MAX LEVEL ✦");
        }
        
        // Mana (compact)
        player.sendMessage("§7Mana: " + manaBar + " §b" + String.format("%.0f", currentMana) + "§8/§b" + String.format("%.0f", maxMana));
        
        // Abilities count (single line)
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(activeFragment);
        if (fragment != null) {
            int unlockedCount = 0;
            int totalCount = fragment.getAbilities().size();
            
            for (com.muzlik.fragment.ability.AbilityDefinition ability : fragment.getAbilities()) {
                boolean isUnlocked = rank >= ability.getRankRequirement() && level >= ability.getLevelRequirement();
                if (isUnlocked) unlockedCount++;
            }
            
            String abilityStatus = unlockedCount == totalCount ? "§a" + unlockedCount + "§8/§a" + totalCount + " §7✓" : 
                                  "§e" + unlockedCount + "§8/§7" + totalCount;
            player.sendMessage("§7Abilities: " + abilityStatus + " §8│ §7Use §e/fragment abilities §7for details");
        }
        
        player.sendMessage("");
    }
    
    /**
     * Show compact abilities list (FIXED VERSION)
     */
    private void showAbilitiesList(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            player.sendMessage("§c✗ No Fragment active");
            player.sendMessage("§7Use §e/fragment list §7to activate one");
            return;
        }
        
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(activeFragment);
        if (fragment == null) {
            player.sendMessage("§c✗ Fragment data not found");
            return;
        }
        
        int rank = rankManager.getRank(player, activeFragment);
        int level = levelManager.getLevel(player, activeFragment);
        
        // Get fragment color
        String fragColor = "§c";
        switch (activeFragment) {
            case FIRE: fragColor = "§c"; break;
            case WATER: fragColor = "§b"; break;
            case AIR: fragColor = "§f"; break;
            case DARK: fragColor = "§8"; break;
            case LIGHT: fragColor = "§e"; break;
            case VOID: fragColor = "§5"; break;
            case STORM: fragColor = "§9"; break;
            case DRAGON: fragColor = "§6"; break;
            case TIME: fragColor = "§d"; break;
            case LUCK: fragColor = "§a"; break;
        }
        
        // Compact header - Fixed box borders to not overflow
        player.sendMessage("");
        player.sendMessage("§8╔═════════════════════════════════╗");
        player.sendMessage("§8║  " + fragColor + "§l" + activeFragment.getDisplayName().toUpperCase() + " ABILITIES §r§8║");
        player.sendMessage("§8╚═════════════════════════════════╝");
        
        // List abilities compactly
        for (com.muzlik.fragment.ability.AbilityDefinition ability : fragment.getAbilities()) {
            boolean isUnlocked = rank >= ability.getRankRequirement() && level >= ability.getLevelRequirement();
            boolean isOnCooldown = cooldownManager.isOnCooldown(player, ability.getId());
            
            if (isUnlocked) {
                String statusSymbol = isOnCooldown ? "§e⏳" : "§a✓";
                player.sendMessage(statusSymbol + " §f" + ability.getDisplayName() + " §8│ §7Slot " + ability.getSlot().getSlotIndex());
                
                // Compact info line
                String cooldownInfo = isOnCooldown ? 
                    " §c(" + String.format("%.1f", cooldownManager.getRemainingCooldownSeconds(player, ability.getId())) + "s)" : "";
                player.sendMessage("  §7Mana: §b" + String.format("%.0f", ability.getManaCost()) + 
                                 " §8│ §7CD: §e" + (ability.getCooldown() / 1000.0) + "s" + cooldownInfo);
            } else {
                player.sendMessage("§c✗ §7" + ability.getDisplayName() + " §8│ §cRank " + ability.getRankRequirement() + " required");
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§7Use §eSneak + Right/Left Click §7with hotbar slots 0-4");
        player.sendMessage("");
    }
    
    /**
     * Show formatted Fragment list with pre-GUI message
     */
    private void showFragmentList(Player player) {
        Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(player);
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        // CLEAN MINIMAL HEADER
        player.sendMessage("");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("  §f§l" + player.getName().toUpperCase() + "'S FRAGMENTS");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("");
        
        if (ownedFragments.isEmpty()) {
            player.sendMessage("  §7No fragments yet");
            player.sendMessage("  §8Complete a ritual to obtain one");
            player.sendMessage("");
        } else {
            for (FragmentType type : ownedFragments) {
                boolean isActive = type.equals(activeFragment);
                int rank = rankManager.getRank(player, type);
                int level = levelManager.getLevel(player, type);
                
                if (isActive) {
                    player.sendMessage("  §f> " + type.getDisplayName() + " §a[ACTIVE]");
                    player.sendMessage("    §8Rank " + rank + " | Level " + level);
                } else {
                    player.sendMessage("  §8" + type.getDisplayName());
                    player.sendMessage("    §8Rank " + rank + " | Level " + level);
                }
            }
            
            if (activeFragment != null) {
                player.sendMessage("");
                double currentMana = manaManager.getMana(player);
                double maxMana = manaManager.getMaxMana(player);
                player.sendMessage("  §7Mana: §f" + String.format("%.0f", currentMana) + "§8/§f" + String.format("%.0f", maxMana));
            }
        }
        
        player.sendMessage("");
        player.sendMessage("  §8Opening GUI...");
        player.sendMessage("");
        
        // Open GUI after message
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            uiManager.openFragmentOverview(player);
        }, 20L); // 1 second delay for smooth transition
    }
    /**
     * Generate resource pack template (admin command)
     */
    private void generateResourcePack(Player player) {
        player.sendMessage("§a§l✓ Generating resource pack template...");
        
        try {
            com.muzlik.texture.ResourcePackGenerator generator = 
                    new com.muzlik.texture.ResourcePackGenerator(plugin);
            generator.generateResourcePack();
            generator.generateExampleModels(new java.io.File(plugin.getDataFolder(), "resourcepack"));
            
            player.sendMessage("§a§l✓ Resource pack template generated!");
            player.sendMessage("§7Location: §e" + plugin.getDataFolder().getAbsolutePath() + "/resourcepack");
            player.sendMessage("§7Check TEXTURE_MAPPING.md for texture IDs and instructions");
            player.sendMessage("");
            player.sendMessage("§7Next steps:");
            player.sendMessage("§7  1. Add your 16x16 PNG textures to the folders");
            player.sendMessage("§7  2. Zip the 'resourcepack' folder");
            player.sendMessage("§7  3. Distribute to players or host on server");
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Failed to generate resource pack: " + e.getMessage());
            plugin.getLogger().severe("Resource pack generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle controls command - Change control scheme or open GUI
     */
    private void handleControls(Player player, String[] args) {
        if (plugin instanceof FrostSMPPlugin) {
            FrostSMPPlugin frostPlugin = (FrostSMPPlugin) plugin;
            com.muzlik.player.PlayerPreferencesManager prefsManager = frostPlugin.getPreferencesManager();
            
            if (prefsManager == null) {
                player.sendMessage("§cPreferences system not initialized");
                return;
            }
            
            // If no arguments, open GUI
            if (args.length == 1) {
                uiManager.openControlSchemeGUI(player);
                return;
            }
            
            // Handle text commands
            String schemeArg = args[1].toUpperCase().replace(" ", "_");
            
            if (schemeArg.equals("NEXT")) {
                // Cycle to next scheme
                com.muzlik.player.ControlScheme current = prefsManager.getControlScheme(player);
                com.muzlik.player.ControlScheme next = current.next();
                prefsManager.setControlScheme(player, next);
                return;
            }
            
            if (schemeArg.equals("PREV") || schemeArg.equals("PREVIOUS")) {
                // Cycle to previous scheme
                com.muzlik.player.ControlScheme current = prefsManager.getControlScheme(player);
                com.muzlik.player.ControlScheme prev = current.previous();
                prefsManager.setControlScheme(player, prev);
                return;
            }
            
            // Try to match scheme name
            try {
                com.muzlik.player.ControlScheme scheme = com.muzlik.player.ControlScheme.valueOf(schemeArg);
                prefsManager.setControlScheme(player, scheme);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cUnknown control scheme: §e" + args[1]);
                player.sendMessage("§7Available: sneak_click, double_sneak, swap_hands, click_only");
                player.sendMessage("§7Or use §e/fragment controls §7to open GUI");
            }
        }
    }
    
    /**
     * Handle toggle command - Enable/disable abilities
     */
    private void handleToggle(Player player) {
        if (plugin instanceof FrostSMPPlugin) {
            FrostSMPPlugin frostPlugin = (FrostSMPPlugin) plugin;
            com.muzlik.player.PlayerPreferencesManager prefsManager = frostPlugin.getPreferencesManager();
            
            if (prefsManager == null) {
                player.sendMessage("§cPreferences system not initialized");
                return;
            }
            
            prefsManager.toggleAbilities(player);
        }
    }
    
    /**
     * Handle withdraw command - Deactivate fragment and give item to player
     */
    private void handleWithdraw(Player player) {
        com.muzlik.fragment.PlayerFragmentData data = fragmentManager.getPlayerData(player);
        
        if (data == null) {
            player.sendMessage("§cNo fragment data found!");
            return;
        }
        
        FragmentType activeFragment = data.getActiveFragment();
        
        if (activeFragment == null) {
            player.sendMessage("§cYou don't have an active fragment!");
            return;
        }
        
        // SECURITY: Remove fragment from owned list before giving the item
        data.removeFragment(activeFragment);

        // Deactivate the fragment
        fragmentManager.setActiveFragment(player, null);
        
        // Create fragment item
        ItemStack fragmentItem = fragmentManager.createFragmentItem(activeFragment);
        
        if (fragmentItem == null) {
            player.sendMessage("§cFailed to create fragment item!");
            return;
        }
        
        // Give item to player
        player.getInventory().addItem(fragmentItem);
        
        player.sendMessage("§a✓ Fragment withdrawn!");
        player.sendMessage("§7Your §b" + activeFragment.getDisplayName() + " Fragment §7has been deactivated and added to your inventory.");
        player.sendMessage("§7Right-click it to activate again.");
    }
    
    /**
     * Handle debug commands
     */
    private void handleDebug(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c✗ Usage: /fragment debug <player|slots|rituals|health>");
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "player":
                if (args.length < 3) {
                    debugPlayer(player, player);
                } else {
                    Player target = plugin.getServer().getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage("§c✗ Player not found: " + args[2]);
                        return;
                    }
                    debugPlayer(player, target);
                }
                break;
            case "slots":
                if (args.length < 3) {
                    debugSlots(player, player);
                } else {
                    Player target = plugin.getServer().getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage("§c✗ Player not found: " + args[2]);
                        return;
                    }
                    debugSlots(player, target);
                }
                break;
            case "rituals":
                debugRituals(player);
                break;
            case "health":
                debugHealth(player);
                break;
            default:
                player.sendMessage("§c✗ Unknown debug command: " + subCommand);
                player.sendMessage("§7Available: player, slots, rituals, health");
                break;
        }
    }
    /**
     * Debug player info
     */
    private void debugPlayer(Player admin, Player target) {
        admin.sendMessage("");
        admin.sendMessage("§8§m                                        ");
        admin.sendMessage("  §f§lDEBUG: " + target.getName());
        admin.sendMessage("§8§m                                        ");
        admin.sendMessage("");
        
        // Active Fragment
        FragmentType activeFragment = fragmentManager.getActiveFragment(target);
        if (activeFragment != null) {
            int rank = rankManager.getRank(target, activeFragment);
            int level = levelManager.getLevel(target, activeFragment);
            double xp = levelManager.getXP(target, activeFragment);
            
            admin.sendMessage("  §7Active Fragment: §b" + activeFragment.getDisplayName());
            admin.sendMessage("  §7Rank: §6" + rank + " §8| §7Level: §e" + level + " §8| §7XP: §e" + String.format("%.0f", xp));
        } else {
            admin.sendMessage("  §7Active Fragment: §cNone");
        }
        
        admin.sendMessage("");
        
        // All Fragments
        admin.sendMessage("  §7All Fragments:");
        java.util.Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(target);
        if (ownedFragments.isEmpty()) {
            admin.sendMessage("    §8No fragments owned");
        } else {
            for (FragmentType type : ownedFragments) {
                int rank = rankManager.getRank(target, type);
                int level = levelManager.getLevel(target, type);
                admin.sendMessage("    §b" + type.getDisplayName() + " §8- §7R" + rank + " L" + level);
            }
        }
        
        admin.sendMessage("");
        
        // Mana
        double currentMana = manaManager.getMana(target);
        double maxMana = manaManager.getMaxMana(target);
        admin.sendMessage("  §7Mana: §b" + String.format("%.0f", currentMana) + " §8/ §b" + String.format("%.0f", maxMana));
        
        // Character Level
        if (characterLevelManager != null) {
            int charLevel = characterLevelManager.getCharacterLevel(target);
            admin.sendMessage("  §7Character Level: §e" + charLevel);
        }
        
        admin.sendMessage("");
    }
    
    /**
     * Debug ability slots
     */
    private void debugSlots(Player admin, Player target) {
        if (plugin instanceof FrostSMPPlugin) {
            com.muzlik.fragment.ability.AbilitySlotManager slotManager = 
                ((FrostSMPPlugin) plugin).getAbilitySlotManager();
            
            admin.sendMessage("");
            admin.sendMessage("§8§m                                        ");
            admin.sendMessage("  §f§lDEBUG SLOTS: " + target.getName());
            admin.sendMessage("§8§m                                        ");
            admin.sendMessage("");
            
            java.util.Collection<FragmentType> ownedFragments = fragmentManager.getPlayerFragments(target);
            if (ownedFragments.isEmpty()) {
                admin.sendMessage("  §8No fragments owned");
            } else {
                for (FragmentType type : ownedFragments) {
                    java.util.Set<Integer> unlockedSlots = slotManager.getUnlockedSlots(target, type);
                    admin.sendMessage("  §b" + type.getDisplayName() + ":");
                    admin.sendMessage("    §7Unlocked Slots: §e" + unlockedSlots.toString());
                }
            }
            
            admin.sendMessage("");
        } else {
            admin.sendMessage("§c✗ Ability slot manager not available");
        }
    }
    
    /**
     * Debug active rituals
     */
    private void debugRituals(Player admin) {
        if (plugin instanceof FrostSMPPlugin) {
            com.muzlik.ritual.RitualManager ritualManager = 
                ((FrostSMPPlugin) plugin).getRitualManager();
            
            admin.sendMessage("");
            admin.sendMessage("§8§m                                        ");
            admin.sendMessage("  §f§lDEBUG: ACTIVE RITUALS");
            admin.sendMessage("§8§m                                        ");
            admin.sendMessage("");
            
            int ritualCount = 0;
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (ritualManager.hasActiveRitual(player)) {
                    ritualCount++;
                    com.muzlik.ritual.RitualInstance ritual = ritualManager.getActiveRitual(player);
                    
                    org.bukkit.Location loc = ritual.getLocation();
                    int progress = ritual.getProgressPercent();
                    long remainingSeconds = ritual.getRemainingSeconds();
                    
                    admin.sendMessage("  §b" + player.getName() + " §8- §7" + ritual.getType().getDisplayName());
                    admin.sendMessage("    §7Fragment: §b" + (ritual.getFragmentType() != null ? ritual.getFragmentType().getDisplayName() : "None"));
                    admin.sendMessage("    §7Progress: §e" + progress + "% §8| §7Time: §e" + remainingSeconds + "s");
                    admin.sendMessage("    §7Location: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                    admin.sendMessage("");
                }
            }
            
            if (ritualCount == 0) {
                admin.sendMessage("  §8No active rituals");
                admin.sendMessage("");
            }
        } else {
            admin.sendMessage("§c✗ Ritual manager not available");
        }
    }
    
    /**
     * Debug system health
     */
    private void debugHealth(Player admin) {
        admin.sendMessage("");
        admin.sendMessage("§8§m                                        ");
        admin.sendMessage("  §f§lDEBUG: SYSTEM HEALTH");
        admin.sendMessage("§8§m                                        ");
        admin.sendMessage("");
        
        if (plugin instanceof FrostSMPPlugin) {
            com.muzlik.monitoring.HealthMonitor healthMonitor = 
                ((FrostSMPPlugin) plugin).getHealthMonitor();
            
            if (healthMonitor != null) {
                com.muzlik.monitoring.HealthMonitor.HealthMetrics metrics = healthMonitor.getMetrics();
                
                // Display metrics with color coding
                admin.sendMessage("  §7Active Rituals: " + getMetricColor(metrics.activeRituals, 50) + metrics.activeRituals + " §8/ §750");
                admin.sendMessage("  §7Active VFX Effects: " + getMetricColor(metrics.activeVFXEffects, 500) + metrics.activeVFXEffects + " §8/ §7500");
                admin.sendMessage("  §7Queued Tasks: " + getMetricColor(metrics.queuedTasks, 100) + metrics.queuedTasks + " §8/ §7100");
                admin.sendMessage("  §7Online Players: §e" + plugin.getServer().getOnlinePlayers().size());
                
                admin.sendMessage("");
                
                // Display warnings if any
                if (healthMonitor.hasWarnings()) {
                    admin.sendMessage("  §c§lWARNINGS:");
                    String warnings = healthMonitor.getWarningMessages();
                    for (String warning : warnings.split("\n")) {
                        admin.sendMessage("  " + warning);
                    }
                } else {
                    admin.sendMessage("  §a✓ All systems healthy");
                }
            } else {
                admin.sendMessage("  §c✗ Health monitor not initialized");
            }
        } else {
            admin.sendMessage("  §c✗ Plugin not initialized properly");
        }
        
        admin.sendMessage("");
    }
    
    /**
     * Get color code for metric based on threshold
     */
    private String getMetricColor(int value, int threshold) {
        double percent = (double) value / threshold;
        if (percent >= 1.0) {
            return "§c"; // Red - over threshold
        } else if (percent >= 0.75) {
            return "§e"; // Yellow - warning
        } else {
            return "§a"; // Green - healthy
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("  §f§lFRAGMENT COMMANDS");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("");
        player.sendMessage("  §f/fragment gui §8- Open GUI");
        player.sendMessage("  §f/fragment list §8- View fragments");
        player.sendMessage("  §f/fragment info §8- Fragment stats");
        player.sendMessage("  §f/fragment level §8- Character level");
        player.sendMessage("  §f/fragment abilities §8- List abilities");
        player.sendMessage("  §f/fragment recipes §8- View recipes");
        player.sendMessage("  §f/fragment withdraw §8- Deactivate & get item");
        player.sendMessage("  §f/fragment controls §8- Change controls");
        player.sendMessage("  §f/fragment toggle §8- Enable/disable abilities");
        player.sendMessage("");
        player.sendMessage("  §8Use Fragment Changer ritual to switch fragments");
        player.sendMessage("");
        if (player.hasPermission("fragment.admin")) {
            player.sendMessage("  §8Admin: §7/fragment give/grant/set/reset/forceactivate/reload/debug");
            player.sendMessage("  §8Debug: §7/fragment debug <player|slots|rituals|health>");
            player.sendMessage("  §8Fragments: §7fire, water, air, dark, light, void, dragon, storm, time, luck");
            player.sendMessage("  §8Items: §7changer, manaflask");
        }
        player.sendMessage("");
    }
    
    /**
     * Show character level info
     */
    private void showCharacterLevel(Player player) {
        if (characterLevelManager == null) {
            player.sendMessage("§cCharacter level system not initialized");
            return;
        }
        
        int charLevel = characterLevelManager.getCharacterLevel(player);
        int maxLevel = characterLevelManager.getMaxCharacterLevel();
        double charXP = characterLevelManager.getCharacterXP(player);
        double xpForNext = characterLevelManager.getXPForNextLevel(player);
        double maxMana = characterLevelManager.getMaxMana(player);
        
        // Calculate XP progress
        double xpPercent = xpForNext > 0 ? (charXP / xpForNext) : 1.0;
        int xpFilled = (int) (xpPercent * 10);
        String xpBar = "§a" + "█".repeat(xpFilled) + "§8" + "░".repeat(10 - xpFilled);
        
        player.sendMessage("");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("  §f§l✦ CHARACTER LEVEL");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("");
        
        // Level display
        if (charLevel >= maxLevel) {
            player.sendMessage("  §6§lLevel: " + charLevel + " §e★ MAX LEVEL");
        } else {
            player.sendMessage("  §fLevel: §b" + charLevel + " §8/ §7" + maxLevel);
        }
        
        // XP display
        if (charLevel < maxLevel) {
            player.sendMessage("");
            player.sendMessage("  §7XP: " + xpBar);
            player.sendMessage("  §e" + String.format("%.0f", charXP) + " §8/ §e" + String.format("%.0f", xpForNext));
        }
        
        // Max Mana display
        player.sendMessage("");
        player.sendMessage("  §7Max Mana: §b⚡" + String.format("%.0f", maxMana));
        player.sendMessage("  §8(Based on character level)");
        
        player.sendMessage("");
        player.sendMessage("§8§m                                        ");
    }
    private void grantFragment(Player player, String typeName) {
        try {
            FragmentType type = FragmentType.valueOf(typeName.toUpperCase());
            fragmentManager.grantFragment(player, type);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid Fragment type: " + typeName);
        }
    }

    private ItemStack createFragmentCreationItem(FragmentType type) {
        // Use texture system for Fragment creation items
        return com.muzlik.texture.TextureItemBuilder.createFragmentIcon(
                type,
                "§b§l" + type.getDisplayName() + " Fragment Creation",
                Arrays.asList(
                        "§7Place this item to start ritual",
                        "§7Duration: 10-15 minutes",
                        "§7Grants: §b" + type.getDisplayName() + " Fragment",
                        "§7Stay within 5 blocks!",
                        "",
                        "§e§lFRAGMENT CREATION RITUAL"
                ),
                true // Add glow effect
        );
    }

    private ItemStack createFragmentChangerItem() {
        // Use texture system for Fragment changer
        return com.muzlik.texture.TextureItemBuilder.createUIElement(
                "ui_fragment_changer",
                "§d§lFragment Changer",
                Arrays.asList(
                        "§7Place this item to start ritual",
                        "§7Duration: 5 minutes",
                        "§7Allows switching active Fragment",
                        "§7Stay within 5 blocks!",
                        "",
                        "§e§lFRAGMENT CHANGER RITUAL"
                )
        );
    }

    private ItemStack createManaFlaskItem() {
        // Use texture system for mana flask
        return com.muzlik.texture.TextureItemBuilder.createManaFlask();
    }

    private void forceActivateFragment(Player admin, String targetName, String typeName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            admin.sendMessage("§c✗ Player not found: " + targetName);
            return;
        }

        try {
            FragmentType type = FragmentType.valueOf(typeName.toUpperCase());
            
            // Grant the Fragment if they don't have it
            if (!fragmentManager.hasFragment(target, type)) {
                fragmentManager.grantFragment(target, type);
                admin.sendMessage("§a✓ Granted " + type.getDisplayName() + " Fragment to " + target.getName());
            }
            
            // Force activate (bypass cooldown and checks)
            fragmentManager.setActiveFragment(target, type);
            
            admin.sendMessage("§a✓ Force-activated " + type.getDisplayName() + " Fragment for " + target.getName());
            target.sendMessage("§b⚡ Your Fragment has been set to " + type.getDisplayName() + " by an administrator");
            
        } catch (IllegalArgumentException e) {
            admin.sendMessage("§cInvalid Fragment type: " + typeName);
            admin.sendMessage("§7Available: FIRE, WATER, AIR, DARK, LIGHT, VOID, DRAGON, STORM, TIME, LUCK");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Player commands
            completions.addAll(Arrays.asList("gui", "list", "info", "level", "abilities", "recipes", "controls", "toggle", "withdraw"));
            
            // Admin commands (only show to admins)
            if (sender.hasPermission("fragment.admin")) {
                completions.addAll(Arrays.asList("give", "grant", "set", "reset", "forceactivate", "reload", "generatepack", "debug"));
            }
        } else if (args.length == 2) {
            if (sender.hasPermission("fragment.admin") && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("forceactivate"))) {
                // Add online player names (admin only)
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("grant")) {
                // Admin command - show only existing fragments
                completions.addAll(Arrays.asList("FIRE", "WATER", "AIR", "DARK", "LIGHT", "VOID", "DRAGON", "STORM", "TIME", "LUCK"));
            } else if (args[0].equalsIgnoreCase("controls") || args[0].equalsIgnoreCase("control")) {
                completions.addAll(Arrays.asList("sneak_click", "double_sneak", "swap_hands", "click_only", "next", "prev"));
            } else if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("debug")) {
                completions.addAll(Arrays.asList("player", "slots", "rituals", "health"));
            }
        } else if (args.length == 3) {
            if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("give")) {
                completions.addAll(Arrays.asList("fire", "water", "air", "dark", "light", "void", "dragon", "storm", "time", "luck", "changer", "manaflask"));
            } else if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("charlevel", "level", "rank"));
            } else if (sender.hasPermission("fragment.admin") && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("forceactivate"))) {
                completions.addAll(Arrays.asList("FIRE", "WATER", "AIR", "DARK", "LIGHT", "VOID", "DRAGON", "STORM", "TIME", "LUCK"));
            } else if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("debug") && !args[1].equalsIgnoreCase("rituals") && !args[1].equalsIgnoreCase("health")) {
                // Add player names for debug player/slots commands
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 4) {
            if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("set") && (args[2].equalsIgnoreCase("level") || args[2].equalsIgnoreCase("rank"))) {
                completions.addAll(Arrays.asList("FIRE", "WATER", "AIR", "DARK", "LIGHT", "VOID", "DRAGON", "STORM", "TIME", "LUCK"));
            } else if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("set") && args[2].equalsIgnoreCase("charlevel")) {
                completions.addAll(Arrays.asList("1", "5", "10", "25", "50"));
            }
        } else if (args.length == 5) {
            if (sender.hasPermission("fragment.admin") && args[0].equalsIgnoreCase("set")) {
                if (args[2].equalsIgnoreCase("level")) {
                    completions.addAll(Arrays.asList("1", "5", "10", "25", "50"));
                } else if (args[2].equalsIgnoreCase("rank")) {
                    completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"));
                }
            }
        }

        return completions;
    }
}