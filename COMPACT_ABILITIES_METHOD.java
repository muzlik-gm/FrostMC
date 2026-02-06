    /**
     * Show compact abilities list (FIXED VERSION - Like fragment info)
     */
    private void showAbilitiesList(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            player.sendMessage("§c✗ No Fragment active");
            player.sendMessage("§7Use §e/fragment list §7to activate one");
            return;
        }
        
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(activeFragment);
        if (fragment == null) {
            player.sendMessage("§c✗ Fragment data not found");
            return;
        }
        
        int rank = rankManager.getRank(player, activeFragment);
        int level = levelManager.getLevel(player, activeFragment);
        
        // Get fragment color
        String fragColor = "§c";
        switch (activeFragment) {
            case FIRE: fragColor = "§c"; break;
            case WATER: fragColor = "§b"; break;
            case AIR: fragColor = "§f"; break;
            case DARK: fragColor = "§8"; break;
            case LIGHT: fragColor = "§e"; break;
            case VOID: fragColor = "§5"; break;
            case STORM: fragColor = "§9"; break;
            case DRAGON: fragColor = "§6"; break;
            case TIME: fragColor = "§d"; break;
            case LUCK: fragColor = "§a"; break;
        }
        
        // Compact header
        player.sendMessage("");
        player.sendMessage("§8╔═══════════════════════════════════════╗");
        player.sendMessage("§8║  " + fragColor + "§l" + activeFragment.getDisplayName().toUpperCase() + " ABILITIES §r§8║");
        player.sendMessage("§8╚═══════════════════════════════════════╝");
        
        // Compact abilities list (single line per ability)
        for (com.muzlik.fragment.ability.AbilityDefinition ability : fragment.getAbilities()) {
            boolean isUnlocked = rank >= ability.getRankRequirement() && level >= ability.getLevelRequirement();
            boolean isOnCooldown = cooldownManager.isOnCooldown(player, ability.getId());
            
            if (isUnlocked) {
                String statusSymbol = isOnCooldown ? "§e⏳" : "§a✓";
                String cooldownText = isOnCooldown ? " §c(" + String.format("%.1f", cooldownManager.getRemainingCooldownSeconds(player, ability.getId())) + "s)" : "";
                
                player.sendMessage(statusSymbol + " §b" + ability.getDisplayName() + " §8[" + ability.getSlot().getSlotIndex() + "] " + 
                                 "§7Mana: §b" + String.format("%.0f", ability.getManaCost()) + cooldownText);
            } else {
                String statusSymbol = "§c✗";
                int rankReq = ability.getRankRequirement();
                int levelReq = ability.getLevelRequirement();
                
                String requirement = "";
                if (rank < rankReq && level < levelReq) {
                    requirement = " §8(Need Rank " + rankReq + " & Level " + levelReq + ")";
                } else if (rank < rankReq) {
                    requirement = " §8(Need Rank " + rankReq + ")";
                } else if (level < levelReq) {
                    requirement = " §8(Need Level " + levelReq + ")";
                }
                
                player.sendMessage(statusSymbol + " §7" + ability.getDisplayName() + " §8[" + ability.getSlot().getSlotIndex() + "]" + requirement);
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§7Use §eSneak + Right/Left Click §7with hotbar slots 0-4");
        player.sendMessage("");
    }