package com.muzlik.ritual;

/**
 * Enum representing the stages of a ritual progression.
 */
public enum RitualStage {
    CHARGING(0, 33),      // 0-33% progress
    ACTIVATION(33, 66),   // 33-66% progress
    COMPLETION(66, 100);  // 66-100% progress

    private final int startPercent;
    private final int endPercent;

    RitualStage(int startPercent, int endPercent) {
        this.startPercent = startPercent;
        this.endPercent = endPercent;
    }

    public int getStartPercent() {
        return startPercent;
    }

    public int getEndPercent() {
        return endPercent;
    }

    /**
     * Get the ritual stage based on progress percentage
     */
    public static RitualStage fromProgress(int progressPercent) {
        if (progressPercent < 33) {
            return CHARGING;
        } else if (progressPercent < 66) {
            return ACTIVATION;
        } else {
            return COMPLETION;
        }
    }
}
