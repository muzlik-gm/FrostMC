package com.muzlik.fragment.ability;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a transformation effect with duration and properties
 */
public class TransformationEffect {
    private final String id;
    private final long duration; // in ticks
    private final Map<String, Object> effects;
    
    public TransformationEffect(String id, long duration) {
        this.id = id;
        this.duration = duration;
        this.effects = new HashMap<>();
    }
    
    public String getId() {
        return id;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public Map<String, Object> getEffects() {
        return effects;
    }
    
    public void addEffect(String key, Object value) {
        effects.put(key, value);
    }
    
    public Object getEffect(String key) {
        return effects.get(key);
    }
    
    public boolean hasEffect(String key) {
        return effects.containsKey(key);
    }
}
