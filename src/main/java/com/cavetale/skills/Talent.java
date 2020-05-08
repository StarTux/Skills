package com.cavetale.skills;

import lombok.NonNull;

enum Talent {
    // Mining
    MINE_STRIP(null),
    MINE_ORE_ALERT(Talent.MINE_STRIP),
    MINE_XRAY(Talent.MINE_ORE_ALERT),
    MINE_SILK_STRIP(Talent.MINE_STRIP),
    MINE_SILK_MULTI(Talent.MINE_SILK_STRIP),
    // Farming
    FARM_GROWSTICK_RADIUS(null),
    FARM_PLANT_RADIUS(Talent.FARM_GROWSTICK_RADIUS),
    FARM_CROP_DROPS(Talent.FARM_GROWSTICK_RADIUS),
    FARM_DIAMOND_DROPS(Talent.FARM_CROP_DROPS),
    FARM_TALENT_POINTS(Talent.FARM_DIAMOND_DROPS),
    // Combat
    COMBAT_FIRE(null),
    COMBAT_SILENCE(Talent.COMBAT_FIRE),
    COMBAT_SPIDERS(Talent.COMBAT_SILENCE),
    COMBAT_GOD_MODE(Talent.COMBAT_SPIDERS),
    COMBAT_ARCHER_ZONE(Talent.COMBAT_FIRE);

    public final String key;
    public final Talent depends;
    public final String displayName;
    public final SkillType skill;
    public static final int COUNT = 15;

    Talent(final Talent depends) {
        key = name().toLowerCase();
        this.depends = depends;
        this.displayName = Util.niceEnumName(this);
        if (name().startsWith("MINE")) {
            skill = SkillType.MINING;
        } else if (name().startsWith("FARM")) {
            skill = SkillType.FARMING;
        } else if (name().startsWith("COMBAT")) {
            skill = SkillType.COMBAT;
        } else {
            skill = null;
        }
    }

    static Talent of(@NonNull String key) {
        for (Talent t : Talent.values()) {
            if (key.equals(t.key)) return t;
        }
        return null;
    }

    boolean isTerminal() {
        for (Talent talent : Talent.values()) {
            if (talent.depends == this) return false;
        }
        return true;
    }
}
