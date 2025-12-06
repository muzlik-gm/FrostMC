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
        
        plugin.getLogger().info("All 10 Fragments registered successfully");
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_flame_burst", FragmentType.FIRE)
            .displayName("Flame Burst")
            .description("Shoots fireball that explodes on impact, ignites enemies for 5s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(20)
            .cooldown(5000)
            .executor(new com.muzlik.fragment.ability.executors.fire.FlameBurstExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_blazing_step", FragmentType.FIRE)
            .displayName("Blazing Step")
            .description("Dash forward 8 blocks, leaves burning trail that damages enemies")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(30)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.fire.BlazingStepExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_inferno_maelstrom", FragmentType.FIRE)
            .displayName("Inferno Maelstrom")
            .description("Creates massive fire vortex in 6-block radius, pulls enemies in while burning")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(60)
            .cooldown(20000)
            .executor(new com.muzlik.fragment.ability.executors.fire.InfernoMaelstromExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_phoenix_rebirth", FragmentType.FIRE)
            .displayName("Phoenix Rebirth")
            .description("§7[PASSIVE] Upon death, revive with 50% HP in flames, damaging nearby enemies. §8Cooldown: 1 hour")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(80)
            .cooldown(3600000) // 1 hour
            .rankRequirement(4)
            .executor(new com.muzlik.fragment.ability.executors.fire.PhoenixRebirthExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.PASSIVE)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("fire_eternal_flame", FragmentType.FIRE)
            .displayName("Eternal Flame")
            .description("Become living flame for 15s: immune to damage, all fire abilities cost 50% less, +50% damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(100)
            .cooldown(90000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.fire.EternalFlameExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("water_aqua_pulse", FragmentType.WATER)
            .displayName("Aqua Pulse")
            .description("Creates healing water sphere around caster, heals 4 hearts over 5 seconds (AOE 4 blocks)")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(25)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.water.AquaPulseExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("water_tidal_shield", FragmentType.WATER)
            .displayName("Tidal Shield")
            .description("Creates water barrier that absorbs 6 hearts of damage for 8 seconds")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(35)
            .cooldown(12000)
            .executor(new com.muzlik.fragment.ability.executors.water.TidalShieldExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("water_tsunami_wave", FragmentType.WATER)
            .displayName("Tsunami Wave")
            .description("Summons massive wave that pushes enemies back and slows them")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(55)
            .cooldown(18000)
            .executor(new com.muzlik.fragment.ability.executors.water.TsunamiWaveExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_wind_blade", FragmentType.AIR)
            .displayName("Wind Blade")
            .description("Sends razor-sharp wind projectile that pierces enemies, 5 hearts damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(18)
            .cooldown(4000)
            .executor(new com.muzlik.fragment.ability.executors.air.WindBladeExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_gale_step", FragmentType.AIR)
            .displayName("Gale Step")
            .description("Instant dash 10 blocks in facing direction, +60% speed for 4s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(25)
            .cooldown(6000)
            .executor(new com.muzlik.fragment.ability.executors.air.GaleStepExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_tempest_barrage", FragmentType.AIR)
            .displayName("Tempest Barrage")
            .description("Unleashes 8 wind blades in rapid succession, each dealing 3 hearts")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(65)
            .cooldown(22000)
            .executor(new com.muzlik.fragment.ability.executors.air.TempestBarrageExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_cyclone_armor", FragmentType.AIR)
            .displayName("Cyclone Armor")
            .description("Surrounds self with wind barrier that reflects projectiles and damages melee attackers")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(45)
            .cooldown(15000)
            .rankRequirement(4)
            .executor(new com.muzlik.fragment.ability.executors.air.CycloneArmorExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("air_storm_sovereign", FragmentType.AIR)
            .displayName("Storm Sovereign")
            .description("§7Fly freely for §f10 minutes§7, +100% speed. §8Hold sneak+left click 10s to cancel (no cooldown)")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(90)
            .cooldown(75000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.air.StormSovereignExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("earth_stone_fist", FragmentType.EARTH)
            .displayName("Stone Fist")
            .description("Powerful earth-enhanced punch, 7 hearts damage + knockback")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(20)
            .cooldown(5000)
            .executor(new com.muzlik.fragment.ability.executors.earth.StoneFistExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("earth_earthen_fortress", FragmentType.EARTH)
            .displayName("Earthen Fortress")
            .description("+8 armor points for 8 seconds, reduces damage by 40%")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(30)
            .cooldown(10000)
            .executor(new com.muzlik.fragment.ability.executors.earth.EarthenFortressExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("earth_seismic_slam", FragmentType.EARTH)
            .displayName("Seismic Slam")
            .description("Slams ground creating 7-block radius shockwave, 6 hearts damage + stun 2s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(50)
            .cooldown(16000)
            .executor(new com.muzlik.fragment.ability.executors.earth.SeismicSlamExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_shadow_strike", FragmentType.DARK)
            .displayName("Shadow Strike")
            .description("Dark bolt that curses enemies, 4 hearts + Wither II for 6s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(22)
            .cooldown(6000)
            .executor(new com.muzlik.fragment.ability.executors.dark.ShadowStrikeExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_vampiric_drain", FragmentType.DARK)
            .displayName("Vampiric Drain")
            .description("Drains 5 hearts from target, heals caster for 70% of damage dealt")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(40)
            .cooldown(12000)
            .executor(new com.muzlik.fragment.ability.executors.dark.VampiricDrainExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_abyssal_void", FragmentType.DARK)
            .displayName("Abyssal Void")
            .description("Creates 8-block dark zone: enemies take Wither III, Slowness III, Weakness II for 10s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(70)
            .cooldown(25000)
            .executor(new com.muzlik.fragment.ability.executors.dark.AbyssalVoidExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_shadow_clone", FragmentType.DARK)
            .displayName("Shadow Clone")
            .description("Creates 2 shadow clones that mimic your attacks for 15s, each dealing 50% damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(55)
            .cooldown(20000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.dark.ShadowCloneExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dark_eternal_darkness", FragmentType.DARK)
            .displayName("Eternal Darkness")
            .description("Become shadow incarnate for 15s: invisible, +75% damage, life steal on all attacks")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(95)
            .cooldown(80000)
            .rankRequirement(6)
            .executor(new com.muzlik.fragment.ability.executors.dark.EternalDarknessExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_radiant_lance", FragmentType.LIGHT)
            .displayName("Radiant Lance")
            .description("Shoots concentrated light beam that pierces through enemies, 6 hearts + Blindness 4s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(25)
            .cooldown(5000)
            .executor(new com.muzlik.fragment.ability.executors.light.RadiantLanceExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_divine_blessing", FragmentType.LIGHT)
            .displayName("Divine Blessing")
            .description("Heals 5 hearts, removes all negative effects, grants Regeneration II for 8s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(35)
            .cooldown(10000)
            .executor(new com.muzlik.fragment.ability.executors.light.DivineBlessingExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_celestial_judgment", FragmentType.LIGHT)
            .displayName("Celestial Judgment")
            .description("Summons pillar of light from sky, 7-block radius, 8 hearts damage to undead/dark, 5 hearts to others")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(65)
            .cooldown(20000)
            .executor(new com.muzlik.fragment.ability.executors.light.CelestialJudgmentExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        // Advanced Abilities (Unlock via Rank-Up) - Slots 3, 4
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_holy_sanctuary", FragmentType.LIGHT)
            .displayName("Holy Sanctuary")
            .description("Creates 6-block holy zone for 12s: allies heal 1 heart/sec, enemies take 2 hearts/sec")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ADVANCED)
            .manaCost(50)
            .cooldown(18000)
            .rankRequirement(5)
            .executor(new com.muzlik.fragment.ability.executors.light.HolySanctuaryExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("light_seraph_ascension", FragmentType.LIGHT)
            .displayName("Seraph Ascension")
            .description("Become angelic being for 15s: flight, all healing doubled, immune to debuffs, +60% damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.MASTERY)
            .manaCost(100)
            .cooldown(85000)
            .rankRequirement(6)
            .executor(new com.muzlik.fragment.ability.executors.light.SeraphAscensionExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("void_void_slash", FragmentType.VOID)
            .displayName("Void Slash")
            .description("Tears space itself, dealing true damage that bypasses defenses, 8 hearts (ignores armor)")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(32)
            .cooldown(5000)
            .executor(new com.muzlik.fragment.ability.executors.voidfrag.VoidSlashExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("void_blink_step", FragmentType.VOID)
            .displayName("Blink Step")
            .description("Instantly teleport up to 15 blocks in facing direction, leaves void rift that damages enemies")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(35)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.voidfrag.BlinkStepExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("void_dimensional_collapse", FragmentType.VOID)
            .displayName("Dimensional Collapse")
            .description("Creates black hole at target location, pulls all enemies within 10 blocks, 10 hearts damage + stun 3s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(85)
            .cooldown(28000)
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.voidfrag.DimensionalCollapseExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("mob_beast_summon", FragmentType.MOB)
            .displayName("Beast Summon")
            .description("Summons wolf pack (3 wolves) that fight for 30s, scales with rank")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(30)
            .cooldown(8000)
            .executor(new com.muzlik.fragment.ability.executors.mob.BeastSummonExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("mob_iron_golem_guardian", FragmentType.MOB)
            .displayName("Iron Golem Guardian")
            .description("Summons Iron Golem that protects caster for 45s, taunts enemies")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(45)
            .cooldown(15000)
            .executor(new com.muzlik.fragment.ability.executors.mob.IronGolemGuardianExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("mob_legion_of_shadows", FragmentType.MOB)
            .displayName("Legion of Shadows")
            .description("Summons 5 skeleton warriors + 2 zombie brutes that fight for 60s")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(80)
            .cooldown(30000)
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.mob.LegionOfShadowsExecutor())
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_dragons_roar", FragmentType.DRAGON)
            .displayName("Dragon's Roar")
            .description("Unleashes devastating dragon breath in 12-block cone, ignites enemies, 9 hearts damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(40)
            .cooldown(6000)
            .executor(new com.muzlik.fragment.ability.executors.dragon.DragonsRoarExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_draconic_wings", FragmentType.DRAGON)
            .displayName("Draconic Wings")
            .description("§7Fly freely for §530 minutes§7, +80% speed, immune to fall damage. §8Hold sneak+left click 10s to cancel (no cooldown)")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(50)
            .cooldown(12000)
            .executor(new com.muzlik.fragment.ability.executors.dragon.DraconicWingsExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("dragon_cataclysm", FragmentType.DRAGON)
            .displayName("Cataclysm")
            .description("Transform into dragon form for 10s: all abilities cost 0 mana, +100% damage, AOE attacks")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(100)
            .cooldown(35000)
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.dragon.CataclysmExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Note: DRAGON Fragment unlocks Slot 3 at Rank 10, Slot 4 at Rank 12 (not implemented yet)
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
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("storm_lightning_bolt", FragmentType.STORM)
            .displayName("Lightning Bolt")
            .description("Summons lightning strike on target location, instant cast, 7 hearts damage")
            .slot(com.muzlik.fragment.ability.AbilitySlot.PRIMARY)
            .manaCost(28)
            .cooldown(4000)
            .executor(new com.muzlik.fragment.ability.executors.storm.LightningBoltExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("storm_chain_lightning", FragmentType.STORM)
            .displayName("Chain Lightning")
            .description("Lightning chains between up to 5 enemies within 8 blocks, 5 hearts per target")
            .slot(com.muzlik.fragment.ability.AbilitySlot.SECONDARY)
            .manaCost(40)
            .cooldown(10000)
            .executor(new com.muzlik.fragment.ability.executors.storm.ChainLightningExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
            
        builder.addAbility(new com.muzlik.fragment.ability.AbilityDefinition.Builder("storm_thunderstorm_descent", FragmentType.STORM)
            .displayName("Thunderstorm Descent")
            .description("Calls down 10 lightning strikes in 10-block radius over 5 seconds, each 6 hearts")
            .slot(com.muzlik.fragment.ability.AbilitySlot.ULTIMATE)
            .manaCost(75)
            .cooldown(24000)
            // FIXED: Ultimate slot should be available from rank 1
            .executor(new com.muzlik.fragment.ability.executors.storm.ThunderstormDescentExecutor())
            .activation(com.muzlik.fragment.ability.ActivationType.RIGHT_CLICK)
            .build());
        
        // Note: STORM Fragment unlocks Slot 3 at Rank 8, Slot 4 at Rank 10 (not implemented yet)
        fragmentManager.registerFragment(builder.build());
    }
}
