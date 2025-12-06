package com.muzlik.command;

import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.level.LevelManager;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.mana.ManaManager;
import com.muzlik.recipe.RecipeManager;
import com.muzlik.ui.UIManager;
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
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /fragment give <item>");
                    return true;
                }
                giveItem(player, args[1]);
                break;

            case "list":
                showFragmentList(player);
                break;
            
            case "gui":
                // Open GUI directly without text
                uiManager.openFragmentOverview(player);
                break;

            case "mana":
                uiManager.openManaStatus(player);
                break;
            
            case "info":
                showFragmentInfo(player);
                break;
            
            case "abilities":
                showAbilitiesList(player);
                break;

            case "grant":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /fragment grant <type>");
                    return true;
                }
                grantFragment(player, args[1]);
                break;

            case "activate":
            case "select":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /fragment activate <type>");
                    return true;
                }
                activateFragment(player, args[1]);
                break;

            case "forceactivate":
            case "setactive":
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
            case "resourcepack":
                if (!player.hasPermission("fragment.admin")) {
                    player.sendMessage("§cYou don't have permission to use this command");
                    return true;
                }
                generateResourcePack(player);
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    /**
     * Show detailed Fragment info (Task 7.1, 7.2)
     */
    private void showFragmentInfo(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            player.sendMessage("§cœ— No Fragment active");
            player.sendMessage("§7Use §e/fragment list §7to view and activate a Fragment");
            return;
        }
        
        // Get stats
        int rank = rankManager.getRank(player, activeFragment);
        int maxRank = rankManager.getMaxRank(activeFragment);
        int level = levelManager.getLevel(player, activeFragment);
        double xp = levelManager.getXP(player, activeFragment);
        double xpRequired = levelManager.getXPForNextLevel(player, activeFragment);
        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        double regenRate = manaManager.getManaRegenRate(player);
        
        // Calculate XP progress bar
        double xpPercent = xpRequired > 0 ? (xp / xpRequired) : 1.0;
        String xpBar = buildProgressBar(xpPercent, 20);
        
        // Calculate mana progress bar
        double manaPercent = maxMana > 0 ? (currentMana / maxMana) : 1.0;
        String manaBar = buildProgressBar(manaPercent, 20);
        
        // Header
        player.sendMessage("§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬");
        player.sendMessage("§e§l              " + activeFragment.getDisplayName().toUpperCase() + " FRAGMENT");
        player.sendMessage("§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬");
        player.sendMessage("");
        
        // Rank display
        String rankBadge = getRankBadge(rank, maxRank);
        player.sendMessage("§7Rank: " + rankBadge + " §6§l" + rank + " §7/ §6" + maxRank);
        if (rank < maxRank) {
            player.sendMessage("§7  §8†’ §7Next rank unlocks more power!");
        } else {
            player.sendMessage("§7  §6§l˜… MASTERY ACHIEVED ˜…");
        }
        player.sendMessage("");
        
        // Level and XP display
        player.sendMessage("§7Level: §e§l" + level + " §7/ §e50");
        player.sendMessage("§7XP: " + xpBar);
        player.sendMessage("§7  §e" + String.format("%.0f", xp) + " §7/ §e" + String.format("%.0f", xpRequired) + " §8(" + String.format("%.1f", xpPercent * 100) + "%)");
        player.sendMessage("");
        
        // Mana display
        player.sendMessage("§7Mana: §b§l" + String.format("%.0f", currentMana) + " §7/ §b" + String.format("%.0f", maxMana));
        player.sendMessage("§7" + manaBar);
        player.sendMessage("§7  §8Regen: §b+" + String.format("%.1f", regenRate) + " §7per second");
        player.sendMessage("");
        
        // Abilities display
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(activeFragment);
        if (fragment != null) {
            player.sendMessage("§7Abilities:");
            
            int unlockedCount = 0;
            int totalCount = fragment.getAbilities().size();
            
            for (com.muzlik.fragment.ability.AbilityDefinition ability : fragment.getAbilities()) {
                boolean isUnlocked = rank >= ability.getRankRequirement() && level >= ability.getLevelRequirement();
                
                if (isUnlocked) {
                    unlockedCount++;
                    String statusSymbol = "§aœ“";
                    player.sendMessage("  " + statusSymbol + " §b" + ability.getDisplayName() + " §7(" + ability.getSlot().getDisplayName() + ")");
                } else {
                    String statusSymbol = "§cœ—";
                    player.sendMessage("  " + statusSymbol + " §7" + ability.getDisplayName() + " §8(Requires Rank " + ability.getRankRequirement() + ")");
                }
            }
            
            player.sendMessage("");
            player.sendMessage("§7Unlocked: §a" + unlockedCount + " §7/ §e" + totalCount);
        }
        
        player.sendMessage("");
        player.sendMessage("§7Use §e/fragment abilities §7for detailed ability information");
        player.sendMessage("§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬");
    }
    
    /**
     * Show abilities list (Task 8.1, 8.2)
     */
    private void showAbilitiesList(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            player.sendMessage("§cœ— No Fragment active");
            player.sendMessage("§7Use §e/fragment list §7to view and activate a Fragment");
            return;
        }
        
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(activeFragment);
        if (fragment == null) {
            player.sendMessage("§cœ— Fragment data not found");
            return;
        }
        
        int rank = rankManager.getRank(player, activeFragment);
        int level = levelManager.getLevel(player, activeFragment);
        
        // Header
        player.sendMessage("§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬");
        player.sendMessage("§e§l         " + activeFragment.getDisplayName().toUpperCase() + " ABILITIES");
        player.sendMessage("§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬");
        player.sendMessage("");
        
        // List abilities
        for (com.muzlik.fragment.ability.AbilityDefinition ability : fragment.getAbilities()) {
            boolean isUnlocked = rank >= ability.getRankRequirement() && level >= ability.getLevelRequirement();
            boolean isOnCooldown = cooldownManager.isOnCooldown(player, ability.getId());
            
            if (isUnlocked) {
                String statusSymbol = isOnCooldown ? "§e±" : "§aœ“";
                player.sendMessage(statusSymbol + " §b§l" + ability.getDisplayName() + " §7(Slot " + ability.getSlot().getSlotIndex() + ")");
                player.sendMessage("   §7" + ability.getDescription());
                player.sendMessage("   §7Mana: §b" + String.format("%.0f", ability.getManaCost()) + " §8| §7Cooldown: §e" + (ability.getCooldown() / 1000.0) + "s");
                
                if (isOnCooldown) {
                    double remaining = cooldownManager.getRemainingCooldownSeconds(player, ability.getId());
                    player.sendMessage("   §c± On cooldown: §e" + String.format("%.1f", remaining) + "s");
                }
            } else {
                player.sendMessage("§cœ— §7" + ability.getDisplayName() + " §8(Slot " + ability.getSlot().getSlotIndex() + ")");
                player.sendMessage("   §7" + ability.getDescription());
                
                int rankReq = ability.getRankRequirement();
                int levelReq = ability.getLevelRequirement();
                
                if (rank < rankReq) {
                    int ranksAway = rankReq - rank;
                    player.sendMessage("   §cœ— Requires Rank §6" + rankReq + " §7(you are §6" + rank + "§7) - §e" + ranksAway + " ranks away");
                }
                if (level < levelReq) {
                    int levelsAway = levelReq - level;
                    player.sendMessage("   §cœ— Requires Level §e" + levelReq + " §7(you are §e" + level + "§7) - §e" + levelsAway + " levels away");
                }
            }
            
            player.sendMessage("");
        }
        
        player.sendMessage("§7Use §eSneak + Right/Left Click §7while holding hotbar slots 0-4 to use abilities");
        player.sendMessage("§6§l–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬–¬");
    }
    
    /**
     * Build progress bar for display
     */
    private String buildProgressBar(double percent, int length) {
        int filled = (int) (percent * length);
        int empty = length - filled;
        
        StringBuilder bar = new StringBuilder("§7[");
        bar.append("§a").append("–ˆ".repeat(Math.max(0, filled)));
        bar.append("§7").append("–‘".repeat(Math.max(0, empty)));
        bar.append("§7]");
        
        return bar.toString();
    }
    
    /**
     * Get rank badge symbol
     */
    private String getRankBadge(int rank, int maxRank) {
        if (rank >= maxRank) {
            return "§6§l˜…"; // Max rank - Gold star
        } else if (rank >= maxRank - 1) {
            return "§6—†"; // Near max - Gold diamond
        } else if (rank >= (maxRank / 2)) {
            return "§e—†"; // Mid rank - Yellow diamond
        } else {
            return "§7—†"; // Low rank - Gray diamond
        }
    }
    
    /**
     * Show formatted Fragment list with pre-GUI message (Task 6.1)
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
        
        // Open GUI after message (Task 6.2)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            uiManager.openFragmentOverview(player);
        }, 20L); // 1 second delay for smooth transition
    }
    
    /**
     * Generate resource pack template (admin command)
     */
    private void generateResourcePack(Player player) {
        player.sendMessage("§a§lœ“ Generating resource pack template...");
        
        try {
            com.muzlik.texture.ResourcePackGenerator generator = 
                    new com.muzlik.texture.ResourcePackGenerator(plugin);
            generator.generateResourcePack();
            generator.generateExampleModels(new java.io.File(plugin.getDataFolder(), "resourcepack"));
            
            player.sendMessage("§a§lœ“ Resource pack template generated!");
            player.sendMessage("§7Location: §e" + plugin.getDataFolder().getAbsolutePath() + "/resourcepack");
            player.sendMessage("§7Check TEXTURE_MAPPING.md for texture IDs and instructions");
            player.sendMessage("");
            player.sendMessage("§7Next steps:");
            player.sendMessage("§7  1. Add your 16x16 PNG textures to the folders");
            player.sendMessage("§7  2. Zip the 'resourcepack' folder");
            player.sendMessage("§7  3. Distribute to players or host on server");
            
        } catch (Exception e) {
            player.sendMessage("§cœ— Failed to generate resource pack: " + e.getMessage());
            plugin.getLogger().severe("Resource pack generation failed: " + e.getMessage());
            e.printStackTrace();
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
        player.sendMessage("  §f/fragment abilities §8- List abilities");
        player.sendMessage("  §f/fragment activate <type> §8- Switch");
        player.sendMessage("");
        player.sendMessage("  §8Admin: §7/fragment give/grant");
        player.sendMessage("");
    }

    private void giveItem(Player player, String itemName) {
        ItemStack item = null;

        switch (itemName.toLowerCase()) {
            case "fire":
                item = createFragmentCreationItem(FragmentType.FIRE);
                break;
            case "water":
                item = createFragmentCreationItem(FragmentType.WATER);
                break;
            case "air":
                item = createFragmentCreationItem(FragmentType.AIR);
                break;
            case "earth":
                item = createFragmentCreationItem(FragmentType.EARTH);
                break;
            case "dark":
                item = createFragmentCreationItem(FragmentType.DARK);
                break;
            case "light":
                item = createFragmentCreationItem(FragmentType.LIGHT);
                break;
            case "void":
                item = createFragmentCreationItem(FragmentType.VOID);
                break;
            case "mob":
                item = createFragmentCreationItem(FragmentType.MOB);
                break;
            case "dragon":
                item = createFragmentCreationItem(FragmentType.DRAGON);
                break;
            case "storm":
                item = createFragmentCreationItem(FragmentType.STORM);
                break;
            case "changer":
                item = createFragmentChangerItem();
                break;
            case "manaflask":
                item = createManaFlaskItem();
                break;
            default:
                player.sendMessage("§cUnknown item: " + itemName);
                player.sendMessage("§7Available: fire, water, air, earth, dark, light, void, mob, dragon, storm, changer, manaflask");
                return;
        }

        player.getInventory().addItem(item);
        player.sendMessage("§aœ“ Given: §b" + item.getItemMeta().getDisplayName());
    }

    private void grantFragment(Player player, String typeName) {
        try {
            FragmentType type = FragmentType.valueOf(typeName.toUpperCase());
            fragmentManager.grantFragment(player, type);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid Fragment type: " + typeName);
        }
    }

    private void activateFragment(Player player, String typeName) {
        try {
            FragmentType type = FragmentType.valueOf(typeName.toUpperCase());
            
            // Check if player owns this Fragment
            if (!fragmentManager.hasFragment(player, type)) {
                player.sendMessage("§cœ— You don't own this Fragment");
                player.sendMessage("§7Complete a Fragment Creation ritual to obtain it");
                return;
            }
            
            // Check if it's already active
            if (type.equals(fragmentManager.getActiveFragment(player))) {
                player.sendMessage("§eš  This Fragment is already active");
                return;
            }
            
            // Check cooldown
            if (!fragmentManager.canSwitchFragment(player)) {
                long cooldown = fragmentManager.getFragmentSwitchCooldown(player);
                long minutes = cooldown / 60000;
                long seconds = (cooldown % 60000) / 1000;
                player.sendMessage("§cœ— Fragment switch cooldown: " + minutes + "m " + seconds + "s");
                player.sendMessage("§7Complete a Fragment Changer ritual to switch immediately");
                return;
            }
            
            // Activate the Fragment
            fragmentManager.setActiveFragment(player, type);
            fragmentManager.recordFragmentSwitch(player);
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid Fragment type: " + typeName);
            player.sendMessage("§7Available: FIRE, WATER, AIR, EARTH, DARK, LIGHT, VOID, MOB, DRAGON, STORM");
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
            admin.sendMessage("§cœ— Player not found: " + targetName);
            return;
        }

        try {
            FragmentType type = FragmentType.valueOf(typeName.toUpperCase());
            
            // Grant the Fragment if they don't have it
            if (!fragmentManager.hasFragment(target, type)) {
                fragmentManager.grantFragment(target, type);
                admin.sendMessage("§aœ“ Granted " + type.getDisplayName() + " Fragment to " + target.getName());
            }
            
            // Force activate (bypass cooldown and checks)
            fragmentManager.setActiveFragment(target, type);
            
            // Update mana pool for the new Fragment (mana data is auto-created)
            // The mana system will automatically adjust based on the new Fragment rank
            
            admin.sendMessage("§aœ“ Force-activated " + type.getDisplayName() + " Fragment for " + target.getName());
            target.sendMessage("§bœ¦ Your Fragment has been set to " + type.getDisplayName() + " by an administrator");
            
        } catch (IllegalArgumentException e) {
            admin.sendMessage("§cInvalid Fragment type: " + typeName);
            admin.sendMessage("§7Available: FIRE, WATER, AIR, EARTH, DARK, LIGHT, VOID, MOB, DRAGON, STORM");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("gui", "give", "list", "info", "abilities", "mana", "grant", "activate", "forceactivate", "generatepack"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                completions.addAll(Arrays.asList("fire", "water", "air", "earth", "dark", "light", "void", "mob", "dragon", "storm", "changer", "manaflask"));
            } else if (args[0].equalsIgnoreCase("grant") || args[0].equalsIgnoreCase("activate")) {
                completions.addAll(Arrays.asList("FIRE", "WATER", "AIR", "EARTH", "DARK", "LIGHT", "VOID", "MOB", "DRAGON", "STORM"));
            } else if (args[0].equalsIgnoreCase("forceactivate")) {
                // Add online player names
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("forceactivate")) {
                completions.addAll(Arrays.asList("FIRE", "WATER", "AIR", "EARTH", "DARK", "LIGHT", "VOID", "MOB", "DRAGON", "STORM"));
            }
        }

        return completions;
    }
}

