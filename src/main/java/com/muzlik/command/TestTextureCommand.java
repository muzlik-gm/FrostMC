package com.muzlik.command;

import com.muzlik.texture.TextureRegistry;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Debug command to test custom model data textures
 */
public class TestTextureCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§e§lTexture Test Command");
            player.sendMessage("§7Usage: /testtexture <ui_back_button|ui_stats|ui_bonus|ui_controls>");
            player.sendMessage("§7This will give you a paper item with the specified custom model data");
            return true;
        }
        
        String textureName = args[0];
        int customModelData = TextureRegistry.getUITexture(textureName);
        
        ItemStack item = new ItemStack(TextureRegistry.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(customModelData);
        meta.setDisplayName("§e" + textureName);
        meta.setLore(java.util.Arrays.asList(
                "§7Custom Model Data: §f" + customModelData,
                "§7Material: §f" + TextureRegistry.getBaseMaterial(),
                "",
                "§7If you see a custom texture,",
                "§7the resource pack is working!"
        ));
        item.setItemMeta(meta);
        
        player.getInventory().addItem(item);
        player.sendMessage("§aGave you test item: §e" + textureName + " §7(CMD: " + customModelData + ")");
        
        return true;
    }
}
