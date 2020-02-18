package com.cavetale.skills;

import java.util.HashMap;
import lombok.NonNull;

public enum Talent {
    // Mining
    MINE_STRIP,
    MINE_ORE_ALERT,
    MINE_XRAY,
    MINE_SILK_STRIP,
    MINE_SILK_MULTI,
    // Farming
    FARM_GROWSTICK_RADIUS,
    FARM_PLANT_RADIUS,
    FARM_CROP_DROPS,
    FARM_DIAMOND_DROPS,
    FARM_TALENT_POINTS,
    // Combat
    COMBAT_FIRE,
    COMBAT_SILENCE,
    COMBAT_SPIDERS,
    COMBAT_GOD_MODE,
    COMBAT_ARCHER_ZONE;

    public final String key;
    public final String displayName;
    Talent depends = null;
    SkillType skill = null;
    boolean terminal = true;
    private static final HashMap<String, Talent> KEY_MAP = new HashMap<>();

    static {
        chain(MINE_STRIP,
              MINE_ORE_ALERT,
              MINE_XRAY);
        chain(MINE_STRIP,
              MINE_SILK_STRIP,
              MINE_SILK_MULTI);
        chain(FARM_GROWSTICK_RADIUS,
              FARM_PLANT_RADIUS);
        chain(FARM_GROWSTICK_RADIUS,
              FARM_CROP_DROPS,
              FARM_DIAMOND_DROPS,
              FARM_TALENT_POINTS);
        chain(COMBAT_FIRE,
              COMBAT_SILENCE,
              COMBAT_SPIDERS,
              COMBAT_GOD_MODE);
        chain(COMBAT_FIRE,
              COMBAT_ARCHER_ZONE);
        for (Talent talent : values()) {
            KEY_MAP.put(talent.key, talent);
            KEY_MAP.put(talent.key.replace("_", ""), talent);
        }
    }

    private static void chain(Talent... talents) {
        for (int i = 0; i < talents.length - 1; i += 1) {
            talents[i + 1].depends = talents[i];
            talents[i].terminal = false;
        }
    }

    Talent() {
        key = name().toLowerCase();
        this.displayName = Util.niceEnumName(this);
    }

    public static Talent of(@NonNull String key) {
        return KEY_MAP.get(key);
    }
}
