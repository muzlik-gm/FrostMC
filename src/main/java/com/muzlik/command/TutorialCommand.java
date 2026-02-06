package com.muzlik.command;

import com.muzlik.tutorial.InteractiveTutorial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Command handler for the new interactive tutorial system
 */
public class TutorialCommand implements CommandExecutor, TabCompleter {
    
    private final InteractiveTutorial tutorial;
    
    public TutorialCommand(InteractiveTutorial tutorial) {
        this.tutorial = tutorial;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
            case "continue":
                return handleStart(sender, args);
                
            case "reset":
                return handleReset(sender, args);
                
            case "complete":
                return handleComplete(sender, args);
                
            case "status":
                return handleStatus(sender, args);
                
            case "stats":
                return handleStats(sender);
                
            case "help":
                showHelp(sender);
                return true;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                showHelp(sender);
                return true;
        }
    }
    
    private boolean handleStart(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can start the tutorial!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (tutorial.hasCompletedTutorial(player)) {
            player.sendMessage(ChatColor.YELLOW + "You've already completed the tutorial!");
            player.sendMessage(ChatColor.GRAY + "Contact an admin if you need it reset.");
            return true;
        }
        
        // Check if player is already in tutorial
        if (tutorial.isInTutorial(player)) {
            String currentPhase = tutorial.getCurrentPhase(player);
            player.sendMessage(ChatColor.GREEN + "Tutorial is already active!");
            player.sendMessage(ChatColor.AQUA + "Current phase: " + ChatColor.YELLOW + currentPhase);
            player.sendMessage(ChatColor.GRAY + "Continue following the instructions to progress.");
            
            // Optionally provide a hint for the current phase
            tutorial.provideCurrentPhaseHint(player);
            return true;
        }
        
        // Start new tutorial
        tutorial.forceStartTutorial(player);
        player.sendMessage(ChatColor.GREEN + "Tutorial started! Follow the instructions to learn Fragment powers.");
        return true;
    }
    
    private boolean handleReset(CommandSender sender, String[] args) {
        Player target;
        
        if (args.length > 1) {
            // Admin resetting another player's tutorial
            if (!sender.hasPermission("frostmc.tutorial.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reset other players' tutorials!");
                return true;
            }
            
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }
        } else {
            // Player trying to reset their own tutorial - ONLY ADMINS ALLOWED
            if (!sender.hasPermission("frostmc.tutorial.admin")) {
                sender.sendMessage(ChatColor.RED + "Only administrators can reset tutorials!");
                sender.sendMessage(ChatColor.GRAY + "Contact an admin if you need your tutorial reset.");
                return true;
            }
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player name!");
                return true;
            }
            target = (Player) sender;
        }
        
        tutorial.resetTutorial(target);
        
        if (target == sender) {
            sender.sendMessage(ChatColor.GREEN + "Your tutorial has been reset!");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Reset tutorial for " + target.getName());
            target.sendMessage(ChatColor.YELLOW + "Your tutorial has been reset by " + sender.getName());
        }
        
        return true;
    }
    
    private boolean handleComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frostmc.tutorial.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to force-complete tutorials!");
            return true;
        }
        
        Player target;
        
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player name!");
                return true;
            }
            target = (Player) sender;
        }
        
        tutorial.forceCompleteTutorial(target);
        
        if (target == sender) {
            sender.sendMessage(ChatColor.GREEN + "Your tutorial has been completed!");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Completed tutorial for " + target.getName());
            target.sendMessage(ChatColor.YELLOW + "Your tutorial has been completed by " + sender.getName());
        }
        
        return true;
    }
    
    private boolean handleStatus(CommandSender sender, String[] args) {
        Player target;
        
        if (args.length > 1) {
            if (!sender.hasPermission("frostmc.tutorial.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to check other players' tutorial status!");
                return true;
            }
            
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player name!");
                return true;
            }
            target = (Player) sender;
        }
        
        boolean completed = tutorial.hasCompletedTutorial(target);
        
        if (target == sender) {
            if (completed) {
                sender.sendMessage(ChatColor.GREEN + "✓ You have completed the tutorial!");
            } else {
                // Check if player is currently in tutorial
                if (tutorial.isInTutorial(target)) {
                    String currentPhase = tutorial.getCurrentPhase(target);
                    sender.sendMessage(ChatColor.YELLOW + "○ You are currently in the tutorial.");
                    sender.sendMessage(ChatColor.GRAY + "Current phase: " + ChatColor.AQUA + currentPhase);
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "○ You haven't completed the tutorial yet.");
                    sender.sendMessage(ChatColor.GRAY + "Use '/tutorial start' to begin!");
                }
            }
        } else {
            if (completed) {
                sender.sendMessage(ChatColor.GREEN + target.getName() + " has completed the tutorial.");
            } else {
                if (tutorial.isInTutorial(target)) {
                    String currentPhase = tutorial.getCurrentPhase(target);
                    sender.sendMessage(ChatColor.YELLOW + target.getName() + " is currently in the tutorial.");
                    sender.sendMessage(ChatColor.GRAY + "Current phase: " + ChatColor.AQUA + currentPhase);
                } else {
                    sender.sendMessage(ChatColor.YELLOW + target.getName() + " hasn't completed the tutorial yet.");
                }
            }
        }
        
        return true;
    }
    
    private boolean handleStats(CommandSender sender) {
        if (!sender.hasPermission("frostmc.tutorial.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to view tutorial statistics!");
            return true;
        }
        
        Map<String, Object> stats = tutorial.getStats();
        
        sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════════════════════");
        sender.sendMessage(ChatColor.DARK_PURPLE + "    Tutorial Statistics");
        sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════════════════════");
        sender.sendMessage("");
        
        sender.sendMessage(ChatColor.AQUA + "Completed Players: " + ChatColor.WHITE + stats.get("completed_players"));
        sender.sendMessage(ChatColor.AQUA + "Active Tutorials: " + ChatColor.WHITE + stats.get("active_tutorials"));
        sender.sendMessage("");
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> phaseDistribution = (Map<String, Integer>) stats.get("phase_distribution");
        
        if (!phaseDistribution.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Current Phase Distribution:");
            for (Map.Entry<String, Integer> entry : phaseDistribution.entrySet()) {
                String phaseName = formatPhaseName(entry.getKey());
                sender.sendMessage(ChatColor.GRAY + "  " + phaseName + ": " + ChatColor.WHITE + entry.getValue());
            }
        } else {
            sender.sendMessage(ChatColor.GRAY + "No active tutorials currently running.");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════════════════════");
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════════════════════");
        sender.sendMessage(ChatColor.DARK_PURPLE + "    Interactive Tutorial");
        sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════════════════════");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "/tutorial start" + ChatColor.GRAY + " - Start or continue the tutorial");
        sender.sendMessage(ChatColor.AQUA + "/tutorial continue" + ChatColor.GRAY + " - Same as start (alias)");
        sender.sendMessage(ChatColor.AQUA + "/tutorial status [player]" + ChatColor.GRAY + " - Check tutorial completion");
        sender.sendMessage("");
        
        if (sender.hasPermission("frostmc.tutorial.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "Admin Commands:");
            sender.sendMessage(ChatColor.GOLD + "/tutorial reset [player]" + ChatColor.GRAY + " - Reset tutorial progress");
            sender.sendMessage(ChatColor.GOLD + "/tutorial complete [player]" + ChatColor.GRAY + " - Force complete tutorial");
            sender.sendMessage(ChatColor.GOLD + "/tutorial stats" + ChatColor.GRAY + " - View tutorial statistics");
            sender.sendMessage("");
        }
        
        sender.sendMessage(ChatColor.GREEN + "The tutorial teaches through gameplay - learn by doing");
        sender.sendMessage(ChatColor.GRAY + "Learn by doing real actions and completing challenges.");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════════════════════");
    }
    
    private String formatPhaseName(String phaseName) {
        return Arrays.stream(phaseName.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(phaseName);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("start", "continue", "status", "help");
            
            if (sender.hasPermission("frostmc.tutorial.admin")) {
                subCommands = Arrays.asList("start", "continue", "reset", "complete", "status", "stats", "help");
            }
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // Second argument - player names for applicable commands
            String subCommand = args[0].toLowerCase();
            
            if ((subCommand.equals("reset") || subCommand.equals("status") || subCommand.equals("complete")) && 
                sender.hasPermission("frostmc.tutorial.admin")) {
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            } else if (subCommand.equals("status")) {
                // Status can be used by anyone for themselves, but only admins for others
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}