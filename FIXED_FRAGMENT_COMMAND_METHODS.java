    /**
     * Show compact, beautiful Fragment info (FIXED VERSION)
     */
    private void showFragmentInfo(Player player) {
        FragmentType activeFragment = fragmentManager.getActiveFragment(player);
        
        if (activeFragment == null) {
            sendStyledMessage(player, "&c✗ No Fragment active");
            sendStyledMessage(player, "&7Use &e/fragment list &7to activate one");
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
        
        // Get fragment color
        String fragColor = getFragmentColor(activeFragment);
        String rankBadge = getModernRankBadge(rank, maxRank);
        
        // Calculate progress bars
        double xpPercent = xpRequired > 0 ? (xp / xpRequired) : 1.0;
        double manaPercent = maxMana > 0 ? (currentMana / maxMana) : 1.0;
        String xpBar = buildModernProgressBar(xpPercent, 12, "&a", "&8");
        String manaBar = buildModernProgressBar(manaPercent, 12, "&b", "&8");
        
        // Compact header
        player.sendMessage("");
        sendStyledMessage(player, "&8╔═══════════════════════════════════════╗");
        sendStyledMessage(player, "&8║  " + fragColor + "&l" + activeFragment.getDisplayName().toUpperCase() + " FRAGMENT &r&8║");
        sendStyledMessage(player, "&8╚═══════════════════════════════════════╝");
        
        // Rank & Level (single line)
        sendStyledMessage(player, "&7Rank: " + rankBadge + " &f" + rank + "&8/&7" + maxRank + 
                         " &8│ &7Level: &e" + level + "&8/&7" + maxLevel);
        
        // XP Progress (compact)
        if (level < maxLevel) {
            sendStyledMessage(player, "&7XP: " + xpBar + " &e" + String.format("%.0f", xp) + "&8/&e" + String.format("%.0f", xpRequired));
        } else {
            sendStyledMessage(player, "&7XP: &6&l✦ MAX LEVEL ✦");
        }
        
        // Mana (compact)
        sendStyledMessage(player, "&7Mana: " + manaBar + " &b" + String.format("%.0f", currentMana) + "&8/&b" + String.format("%.0f", maxMana));
        
        // Abilities count (single line)
        com.muzlik.fragment.FragmentDefinition fragment = fragmentManager.getFragment(activeFragment);
        if (fragment != null) {
            int unlockedCount = 0;
            int totalCount = fragment.getAbilities().size();
            
            for (com.muzlik.fragment.ability.AbilityDefinition ability : fragment.getAbilities()) {
                boolean isUnlocked = rank >= ability.getRankRequirement() && level >= ability.getLevelRequirement();
                if (isUnlocked) unlockedCount++;
            }
            
            String abilityStatus = unlockedCount == totalCount ? "&a" + unlockedCount + "&8/&a" + totalCount + " &7✓" : 
                                  "&e" + unlockedCount + "&8/&7" + totalCount;
            sendStyledMessage(player, "&7Abilities: " + abilityStatus + " &8│ &7Use &e/fragment abilities &7for details");
        }
        
        player.sendMessage("");
    }