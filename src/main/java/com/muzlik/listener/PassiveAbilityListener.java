package com.muzlik.listener;

import com.muzlik.FrostSMPPlugin;
import com.muzlik.fragment.FragmentManager;
import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.rank.RankManager;
import com.muzlik.mana.ManaManager;
import com.muzlik.cooldown.CooldownManager;
import com.muzlik.vfx.VFXLayerBuilder;
import com.muzlik.vfx.ParticlePattern;
import com.muzlik.vfx.CinematicEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles passive abilities that trigger on death or damage
 * - Phoenix Rebirth: Revive on death
 * - Other passive abilities can be added here
 */
public class PassiveAbilityListener implements Listener {
    private final FrostSMPPlugin plugin;
    private final FragmentManager fragmentManager;
    private final RankManager rankManager;
    private final ManaManager manaManager;
    private final CooldownManager cooldownManager;
    
    // Track players with Phoenix Rebirth active (waiting to revive)
    private final Map<UUID, Long> phoenixRebirthActive = new HashMap<>();
    
    // Cooldown: 1 hour (3600000ms)
    private static final long PHOENIX_REBIRTH_COOLDOWN = 3600000L;
    private static final int PHOENIX_REBIRTH_MANA_COST = 80;
    
    public PassiveAbilityListener(FrostSMPPlugin plugin, FragmentManager fragmentManager, 
                                  RankManager rankManager, ManaManager manaManager, 
                                  CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.rankManager = rankManager;
        this.manaManager = manaManager;
        this.cooldownManager = cooldownManager;
    }
    
    /**
     * Handle player death - check for Phoenix Rebirth
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if player has Fire fragment and Phoenix Rebirth unlocked
        if (!fragmentManager.hasFragment(player, FragmentType.FIRE)) {
            return;
        }
        
        // Check if Phoenix Rebirth is unlocked (Rank 5)
        int rank = rankManager.getRank(player, FragmentType.FIRE);
        if (rank < 5) {
            return;
        }
        
        // Check if on cooldown
        String cooldownKey = "fire_phoenix_rebirth";
        if (cooldownManager.isOnCooldown(player, cooldownKey)) {
            long remaining = cooldownManager.getRemainingCooldown(player, cooldownKey);
            long minutes = remaining / 60000;
            player.sendMessage("§8ᴘʜᴏᴇɴɪx ʀᴇʙɪʀᴛʜ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ: §c" + minutes + "ᴍ");
            return;
        }
        
        // Check if player has enough mana
        if (manaManager.getMana(player) < PHOENIX_REBIRTH_MANA_COST) {
            player.sendMessage("§8ɴᴏᴛ ᴇɴᴏᴜɢʜ ᴍᴀɴᴀ ꜰᴏʀ ᴘʜᴏᴇɴɪx ʀᴇʙɪʀᴛʜ");
            return;
        }
        
        // ACTIVATE PHOENIX REBIRTH!
        event.setCancelled(true); // Cancel death
        
        // Consume mana
        manaManager.consumeMana(player, PHOENIX_REBIRTH_MANA_COST);
        
        // Revive player with 50% HP
        double maxHealth = player.getMaxHealth();
        player.setHealth(maxHealth * 0.5);
        
        // Clear death effects
        player.setFireTicks(0);
        player.setFallDistance(0);
        
        // Add brief invulnerability
        player.setNoDamageTicks(60); // 3 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0, false, false));
        
        // Spawn epic VFX
        spawnPhoenixRebirthVFX(player, rank);
        
        // Damage nearby enemies
        damageNearbyEnemies(player, rank);
        
        // Start cooldown AFTER revival
        cooldownManager.startCooldown(player, cooldownKey, PHOENIX_REBIRTH_COOLDOWN);
        
        // Message
        player.sendMessage("§6§l🔥 ᴘʜᴏᴇɴɪx ʀᴇʙɪʀᴛʜ! §7ʏᴏᴜ ʀɪsᴇ ꜰʀᴏᴍ ᴛʜᴇ ᴀsʜᴇs!");
        player.sendMessage("§8ᴄᴏᴏʟᴅᴏᴡɴ: §c1 ʜᴏᴜʀ");
        
        plugin.getLogger().info("[Phoenix Rebirth] " + player.getName() + " revived!");
    }
    
    /**
     * Spawn Phoenix Rebirth VFX
     */
    private void spawnPhoenixRebirthVFX(Player player, int rank) {
        // 5-Layer VFX System with slow-motion cinematic
        VFXLayerBuilder vfxBuilder = new VFXLayerBuilder(plugin, player.getLocation(), rank, player)
            .withPerformanceManager(plugin.getVFXPerformanceManager())
            // Core: FLAME rising phoenix
            .core(Particle.FLAME, 150, ParticlePattern.SPIRAL, 1.5, 3.0, 1.5, 0.15, null)
            // Secondary: END_ROD wings
            .secondary(Particle.END_ROD, 80, ParticlePattern.SPHERE, 2.0, 1.5, 2.0, 0.08, null)
            // Ambient: SMOKE_LARGE aura
            .ambient(Particle.SMOKE_LARGE, 60, ParticlePattern.RING, 1.5, 0.8, 1.5, 0.03, null)
            // Impact: LAVA burst
            .impact(Particle.LAVA, 80, ParticlePattern.BURST, 1.5, 1.5, 1.5, 0.15, null)
            // Cinematic: Slow-motion effect
            .cinematic(0.4, CinematicEffect.SLOW_MOTION);
        
        vfxBuilder.spawn();
        
        // Epic sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.5f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 1.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);
    }
    
    /**
     * Damage nearby enemies on revival
     */
    private void damageNearbyEnemies(Player player, int rank) {
        double radius = 5.0 + (rank * 0.5); // 5.5 blocks at rank 1, up to 9 blocks at rank 8
        double damage = 4.0 + (rank * 0.5); // 4.5 hearts at rank 1, up to 8 hearts at rank 8
        
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity target = (LivingEntity) entity;
                
                // Deal damage
                target.damage(damage, player);
                
                // Set on fire
                target.setFireTicks(60 + (rank * 10)); // 3-7 seconds
                
                // Knockback
                target.setVelocity(target.getLocation().toVector()
                    .subtract(player.getLocation().toVector())
                    .normalize()
                    .multiply(0.8)
                    .setY(0.3));
            }
        }
    }
}
