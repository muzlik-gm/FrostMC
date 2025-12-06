package com.muzlik.fragment.ability;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.ability.executors.fire.*;
import com.muzlik.fragment.ability.executors.water.*;
import com.muzlik.fragment.ability.executors.air.*;
import com.muzlik.fragment.ability.executors.earth.*;

import java.util.*;

/**
 * Registry for all Fragment abilities
 */
public class FragmentAbilityRegistry {
    private final Map<FragmentType, List<AbilityDefinition>> abilities = new HashMap<>();
    
    public FragmentAbilityRegistry() {
        registerAllAbilities();
    }
    
    private void registerAllAbilities() {
        registerFireAbilities();
        registerWaterAbilities();
        registerAirAbilities();
        registerEarthAbilities();
        // Dark, Light, Storm, Void, Mob, Dragon would be registered here
    }
    
    private void registerFireAbilities() {
        List<AbilityDefinition> fireAbilities = new ArrayList<>();
        
        fireAbilities.add(new AbilityDefinition.Builder("fire_flame_burst", FragmentType.FIRE)
            .displayName("Flame Burst")
            .description("Shoots fireball that explodes on impact")
            .slot(AbilitySlot.PRIMARY)
            .manaCost(20)
            .cooldown(5000)
            .executor(new FlameBurstExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        fireAbilities.add(new AbilityDefinition.Builder("fire_dome", FragmentType.FIRE)
            .displayName("Fire Dome")
            .description("Protective dome of fire that burns enemies")
            .slot(AbilitySlot.SECONDARY)
            .manaCost(25)
            .cooldown(12000)
            .executor(new FireDomeExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        fireAbilities.add(new AbilityDefinition.Builder("fire_inferno_maelstrom", FragmentType.FIRE)
            .displayName("Inferno Maelstrom")
            .description("Creates massive fire vortex")
            .slot(AbilitySlot.ULTIMATE)
            .manaCost(60)
            .cooldown(20000)
            .executor(new InfernoMaelstromExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        fireAbilities.add(new AbilityDefinition.Builder("fire_phoenix_rebirth", FragmentType.FIRE)
            .displayName("Phoenix Rebirth")
            .description("Revive upon death")
            .slot(AbilitySlot.ADVANCED)
            .manaCost(80)
            .cooldown(60000)
            .rankRequirement(5)
            .executor(new PhoenixRebirthExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        fireAbilities.add(new AbilityDefinition.Builder("fire_eternal_flame", FragmentType.FIRE)
            .displayName("Eternal Flame")
            .description("Become living flame")
            .slot(AbilitySlot.MASTERY)
            .manaCost(100)
            .cooldown(90000)
            .rankRequirement(7)
            .executor(new EternalFlameExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        abilities.put(FragmentType.FIRE, fireAbilities);
    }
    
    private void registerWaterAbilities() {
        List<AbilityDefinition> waterAbilities = new ArrayList<>();
        
        waterAbilities.add(new AbilityDefinition.Builder("water_aqua_pulse", FragmentType.WATER)
            .displayName("Aqua Pulse")
            .description("AOE healing sphere")
            .slot(AbilitySlot.PRIMARY)
            .manaCost(25)
            .cooldown(8000)
            .executor(new AquaPulseExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        waterAbilities.add(new AbilityDefinition.Builder("water_tidal_shield", FragmentType.WATER)
            .displayName("Tidal Shield")
            .description("Water barrier absorbs damage")
            .slot(AbilitySlot.SECONDARY)
            .manaCost(35)
            .cooldown(12000)
            .executor(new TidalShieldExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        waterAbilities.add(new AbilityDefinition.Builder("water_tsunami_wave", FragmentType.WATER)
            .displayName("Tsunami Wave")
            .description("Massive wave knockback")
            .slot(AbilitySlot.ULTIMATE)
            .manaCost(55)
            .cooldown(18000)
            .executor(new TsunamiWaveExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        abilities.put(FragmentType.WATER, waterAbilities);
    }
    
    private void registerAirAbilities() {
        List<AbilityDefinition> airAbilities = new ArrayList<>();
        
        airAbilities.add(new AbilityDefinition.Builder("air_wind_blade", FragmentType.AIR)
            .displayName("Wind Blade")
            .description("Piercing wind projectile")
            .slot(AbilitySlot.PRIMARY)
            .manaCost(18)
            .cooldown(4000)
            .executor(new WindBladeExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        airAbilities.add(new AbilityDefinition.Builder("air_gale_step", FragmentType.AIR)
            .displayName("Gale Step")
            .description("Instant dash with speed boost")
            .slot(AbilitySlot.SECONDARY)
            .manaCost(25)
            .cooldown(6000)
            .executor(new GaleStepExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        airAbilities.add(new AbilityDefinition.Builder("air_tempest_barrage", FragmentType.AIR)
            .displayName("Tempest Barrage")
            .description("Rapid wind blade barrage")
            .slot(AbilitySlot.ULTIMATE)
            .manaCost(65)
            .cooldown(22000)
            .executor(new TempestBarrageExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        airAbilities.add(new AbilityDefinition.Builder("air_cyclone_armor", FragmentType.AIR)
            .displayName("Cyclone Armor")
            .description("Wind barrier reflects projectiles")
            .slot(AbilitySlot.ADVANCED)
            .manaCost(45)
            .cooldown(15000)
            .rankRequirement(5)
            .executor(new CycloneArmorExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        airAbilities.add(new AbilityDefinition.Builder("air_storm_sovereign", FragmentType.AIR)
            .displayName("Storm Sovereign")
            .description("Command the winds")
            .slot(AbilitySlot.MASTERY)
            .manaCost(90)
            .cooldown(75000)
            .rankRequirement(7)
            .executor(new StormSovereignExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        abilities.put(FragmentType.AIR, airAbilities);
    }
    
    private void registerEarthAbilities() {
        List<AbilityDefinition> earthAbilities = new ArrayList<>();
        
        earthAbilities.add(new AbilityDefinition.Builder("earth_stone_fist", FragmentType.EARTH)
            .displayName("Stone Fist")
            .description("Powerful earth punch")
            .slot(AbilitySlot.PRIMARY)
            .manaCost(20)
            .cooldown(5000)
            .executor(new StoneFistExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        earthAbilities.add(new AbilityDefinition.Builder("earth_earthen_fortress", FragmentType.EARTH)
            .displayName("Earthen Fortress")
            .description("Massive armor boost")
            .slot(AbilitySlot.SECONDARY)
            .manaCost(30)
            .cooldown(10000)
            .executor(new EarthenFortressExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        earthAbilities.add(new AbilityDefinition.Builder("earth_seismic_slam", FragmentType.EARTH)
            .displayName("Seismic Slam")
            .description("Ground shockwave")
            .slot(AbilitySlot.ULTIMATE)
            .manaCost(50)
            .cooldown(16000)
            .executor(new SeismicSlamExecutor())
            .activation(ActivationType.RIGHT_CLICK)
            .build());
            
        abilities.put(FragmentType.EARTH, earthAbilities);
    }
    
    public List<AbilityDefinition> getAbilities(FragmentType fragmentType) {
        return abilities.getOrDefault(fragmentType, new ArrayList<>());
    }
    
    public AbilityDefinition getAbility(FragmentType fragmentType, int slotIndex) {
        List<AbilityDefinition> fragmentAbilities = getAbilities(fragmentType);
        for (AbilityDefinition ability : fragmentAbilities) {
            if (ability.getSlot().getSlotIndex() == slotIndex) {
                return ability;
            }
        }
        return null;
    }
}
