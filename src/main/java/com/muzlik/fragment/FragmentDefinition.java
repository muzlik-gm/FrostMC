package com.muzlik.fragment;

import com.muzlik.fragment.ability.AbilityDefinition;
import com.muzlik.fragment.ability.PassiveAbility;
import com.muzlik.fragment.level.XPCurve;
import com.muzlik.fx.ColorScheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable data class defining a Fragment's properties.
 * Contains all configuration for a Fragment type.
 */
public class FragmentDefinition {
    private final FragmentType type;
    private final String displayName;
    private final String description;
    private final int baseRank;
    private final ColorScheme colorScheme;
    private final XPCurve xpCurve;
    private final double deathXPPenalty;
    private final List<AbilityDefinition> abilities;
    private final Map<Integer, PassiveAbility> rankPassives;
    private final String theme;
    private final int complexityLevel;

    private FragmentDefinition(Builder builder) {
        this.type = builder.type;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.baseRank = builder.baseRank;
        this.colorScheme = builder.colorScheme;
        this.xpCurve = builder.xpCurve;
        this.deathXPPenalty = builder.deathXPPenalty;
        this.abilities = Collections.unmodifiableList(new ArrayList<>(builder.abilities));
        this.rankPassives = Collections.unmodifiableMap(new HashMap<>(builder.rankPassives));
        this.theme = builder.theme;
        this.complexityLevel = builder.complexityLevel;
    }

    // Getters
    public FragmentType getType() { return type; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getBaseRank() { return baseRank; }
    public ColorScheme getColorScheme() { return colorScheme; }
    public XPCurve getXpCurve() { return xpCurve; }
    public double getDeathXPPenalty() { return deathXPPenalty; }
    public List<AbilityDefinition> getAbilities() { return abilities; }
    public Map<Integer, PassiveAbility> getRankPassives() { return rankPassives; }
    public String getTheme() { return theme; }
    public int getComplexityLevel() { return complexityLevel; }

    /**
     * Get the maximum rank for this Fragment (Base Rank + 2)
     */
    public int getMaxRank() {
        return baseRank + 2;
    }

    /**
     * Builder for FragmentDefinition
     */
    public static class Builder {
        private FragmentType type;
        private String displayName;
        private String description;
        private int baseRank;
        private ColorScheme colorScheme;
        private XPCurve xpCurve = XPCurve.LINEAR;
        private double deathXPPenalty = 0.1;
        private List<AbilityDefinition> abilities = new ArrayList<>();
        private Map<Integer, PassiveAbility> rankPassives = new HashMap<>();
        private String theme = "";
        private int complexityLevel = 1;

        public Builder(FragmentType type) {
            this.type = type;
            this.displayName = type.getDisplayName();
            this.description = type.getDescription();
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder baseRank(int baseRank) {
            if (baseRank < 1 || baseRank > 10) {
                throw new IllegalArgumentException("Base rank must be between 1 and 10");
            }
            this.baseRank = baseRank;
            return this;
        }

        public Builder colorScheme(ColorScheme colorScheme) {
            this.colorScheme = colorScheme;
            return this;
        }

        public Builder xpCurve(XPCurve xpCurve) {
            this.xpCurve = xpCurve;
            return this;
        }

        public Builder deathXPPenalty(double deathXPPenalty) {
            if (deathXPPenalty < 0.0 || deathXPPenalty > 1.0) {
                throw new IllegalArgumentException("Death XP penalty must be between 0.0 and 1.0");
            }
            this.deathXPPenalty = deathXPPenalty;
            return this;
        }

        public Builder addAbility(AbilityDefinition ability) {
            this.abilities.add(ability);
            return this;
        }

        public Builder addRankPassive(int rank, PassiveAbility passive) {
            this.rankPassives.put(rank, passive);
            return this;
        }

        public Builder theme(String theme) {
            this.theme = theme;
            return this;
        }

        public Builder complexityLevel(int complexityLevel) {
            if (complexityLevel < 1 || complexityLevel > 10) {
                throw new IllegalArgumentException("Complexity level must be between 1 and 10");
            }
            this.complexityLevel = complexityLevel;
            return this;
        }

        public FragmentDefinition build() {
            if (colorScheme == null) {
                throw new IllegalStateException("ColorScheme must be set");
            }
            if (baseRank < 1 || baseRank > 10) {
                throw new IllegalStateException("Base rank must be set between 1 and 10");
            }
            return new FragmentDefinition(this);
        }
    }
}
