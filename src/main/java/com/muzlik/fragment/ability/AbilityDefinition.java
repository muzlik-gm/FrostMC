package com.muzlik.fragment.ability;

import com.muzlik.fragment.FragmentType;

/**
 * Defines an ability's properties and requirements.
 * Immutable data class for ability metadata.
 */
public class AbilityDefinition {
    private final String id;
    private final String displayName;
    private final String description;
    private final FragmentType fragmentType;
    private final AbilitySlot slot;
    private final double manaCost;
    private final long cooldown; // in milliseconds
    private final int levelRequirement;
    private final int rankRequirement;
    private final ActivationType activation;
    private final AbilityExecutor executor;

    private AbilityDefinition(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.fragmentType = builder.fragmentType;
        this.slot = builder.slot;
        this.manaCost = builder.manaCost;
        this.cooldown = builder.cooldown;
        this.levelRequirement = builder.levelRequirement;
        this.rankRequirement = builder.rankRequirement;
        this.activation = builder.activation;
        this.executor = builder.executor;
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public FragmentType getFragmentType() { return fragmentType; }
    public AbilitySlot getSlot() { return slot; }
    public double getManaCost() { return manaCost; }
    public long getCooldown() { return cooldown; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getRankRequirement() { return rankRequirement; }
    public ActivationType getActivation() { return activation; }
    public AbilityExecutor getExecutor() { return executor; }

    /**
     * Builder for AbilityDefinition
     */
    public static class Builder {
        private String id;
        private String displayName;
        private String description;
        private FragmentType fragmentType;
        private AbilitySlot slot;
        private double manaCost;
        private long cooldown;
        private int levelRequirement;
        private int rankRequirement;
        private ActivationType activation;
        private AbilityExecutor executor;

        public Builder(String id, FragmentType fragmentType) {
            this.id = id;
            this.fragmentType = fragmentType;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder slot(AbilitySlot slot) {
            this.slot = slot;
            return this;
        }

        public Builder manaCost(double manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder cooldown(long cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder levelRequirement(int levelRequirement) {
            this.levelRequirement = levelRequirement;
            return this;
        }

        public Builder rankRequirement(int rankRequirement) {
            this.rankRequirement = rankRequirement;
            return this;
        }

        public Builder activation(ActivationType activation) {
            this.activation = activation;
            return this;
        }

        public Builder executor(AbilityExecutor executor) {
            this.executor = executor;
            return this;
        }

        public AbilityDefinition build() {
            if (id == null || displayName == null || fragmentType == null || 
                slot == null || activation == null || executor == null) {
                throw new IllegalStateException("Required fields must be set");
            }
            return new AbilityDefinition(this);
        }
    }
}
