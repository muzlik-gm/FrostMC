package com.muzlik.command;

import com.muzlik.ui.UIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for opening control scheme GUI
 */
public class ControlsCommand implements CommandExecutor {
    
    private final UIManager uiManager;
    
    public ControlsCommand(UIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players");
            return true;
        }
        
        Player player = (Player) sender;
        uiManager.openControlSchemeGUI(player);
        
        return true;
    }
}
