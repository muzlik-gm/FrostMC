# FrostSMP Fragment System - Texture Mapping

This document lists all custom model data IDs and their corresponding textures.

## Fragment Icons (1000-1099)
Place textures in: assets/minecraft/textures/item/fragments/

- 1005: light.png (Fragment: Light)
- 1006: void.png (Fragment: Void)
- 1003: earth.png (Fragment: Earth)
- 1007: mob.png (Fragment: Mob)
- 1000: fire.png (Fragment: Fire)
- 1001: water.png (Fragment: Water)
- 1002: air.png (Fragment: Air)
- 1004: dark.png (Fragment: Dark)
- 1009: storm.png (Fragment: Storm)
- 1008: dragon.png (Fragment: Dragon)

## Ability Icons (2000-2999)
Place textures in: assets/minecraft/textures/item/abilities/

- 2802: dragon_dragon_meteor.png
- 2951: ability_secondary.png
- 2303: earth_terra_shaper.png
- 2002: fire_inferno_maelstrom.png
- 2401: dark_vampiric_drain.png
- 2954: ability_mastery.png
- 2404: dark_eternal_darkness.png
- 2800: dragon_dragons_roar.png
- 2801: dragon_draconic_wings.png
- 2204: air_storm_sovereign.png
- 2301: earth_seismic_slam.png
- 2302: earth_earthen_fortress.png
- 2950: ability_primary.png
- 2005: fire_fire_dome.png
- 2955: ability_locked.png
- 2903: storm_thunderstorm_descent.png
- 2300: earth_stone_fist.png
- 2703: mob_legion_of_shadows.png
- 2900: storm_lightning_bolt.png
- 2902: storm_storm_surge.png
- 2504: light_seraph_ascension.png
- 2701: mob_pack_call.png
- 2004: fire_eternal_flame.png
- 2603: void_spatial_manipulation.png
- 2001: fire_blazing_step.png
- 2100: water_aqua_pulse.png
- 2803: dragon_cataclysm.png
- 2601: void_blink_step.png
- 2003: fire_phoenix_rebirth.png
- 2901: storm_chain_lightning.png
- 2700: mob_beast_summon.png
- 2101: water_tidal_shield.png
- 2952: ability_ultimate.png
- 2953: ability_advanced.png
- 2502: light_celestial_judgment.png
- 2202: air_tempest_barrage.png
- 2602: void_dimensional_collapse.png
- 2702: mob_iron_golem_guardian.png
- 2203: air_cyclone_armor.png
- 2102: water_tsunami_wave.png
- 2503: light_holy_sanctuary.png
- 2600: void_void_slash.png
- 2402: dark_abyssal_void.png
- 2500: light_radiant_lance.png
- 2201: air_gale_step.png
- 2400: dark_shadow_strike.png
- 2200: air_wind_blade.png
- 2956: ability_cooldown.png
- 2403: dark_shadow_clone.png
- 2501: light_divine_blessing.png
- 2000: fire_flame_burst.png

## Mana Indicators (3000-3099)
Place textures in: assets/minecraft/textures/item/mana/

- 3002: mana_medium.png
- 3000: mana_full.png
- 3006: mana_icon.png
- 3005: mana_flask.png
- 3003: mana_low.png
- 3004: mana_empty.png
- 3001: mana_high.png

## UI Elements (4000-4099)
Place textures in: assets/minecraft/textures/item/ui/

- 4010: ui_rank_badge_bronze.png
- 4021: ui_border_bottom.png
- 4001: ui_info_button.png
- 4020: ui_border_top.png
- 4013: ui_rank_badge_diamond.png
- 4005: ui_warning_icon.png
- 4022: ui_border_left.png
- 4002: ui_close_button.png
- 4012: ui_rank_badge_gold.png
- 4000: ui_back_button.png
- 4004: ui_unlocked_icon.png
- 4011: ui_rank_badge_silver.png
- 4014: ui_rank_badge_master.png
- 4003: ui_locked_icon.png
- 4023: ui_border_right.png

## How to Create Textures

1. Create 16x16 PNG images for each texture
2. Place them in the appropriate folders listed above
3. Create corresponding .json model files (see generated paper.json for examples)
4. Zip the entire 'resourcepack' folder
5. Distribute to players or host on a server

## Model File Template

Each texture needs a corresponding model file. Example for fire.json:

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/fragments/fire"
  }
}
```
