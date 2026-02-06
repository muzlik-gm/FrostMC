    /**
     * Show compact, beautiful Fragment info (FIXED VERSION)
     */
    private void showFragmentInfo(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            player.sendMessage("§c✗ No Fragment active");
            player.sendMessage("§7Use §e/fragment list §7to activate one");
            return;
        }
        
        // Get stats
        int rank = rankManager.getRank(player, activeFragment);
        int maxRank = rankManager.getMaxRank(activeFragment);
        int level = levelManager.getLevel(player, activeFragment);
        int maxLevel = levelManager.getMaxLevel(activeFragment);
        double xp = levelManager.getXP(player, activeFragment);
        double xpRequired = levelManager.getXPForNextLevel(player, activeFragment);
        double currentMana = manaManager.getMana(player);
        double maxMana = manaManager.getMaxMana(player);
        
        // Calculate progress bars (simple version)
        double xpPercent = xpRequired > 0 ? (xp / xpRequired) : 1.0;
        double manaPercent = maxMana > 0 ? (currentMana / maxMana) : 1.0;
        
        // Build simple progress bars
        int xpFilled = (int) (xpPercent * 10);
        int manaFilled = (int) (manaPercent * 10);
        String xpBar = "§a" + "█".repeat(xpFilled) + "§8" + "░".repeat(10 - xpFilled);
        String manaBar = "§b" + "█".repeat(manaFilled) + "§8" + "░".repeat(10 - manaFilled);
        
        // Get fragment color
        String fragColor = "§c"; // Default to red, will be overridden
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
        
        // Rank badge
        String rankBadge = rank >= maxRank ? "§6★" : "§7◆";
        
        // Compact display (6 lines total)
        player.sendMessage("");
        player.sendMessage("§8╔═══════════════════════════════════════╗");
        player.sendMessage("§8║  " + fragColor + "§l" + activeFragment.getDisplayName().toUpperCase() + " FRAGMENT §r§8║");
        player.sendMessage("§8╚═══════════════════════════════════════╝");
        
        // Rank & Level (single line)
        player.sendMessage("§7Rank: " + rankBadge + " §f" + rank + "§8/§7" + maxRank + 
                         " §8│ §7Level: §e" + level + "§8/§7" + maxLevel);
        
        // XP Progress (compact)
        if (level < maxLevel) {
            player.sendMessage("§7XP: " + xpBar + " §e" + String.format("%.0f", xp) + "§8/§e" + String.format("%.0f", xpRequired));
        } else {
            player.sendMessage("§7XP: §6§l✦ MAX LEVEL ✦");
        }
        
        // Mana (compact)
        player.sendMessage("§7Mana: " + manaBar + " §b" + String.format("%.0f", currentMana) + "§8/§b" + String.format("%.0f", maxMana));
        
        // Abilities count (single line)
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(activeFragment);
        if (fragment != null) {
            int unlockedCount = 0;
            int totalCount = fragment.getAbilities().size();
            
            for (com.muzlik.fragment.ability.AbilityDefinition ability : fragment.getAbilities()) {
                boolean isUnlocked = rank >= ability.getRankRequirement() && level >= ability.getLevelRequirement();
                if (isUnlocked) unlockedCount++;
            }
            
            String abilityStatus = unlockedCount == totalCount ? "§a" + unlockedCount + "§8/§a" + totalCount + " §7✓" : 
                                  "§e" + unlockedCount + "§8/§7" + totalCount;
            player.sendMessage("§7Abilities: " + abilityStatus + " §8│ §7Use §e/fragment abilities §7for details");
        }
        
        player.sendMessage("");
    }