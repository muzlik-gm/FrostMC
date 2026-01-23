package your.plugin.ritual;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Example plugin showing how to actually use this ritual system
 * 
 * This is basically a demo - shows you how to:
 * - Set up the RitualManager 
 * - Make catalyst items that players can use
 * - Start rituals with commands or by right-clicking items
 * - Clean everything up when the plugin shuts down
 * 
 * You'll want to customize the completion logic for your own plugin obviously
 */
public class ExampleRitualPlugin extends JavaPlugin implements Listener {
    
    private RitualManager ritualManager;
    
    @Override
    public void onEnable() {
        // Set up the ritual system
        ritualManager = new RitualManager(this);
        
        // Register our event listeners
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("Example Ritual Plugin enabled!");
        getLogger().info("Use /ritual <type> <crystal> to start a ritual");
        getLogger().info("Or craft a ritual catalyst and right-click it");
    }
    
    @Override
    public void onDisable() {
        // Make sure we clean up properly
        if (ritualManager != null) {
            ritualManager.shutdown();
        }
        
        getLogger().info("Example Ritual Plugin disabled!");
    }
    
    /**
     * Handle commands - pretty basic stuff
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("ritual")) {
            return handleRitualCommand(player, args);
        }
        
        if (command.getName().equalsIgnoreCase("ritualitem")) {
            return handleRitualItemCommand(player, args);
        }
        
        return false;
    }
    
    /**
     * Handle the /ritual command - starts a ritual directly
     */
    private boolean handleRitualCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ritual <type> <crystal>");
            player.sendMessage("§7Types: creation, upgrade, power, enchantment");
            player.sendMessage("§7Crystals: fire, water, air, earth, dark, light, void, storm, time, luck");
            return true;
        }
        
        // Figure out what ritual type they want
        RitualType ritualType;
        try {
            ritualType = switch (args[0].toLowerCase()) {
                case "creation" -> RitualType.CRYSTAL_CREATION;
                case "upgrade" -> RitualType.CRYSTAL_UPGRADE;
                case "power" -> RitualType.POWER_INFUSION;
                case "enchantment" -> RitualType.ENCHANTMENT_RITUAL;
                default -> throw new IllegalArgumentException("Invalid ritual type");
            };
        } catch (Exception e) {
            player.sendMessage("§cInvalid ritual type: " + args[0]);
            return true;
        }
        
        // Figure out what crystal type they want
        CrystalType crystalType;
        try {
            crystalType = CrystalType.valueOf(args[1].toUpperCase());
        } catch (Exception e) {
            player.sendMessage("§cInvalid crystal type: " + args[1]);
            return true;
        }
        
        // Make a catalyst item for them
        ItemStack catalyst = createRitualCatalyst(ritualType, crystalType);
        
        // Try to start the ritual
        boolean started = ritualManager.startRitual(player, ritualType, catalyst, crystalType);
        
        if (!started) {
            player.sendMessage("§cFailed to start ritual! Check the error messages above.");
        }
        
        return true;
    }
    
    /**
     * Handle /ritualitem command - gives players catalyst items to use
     */
    private boolean handleRitualItemCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ritualitem <type> <crystal>");
            return true;
        }
        
        // Same parsing as the ritual command
        RitualType ritualType;
        CrystalType crystalType;
        
        try {
            ritualType = switch (args[0].toLowerCase()) {
                case "creation" -> RitualType.CRYSTAL_CREATION;
                case "upgrade" -> RitualType.CRYSTAL_UPGRADE;
                case "power" -> RitualType.POWER_INFUSION;
                case "enchantment" -> RitualType.ENCHANTMENT_RITUAL;
                default -> throw new IllegalArgumentException("Invalid ritual type");
            };
            
            crystalType = CrystalType.valueOf(args[1].toUpperCase());
        } catch (Exception e) {
            player.sendMessage("§cInvalid arguments! Use: /ritualitem <type> <crystal>");
            return true;
        }
        
        // Give them the catalyst item
        ItemStack catalyst = createRitualCatalyst(ritualType, crystalType);
        player.getInventory().addItem(catalyst);
        
        player.sendMessage("§a✓ Given ritual catalyst: §b" + ritualType.getDisplayName() + " §7(§e" + crystalType.getDisplayName() + "§7)");
        player.sendMessage("§7Right-click the item to start the ritual!");
        
        return true;
    }
    
    /**
     * Handle right-clicking catalyst items
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return;
        
        String displayName = meta.getDisplayName();
        
        // Check if it's one of our ritual catalysts
        if (displayName.contains("Ritual Catalyst")) {
            event.setCancelled(true);
            
            // Parse the catalyst to figure out what ritual and crystal type it is
            // This is kinda hacky but it works for the demo
            RitualType ritualType = null;
            CrystalType crystalType = null;
            
            // Look through the lore to find the type info
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                for (String line : lore) {
                    if (line.contains("Type:")) {
                        String typeStr = line.replace("§7Type: §e", "").replace("§7", "");
                        for (RitualType type : RitualType.values()) {
                            if (type.getDisplayName().equals(typeStr)) {
                                ritualType = type;
                                break;
                            }
                        }
                    }
                    if (line.contains("Crystal:")) {
                        String crystalStr = line.replace("§7Crystal: §b", "").replace("§7", "");
                        for (CrystalType crystal : CrystalType.values()) {
                            if (crystal.getDisplayName().equals(crystalStr)) {
                                crystalType = crystal;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (ritualType != null && crystalType != null) {
                // Try to start the ritual
                boolean started = ritualManager.startRitual(player, ritualType, item, crystalType);
                
                if (started) {
                    // Remove one item from their hand
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                }
            } else {
                player.sendMessage("§cInvalid ritual catalyst!");
            }
        }
    }
    
    /**
     * Create a catalyst item for rituals
     */
    private ItemStack createRitualCatalyst(RitualType ritualType, CrystalType crystalType) {
        // Use different materials based on what crystal type it is
        Material material = switch (crystalType) {
            case FIRE -> Material.FIRE_CHARGE;
            case WATER -> Material.HEART_OF_THE_SEA;
            case AIR -> Material.PHANTOM_MEMBRANE;
            case EARTH -> Material.EMERALD;
            case DARK -> Material.WITHER_SKELETON_SKULL;
            case LIGHT -> Material.BEACON;
            case VOID -> Material.ENDER_EYE;
            case STORM -> Material.TRIDENT;
            case TIME -> Material.CLOCK;
            case LUCK -> Material.RABBIT_FOOT;
        };
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§5§lRitual Catalyst");
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Type: §e" + ritualType.getDisplayName());
            lore.add("§7Crystal: §b" + crystalType.getDisplayName());
            lore.add("");
            lore.add("§7" + crystalType.getDescription());
            lore.add("");
            lore.add("§7Duration: §e" + (ritualType.getDefaultDuration() / 60) + " minutes");
            lore.add("");
            lore.add("§e§l▶ RIGHT-CLICK TO START RITUAL");
            lore.add("§8Requires open area under the sky");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
}