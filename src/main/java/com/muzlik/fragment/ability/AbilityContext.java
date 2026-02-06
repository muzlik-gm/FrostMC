package com.muzlik.fragment.ability;

import com.muzlik.fragment.FragmentType;
import com.muzlik.fragment.PlayerFragmentData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Provides context for ability execution.
 * Contains all relevant information about the player, Fragment, and environment.
 */
public class AbilityContext {
    private final Player player;
    private final AbilityDefinition ability;
    private final PlayerFragmentData fragmentData;
    private int rank;
    private AbilityScalingEngine scalingEngine;

    private final FragmentType fragmentType;
    private final int playerRank;
    private final int playerLevel;
    private final Location location;
    private final Vector direction;
    private final Entity targetEntity;

    /**
     * New constructor for ability execution manager
     */
    public AbilityContext(Player player, AbilityDefinition ability, PlayerFragmentData fragmentData) {
        this.player = player;
        this.ability = ability;
        this.fragmentData = fragmentData;
        this.fragmentType = ability.getFragmentType();
        this.playerRank = 0; // Will be set by execution manager
        this.playerLevel = player.getLevel();
        this.location = player.getLocation();
        this.direction = player.getLocation().getDirection();
        this.targetEntity = null;
    }

    private AbilityContext(Builder builder) {
        this.player = builder.player;
        this.ability = null;
        this.fragmentData = null;
        this.fragmentType = builder.fragmentType;
        this.playerRank = builder.playerRank;
        this.playerLevel = builder.playerLevel;
        this.location = builder.location;
        this.direction = builder.direction;
        this.targetEntity = builder.targetEntity;
    }

    // Getters
    public Player getPlayer() { return player; }
    public AbilityDefinition getAbility() { return ability; }
    public PlayerFragmentData getFragmentData() { return fragmentData; }
    public int getRank() { return rank > 0 ? rank : playerRank; }
    public AbilityScalingEngine getScalingEngine() { return scalingEngine; }
    public FragmentType getFragmentType() { return fragmentType; }
    public int getPlayerRank() { return playerRank; }
    public int getPlayerLevel() { return playerLevel; }
    public Location getLocation() { return location; }
    public Vector getDirection() { return direction; }
    public Entity getTargetEntity() { return targetEntity; }

    // Setters for execution manager
    public void setRank(int rank) { this.rank = rank; }
    public void setScalingEngine(AbilityScalingEngine scalingEngine) { this.scalingEngine = scalingEngine; }

    /**
     * Builder for AbilityContext
     */
    public static class Builder {
        private Player player;
        private FragmentType fragmentType;
        private int playerRank;
        private int playerLevel;
        private Location location;
        private Vector direction;
        private Entity targetEntity;

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder fragmentType(FragmentType fragmentType) {
            this.fragmentType = fragmentType;
            return this;
        }

        public Builder playerRank(int playerRank) {
            this.playerRank = playerRank;
            return this;
        }

        public Builder playerLevel(int playerLevel) {
            this.playerLevel = playerLevel;
            return this;
        }

        public Builder location(Location location) {
            this.location = location;
            return this;
        }

        public Builder direction(Vector direction) {
            this.direction = direction;
            return this;
        }

        public Builder targetEntity(Entity targetEntity) {
            this.targetEntity = targetEntity;
            return this;
        }

        public AbilityContext build() {
            if (player == null || fragmentType == null || location == null || direction == null) {
                throw new IllegalStateException("Required fields must be set");
            }
            return new AbilityContext(this);
        }
    }
}
