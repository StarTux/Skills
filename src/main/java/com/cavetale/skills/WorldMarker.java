package com.cavetale.skills;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;

public enum WorldMarker {
    WATERED_CROP;

    public final String id;
    static final Map<String, WorldMarker> idMap = new HashMap<>();

    static {
        for (WorldMarker marker : WorldMarker.values()) {
            idMap.put(marker.id, marker);
        }
    }

    WorldMarker() {
        id = "skills:" + name().toLowerCase();
    }

    static WorldMarker ofId(@NonNull final String id) {
        return idMap.get(id);
    }
}
