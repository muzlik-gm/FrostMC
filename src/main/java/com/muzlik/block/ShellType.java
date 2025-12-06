package com.muzlik.block;

/**
 * Type of visual shell for block controllers.
 * Per SYSTEM.md specification.
 */
public enum ShellType {
    /**
     * Particle-based shell (preferred - safe, no world modification)
     * Uses BLOCK_CRACK particles to simulate moving block
     */
    PARTICLE_SHELL,
    
    /**
     * Real falling block entity (use sparingly)
     * Must be flagged tempFalling=true
     */
    FALLING_BLOCK
}
