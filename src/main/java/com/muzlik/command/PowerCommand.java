package com.muzlik.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.muzlik.power.PowerManager;
import com.muzlik.power.IPower;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Command handler for power-related commands
 */
public class PowerCommand implements CommandExecutor, TabCompleter {

    private final PowerManager powerManager;
    private com.muzlik.ui.PowerGUI powerGUI;

    public PowerCommand(PowerManager powerManager) {
        this.powerManager = powerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }

        Player player = (Player) sender;

        // DISABLED: Old power system conflicts with Fragment system
        player.sendMessage("§c✗ The /power command has been disabled");
        player.sendMessage("§7The Fragment system has replaced the old power system");
        player.sendMessage("§7Use §b/fragment §7commands instead:");
        player.sendMessage("§7  §b/fragment list §7- View your Fragments");
        player.sendMessage("§7  §b/fragment activate <type> §7- Activate a Fragment");
        player.sendMessage("§7  §b/fragment info §7- View Fragment details");
        player.sendMessage("§7  §b/fragment abilities §7- View available abilities");
        return true;
    }

    private boolean handleListCommand(Player player) {
        Collection<IPower> powers = powerManager.getAllPowers();

        if (powers.isEmpty()) {
            player.sendMessage("§c✗ No powers available");
            return true;
        }

        player.sendMessage("");
        player.sendMessage("§6Available Powers");
        player.sendMessage("");

        for (IPower power : powers) {
            player.sendMessage("§e● §f" + power.getDisplayName() + " §8(" + power.getId() + ")");
            player.sendMessage("  §7" + power.getTheme());
        }

        player.sendMessage("");
        player.sendMessage("§7Use §e/power activate <id> §7to activate");
        return true;
    }

    private boolean handleActivateCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /power activate <power_id>");
            return true;
        }

        String powerId = args[1].toLowerCase();
        IPower power = powerManager.getPower(powerId);

        if (power == null) {
            player.sendMessage("§c✗ Power not found: §b" + powerId);
            player.sendMessage("§7Use /power list to see available powers");
            return true;
        }

        boolean success = powerManager.activatePower(player, powerId);

        if (success) {
            player.sendMessage("§a✓ Power activated: §b" + power.getDisplayName());
            player.sendMessage("§7Theme: §b" + power.getTheme());
            player.sendMessage("§7");
            player.sendMessage("§7Use abilities with:");
            player.sendMessage("§7  §b• Slot 1: §aSneak + Click§7 (Primary)");
            player.sendMessage("§7  §b• Slot 2: §aSneak + Click§7 (Secondary)");
        }

        return true;
    }

    private boolean handleDeactivateCommand(Player player) {
        IPower current = powerManager.getPlayerActivePower(player);

        if (current == null) {
            player.sendMessage("§c✗ No active power");
            return true;
        }

        powerManager.deactivatePower(player);
        return true;
    }
    
    private boolean handleRandomCommand(Player player) {
        boolean success = powerManager.assignRandomPower(player);
        
        if (success) {
            IPower power = powerManager.getPlayerActivePower(player);
            player.sendMessage("§a✓ Random power assigned: §b" + power.getDisplayName());
            player.sendMessage("§7Theme: §b" + power.getTheme());
        }
        
        return true;
    }
    
    private boolean handleGUICommand(Player player) {
        if (powerGUI != null) {
            powerGUI.openGUI(player);
        } else {
            player.sendMessage("§cGUI system not available");
        }
        return true;
    }

    private boolean handleInfoCommand(Player player) {
        IPower current = powerManager.getPlayerActivePower(player);

        if (current == null) {
            player.sendMessage("§c✗ No active power");
            return true;
        }

        player.sendMessage("");
        player.sendMessage("§6Active Power: §f" + current.getDisplayName());
        player.sendMessage("§7" + current.getTheme());
        player.sendMessage("");
        player.sendMessage("§6Abilities:");

        for (int i = 0; i < 2; i++) { // Only 2 abilities
            com.muzlik.power.IAbility ability = current.getAbilityManager().getAbility(i);
            String slotName = getSlotName(i);

            if (ability != null) {
                player.sendMessage("§e" + (i + 1) + ". §f" + ability.getDisplayName() + " §8(" + slotName + ")");
                player.sendMessage("   §7" + ability.getDescription());
                player.sendMessage("   §8Cooldown: " + (ability.getCooldown() / 1000.0) + "s");
            }
        }

        player.sendMessage("");
        player.sendMessage("§7Hold slots 1-2 and sneak + click to use");
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§6Power Commands");
        player.sendMessage("");
        player.sendMessage("§e/power gui §8- §7Open power menu");
        player.sendMessage("§e/power list §8- §7List all powers");
        player.sendMessage("§e/power activate <id> §8- §7Activate power");
        player.sendMessage("§e/power random §8- §7Get random power");
        player.sendMessage("§e/power info §8- §7View power details");
        player.sendMessage("§e/power deactivate §8- §7Deactivate power");
        player.sendMessage("");
    }

    private String getSlotName(int slot) {
        switch (slot) {
            case 0:
                return "Slot 1 (Primary)";
            case 1:
                return "Slot 2 (Secondary)";
            default:
                return "Unknown";
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, 
                                      String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("list", "activate", "random", "deactivate", "info", "gui", "menu").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("activate")) {
            return powerManager.getAllPowers().stream()
                    .map(IPower::getId)
                    .filter(id -> id.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
    
    /**
     * Set the PowerGUI instance
     */
    public void setPowerGUI(com.muzlik.ui.PowerGUI powerGUI) {
        this.powerGUI = powerGUI;
    }
}


