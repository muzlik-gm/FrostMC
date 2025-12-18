package com.muzlik.fragment;

import com.muzlik.fragment.level.XPCurve;
import com.muzlik.fx.ColorScheme;
import com.muzlik.fx.FXLibrary;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers all 10 Fragment definitions.
 */
public class FragmentRegistry {
    private final JavaPlugin plugin;
    private final FragmentManager fragmentManager;
    private final FXLibrary fxLibrary;

    public FragmentRegistry(JavaPlugin plugin, FragmentManager fragmentManager, FXLibrary fxLibrary) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.fxLibrary = fxLibrary;
    }

    /**
     * Register all Fragment definitions
     */
    public void registerAllFragments() {
        registerFireFragment();
        registerWaterFragment();
        registerAirFragment();
        registerEarthFragment();
        registerDarkFragment();
        registerLightFragment();
        registerVoidFragment();
        registerMobFragment();
        registerDragonFragment();
        registerStormFragment();
        registerAdminFragment();
        
        // Logging removed for cleaner console
    }

    private void registerFireFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.FIRE);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.FIRE)
            .baseRank(3)
            .colorScheme(colors)
            .xpCurve(XPCurve.EXPONENTIAL)
            .deathXPPenalty(0.1)
            .theme("Burning, explosions, aggressive combat")
            .complexityLevel(3);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_fireball", FragmentType.FIRE)
            .displayName("Fireball")
            .description("Shoots a fireball that explodes on impact, ignites enemies for 5s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(20)
            .cooldown(5000)
            .executor(new com.muzlik.fragment.ability.executors.fire.FireballExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_dash", FragmentType.FIRE)
            .displayName("Dash")
            .description("Dash forward 8 blocks, leaves burning trail that damages enemies")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(30)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.fire.DashExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_inferno", FragmentType.FIRE)
            .displayName("Inferno")
            .description("Creates massive fire vortex in 6-block radius, pulls enemies in while burning")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(60)
            .cooldown(20000)
            .executor(new com.muzlik.fragment.ability.executors.fire.InfernoExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_rebirth", FragmentType.FIRE)
            .displayName("Rebirth")
            .description("§7[PASSIVE] Upon death, revive with 50% HP in flames, damaging nearby enemies. §8Cooldown: 1 hour")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(80)
            .cooldown(3600000) // 1 hour
            .rankRequirement(4)
            .executor(new com.muzlik.fragment.ability.executors.fire.RebirthExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.PASSIVE)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_ignite", FragmentType.FIRE)
            .displayName("Ignite")
            .description("Become living flame for 15s: immune to damage, all fire abilities cost 50% less, +50% damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(100)
            .cooldown(90000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.fire.IgniteExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        fragmentManager.registerFragment(builder.build());
    }

    private void registerWaterFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.WATER);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.WATER)
            .baseRank(2)
            .colorScheme(colors)
            .xpCurve(XPCurve.LINEAR)
            .deathXPPenalty(0.05)
            .theme("Healing, defense, fluid control")
            .complexityLevel(2);
        
        // Core Abilities ONLY (Rank 2 cannot expand) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("water_splash", FragmentType.WATER)
            .displayName("Splash")
            .description("Creates healing water sphere around caster, heals 4 hearts over 5 seconds (AOE 4 blocks)")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(25)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.water.SplashExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("water_shield", FragmentType.WATER)
            .displayName("Shield")
            .description("Creates water barrier that absorbs 6 hearts of damage for 8 seconds")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(35)
            .cooldown(12000)
            .executor(new com.muzlik.fragment.ability.executors.water.ShieldExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("water_wave", FragmentType.WATER)
            .displayName("Wave")
            .description("Summons massive wave that pushes enemies back and slows them")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(55)
            .cooldown(18000)
            .executor(new com.muzlik.fragment.ability.executors.water.WaveExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Note: Water Fragment (Rank 2) cannot unlock additional abilities beyond these 3 core abilities
        fragmentManager.registerFragment(builder.build());
    }

    private void registerAirFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.AIR);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.AIR)
            .baseRank(3)
            .colorScheme(colors)
            .xpCurve(XPCurve.LOGARITHMIC)
            .deathXPPenalty(0.08)
            .theme("Speed, mobility, precision strikes")
            .complexityLevel(4);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_slice", FragmentType.AIR)
            .displayName("Slice")
            .description("Sends razor-sharp wind projectile that pierces enemies, 5 hearts damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(18)
            .cooldown(4000)
            .executor(new com.muzlik.fragment.ability.executors.air.SliceExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_dash", FragmentType.AIR)
            .displayName("Dash")
            .description("Instant dash 10 blocks in facing direction, +60% speed for 4s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(25)
            .cooldown(6000)
            .executor(new com.muzlik.fragment.ability.executors.air.DashExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_barrage", FragmentType.AIR)
            .displayName("Barrage")
            .description("Unleashes 8 wind blades in rapid succession, each dealing 3 hearts")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(65)
            .cooldown(22000)
            .executor(new com.muzlik.fragment.ability.executors.air.BarrageExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_armor", FragmentType.AIR)
            .displayName("Armor")
            .description("Surrounds self with wind barrier that reflects projectiles and damages melee attackers")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(45)
            .cooldown(15000)
            .rankRequirement(4)
            .executor(new com.muzlik.fragment.ability.executors.air.ArmorExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_flight", FragmentType.AIR)
            .displayName("Flight")
            .description("§7Fly freely for §f10 minutes§7, +100% speed. §8Hold sneak+left click 10s to cancel (no cooldown)")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(90)
            .cooldown(75000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.air.FlightExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        fragmentManager.registerFragment(builder.build());
    }

    private void registerEarthFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.EARTH);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.EARTH)
            .baseRank(2)
            .colorScheme(colors)
            .xpCurve(XPCurve.LINEAR)
            .deathXPPenalty(0.05)
            .theme("Defense, tanking, terrain control")
            .complexityLevel(3);
        
        // Core Abilities ONLY (Rank 2 cannot expand) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("earth_punch", FragmentType.EARTH)
            .displayName("Punch")
            .description("Powerful earth-enhanced punch, 7 hearts damage + knockback")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(20)
            .cooldown(5000)
            .executor(new com.muzlik.fragment.ability.executors.earth.PunchExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("earth_wall", FragmentType.EARTH)
            .displayName("Wall")
            .description("+8 armor points for 8 seconds, reduces damage by 40%")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(30)
            .cooldown(10000)
            .executor(new com.muzlik.fragment.ability.executors.earth.WallExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("earth_slam", FragmentType.EARTH)
            .displayName("Slam")
            .description("Slams ground creating 7-block radius shockwave, 6 hearts damage + stun 2s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(50)
            .cooldown(16000)
            .executor(new com.muzlik.fragment.ability.executors.earth.SlamExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Note: Earth Fragment (Rank 2) cannot unlock additional abilities beyond these 3 core abilities
        fragmentManager.registerFragment(builder.build());
    }

    private void registerDarkFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.DARK);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.DARK)
            .baseRank(4)
            .colorScheme(colors)
            .xpCurve(XPCurve.EXPONENTIAL)
            .deathXPPenalty(0.15)
            .theme("Debuffs, life-steal, shadow manipulation")
            .complexityLevel(5);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_strike", FragmentType.DARK)
            .displayName("Strike")
            .description("Dark bolt that curses enemies, 4 hearts + Wither II for 6s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(22)
            .cooldown(6000)
            .executor(new com.muzlik.fragment.ability.executors.dark.StrikeExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_drain", FragmentType.DARK)
            .displayName("Drain")
            .description("Drains 5 hearts from target, heals caster for 70% of damage dealt")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(40)
            .cooldown(12000)
            .executor(new com.muzlik.fragment.ability.executors.dark.DrainExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_void", FragmentType.DARK)
            .displayName("Void")
            .description("Creates 8-block dark zone: enemies take Wither III, Slowness III, Weakness II for 10s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(70)
            .cooldown(25000)
            .executor(new com.muzlik.fragment.ability.executors.dark.VoidExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_clone", FragmentType.DARK)
            .displayName("Clone")
            .description("Creates 2 shadow clones that mimic your attacks for 15s, each dealing 50% damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(55)
            .cooldown(20000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.dark.CloneExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_shroud", FragmentType.DARK)
            .displayName("Shroud")
            .description("Become shadow incarnate for 15s: invisible, +75% damage, life steal on all attacks")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(95)
            .cooldown(80000)
            .rankRequirement(6)
            .executor(new com.muzlik.fragment.ability.executors.dark.ShroudExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        fragmentManager.registerFragment(builder.build());
    }

    private void registerLightFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.LIGHT);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.LIGHT)
            .baseRank(4)
            .colorScheme(colors)
            .xpCurve(XPCurve.EXPONENTIAL)
            .deathXPPenalty(0.12)
            .theme("Holy power, purification, radiant attacks")
            .complexityLevel(5);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_beam", FragmentType.LIGHT)
            .displayName("Beam")
            .description("Shoots concentrated light beam that pierces through enemies, 6 hearts + Blindness 4s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(25)
            .cooldown(5000)
            .executor(new com.muzlik.fragment.ability.executors.light.BeamExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_heal", FragmentType.LIGHT)
            .displayName("Heal")
            .description("Heals 5 hearts, removes all negative effects, grants Regeneration II for 8s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(35)
            .cooldown(10000)
            .executor(new com.muzlik.fragment.ability.executors.light.HealExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_smite", FragmentType.LIGHT)
            .displayName("Smite")
            .description("Summons pillar of light from sky, 7-block radius, 6 hearts damage to undead/dark, 4 hearts to others")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(65)
            .cooldown(20000)
            .executor(new com.muzlik.fragment.ability.executors.light.SmiteExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_sanctuary", FragmentType.LIGHT)
            .displayName("Sanctuary")
            .description("Creates 6-block holy zone for 12s: allies heal 1 heart/sec, enemies take 2 hearts/sec")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(50)
            .cooldown(18000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.light.SanctuaryExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_wings", FragmentType.LIGHT)
            .displayName("Wings")
            .description("Become angelic being for 15s: flight, all healing doubled, immune to debuffs, +60% damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(100)
            .cooldown(85000)
            .rankRequirement(6)
            .executor(new com.muzlik.fragment.ability.executors.light.WingsExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        fragmentManager.registerFragment(builder.build());
    }

    private void registerVoidFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.VOID);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.VOID)
            .baseRank(7)
            .colorScheme(colors)
            .xpCurve(XPCurve.EXPONENTIAL)
            .deathXPPenalty(0.20)
            .theme("Space-time manipulation, teleportation, dimensional power")
            .complexityLevel(8);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("void_slash", FragmentType.VOID)
            .displayName("Slash")
            .description("Tears space itself, dealing true damage that bypasses armor and totems, 2 hearts fixed")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(32)
            .cooldown(300000) // 5 minutes
            .executor(new com.muzlik.fragment.ability.executors.voidfrag.SlashExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("void_blink", FragmentType.VOID)
            .displayName("Blink")
            .description("Instantly teleport up to 15 blocks in facing direction, leaves void rift that damages enemies")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(35)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.voidfrag.BlinkExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("void_collapse", FragmentType.VOID)
            .displayName("Collapse")
            .description("Creates black hole at target location, pulls all enemies within 10 blocks, 3 hearts damage + stun 3s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(85)
            .cooldown(28000)
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.voidfrag.CollapseExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Note: VOID Fragment unlocks Slot 3 at Rank 9, Slot 4 at Rank 11 (not implemented yet)
        fragmentManager.registerFragment(builder.build());
    }

    private void registerMobFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.MOB);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.MOB)
            .baseRank(5)
            .colorScheme(colors)
            .xpCurve(XPCurve.LINEAR)
            .deathXPPenalty(0.10)
            .theme("Summoning, minion control, beast mastery")
            .complexityLevel(6);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("mob_summon", FragmentType.MOB)
            .displayName("Summon")
            .description("Summons wolf pack (3 wolves) that fight for 30s, scales with rank")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(30)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.mob.SummonExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("mob_guardian", FragmentType.MOB)
            .displayName("Guardian")
            .description("Summons Iron Golem that protects caster for 45s, taunts enemies")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(45)
            .cooldown(15000)
            .executor(new com.muzlik.fragment.ability.executors.mob.GuardianExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("mob_legion", FragmentType.MOB)
            .displayName("Legion")
            .description("Summons 5 skeleton warriors + 2 zombie brutes that fight for 60s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(80)
            .cooldown(30000)
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.mob.LegionExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Note: MOB Fragment unlocks Slot 3 at Rank 7, Slot 4 at Rank 9 (not implemented yet)
        fragmentManager.registerFragment(builder.build());
    }

    private void registerDragonFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.DRAGON);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.DRAGON)
            .baseRank(8)
            .colorScheme(colors)
            .xpCurve(XPCurve.EXPONENTIAL)
            .deathXPPenalty(0.25)
            .theme("Supreme draconic power, flight, overwhelming force")
            .complexityLevel(9);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_roar", FragmentType.DRAGON)
            .displayName("Roar")
            .description("Unleashes devastating dragon breath in 12-block cone, ignites enemies, 9 hearts damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(40)
            .cooldown(10000) // Increased from 6s to 10s
            .executor(new com.muzlik.fragment.ability.executors.dragon.RoarExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_wings", FragmentType.DRAGON)
            .displayName("Wings")
            .description("§7Fly freely for §530 minutes§7, +80% speed, immune to fall damage. §8Hold sneak+left click 10s to cancel (no cooldown)")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(50)
            .cooldown(18000) // Increased from 12s to 18s
            .executor(new com.muzlik.fragment.ability.executors.dragon.WingsExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_cataclysm", FragmentType.DRAGON)
            .displayName("Cataclysm")
            .description("Transform into dragon form for 10s: all abilities cost 0 mana, +100% damage, AOE attacks")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(100)
            .cooldown(120000) // Increased from 90s to 120s (2 minutes) for balance
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.dragon.CataclysmExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Rank 9 Ability - Slot 3 (ADVANCED)
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_fury", FragmentType.DRAGON)
            .displayName("Fury")
            .description("Summon 5 explosive homing fireballs that track enemies within 30 blocks")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(80)
            .cooldown(30000) // Increased from 20s to 30s
            .rankRequirement(9)
            .executor(new com.muzlik.fragment.ability.executors.dragon.FuryExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Rank 10 Ability - Slot 4 (MASTERY)
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_ascension", FragmentType.DRAGON)
            .displayName("Ascension")
            .description("§5§lULTIMATE: §7Become a true dragon for 15s - Strength IV, Resistance IV, AOE damage aura, flight")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(150)
            .cooldown(120000) // Increased from 90s to 120s (2 minutes)
            .rankRequirement(10)
            .executor(new com.muzlik.fragment.ability.executors.dragon.AscensionExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        fragmentManager.registerFragment(builder.build());
    }

    private void registerStormFragment() {
        ColorScheme colors = fxLibrary.getColorScheme(FragmentType.STORM);
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.STORM)
            .baseRank(6)
            .colorScheme(colors)
            .xpCurve(XPCurve.EXPONENTIAL)
            .deathXPPenalty(0.18)
            .theme("Lightning, thunder, chain attacks")
            .complexityLevel(7);
        
        // Core Abilities (Always Available) - Slots 0, 1, 2
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("storm_bolt", FragmentType.STORM)
            .displayName("Bolt")
            .description("Summons lightning strike on target location, instant cast, 4 hearts damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(28)
            .cooldown(4000)
            .executor(new com.muzlik.fragment.ability.executors.storm.BoltExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("storm_chain", FragmentType.STORM)
            .displayName("Chain")
            .description("Lightning chains between up to 5 enemies within 8 blocks, 5 hearts per target")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(40)
            .cooldown(10000)
            .executor(new com.muzlik.fragment.ability.executors.storm.ChainExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("storm_descent", FragmentType.STORM)
            .displayName("Descent")
            .description("Calls down 10 lightning strikes in 10-block radius over 5 seconds, each 4 hearts")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(75)
            .cooldown(24000)
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.storm.DescentExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Note: STORM Fragment unlocks Slot 3 at Rank 8, Slot 4 at Rank 10 (not implemented yet)
        fragmentManager.registerFragment(builder.build());
    }

    private void registerAdminFragment() {
        ColorScheme colors = new ColorScheme(
            org.bukkit.Color.fromRGB(139, 0, 0),    // Primary: Dark Red
            org.bukkit.Color.fromRGB(0, 0, 0),      // Secondary: Black
            org.bukkit.Color.fromRGB(255, 0, 0),    // Accent: Bright Red
            org.bukkit.Particle.SOUL_FIRE_FLAME
        );
        
        FragmentDefinition.Builder builder = new FragmentDefinition.Builder(FragmentType.ADMIN)
            .baseRank(10)
            .colorScheme(colors)
            .xpCurve(XPCurve.EXPONENTIAL)
            .deathXPPenalty(0.0) // No penalty for admin
            .theme("Ultimate destruction, reality manipulation, absolute power")
            .complexityLevel(10);
        
        // All 9 abilities available immediately
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_reality_tear", FragmentType.ADMIN)
            .displayName("§4§lReality Tear")
            .description("§cMassive cone attack, 50 true damage to players, instant kill mobs, 30 block range")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(0)
            .cooldown(3000)
            .executor(new com.muzlik.fragment.ability.executors.admin.RealityTearExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_omnipresent_blink", FragmentType.ADMIN)
            .displayName("§4§lOmnipresent Blink")
            .description("§cTeleport anywhere within 100 blocks, through walls, destruction trail")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(0)
            .cooldown(1000)
            .executor(new com.muzlik.fragment.ability.executors.admin.OmnipresentBlinkExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_cataclysm", FragmentType.ADMIN)
            .displayName("§4§lCataclysm")
            .description("§c25-block explosion, 40 damage to players, instant kill mobs, destroys terrain")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(0)
            .cooldown(10000)
            .executor(new com.muzlik.fragment.ability.executors.admin.CataclysmExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_void_chains", FragmentType.ADMIN)
            .displayName("§4§lVoid Chains")
            .description("§cImmobilize and pull all entities within 30 blocks, 5 damage/sec for 8s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(0)
            .cooldown(15000)
            .executor(new com.muzlik.fragment.ability.executors.admin.VoidChainsExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_annihilation_beam", FragmentType.ADMIN)
            .displayName("§4§lAnnihilation Beam")
            .description("§cContinuous laser beam, 10 damage/tick, 50 block range, destroys blocks")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(0)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.admin.AnnihilationBeamExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Additional slots (6-8) for admin
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_temporal_freeze", FragmentType.ADMIN)
            .displayName("§4§lTemporal Freeze")
            .description("§cFreeze all entities within 40 blocks for 6 seconds, you move normally")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SLOT_6)
            .manaCost(0)
            .cooldown(20000)
            .executor(new com.muzlik.fragment.ability.executors.admin.TemporalFreezeExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_meteor_storm", FragmentType.ADMIN)
            .displayName("§4§lMeteor Storm")
            .description("§c15 meteors rain down in 30-block radius, 15 damage each + fire + craters")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SLOT_7)
            .manaCost(0)
            .cooldown(25000)
            .executor(new com.muzlik.fragment.ability.executors.admin.MeteorStormExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_execution", FragmentType.ADMIN)
            .displayName("§4§lExecution")
            .description("§cPoint at any entity within 50 blocks for instant death, even players")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SLOT_8)
            .manaCost(0)
            .cooldown(30000)
            .executor(new com.muzlik.fragment.ability.executors.admin.ExecutionExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("admin_apocalypse", FragmentType.ADMIN)
            .displayName("§4§l§kA§r §4§lAPOCALYPSE §4§l§kA")
            .description("§c60-block devastation, 80 damage to players, instant kill mobs, screen shake")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SLOT_9)
            .manaCost(0)
            .cooldown(60000)
            .executor(new com.muzlik.fragment.ability.executors.admin.ApocalypseExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        fragmentManager.registerFragment(builder.build());
    }
}
