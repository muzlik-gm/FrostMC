package com.muzlik.fragment.ability;

/**
 * Functional interface for executing an ability.
 * Implementations define what happens when an ability is used.
 */
@FunctionalInterface
public interface AbilityExecutor {
    /**
     * Execute the ability with the given context
     * @param context The execution context containing player, Fragment, and environment data
     */
    void execute(AbilityContext context);
}
