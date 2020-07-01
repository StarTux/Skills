package com.cavetale.skills.worldmarker;

public enum MarkerId {
    WATERED_CROP; // "skills:watered_crop"

    public final String key;

    MarkerId() {
        this.key = "skills:" + name().toLowerCase();
    }

    public static MarkerId of(String in) {
        for (MarkerId it : MarkerId.values()) {
            if (it.key.equals(in)) return it;
        }
        return null;
    }

    public boolean is(String in) {
        return key.equals(in);
    }
}
