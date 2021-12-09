package com.cavetale.skills;

import com.cavetale.skills.skill.SkillType;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;

public enum Talent {
    // Mining
    MINE_STRIP(SkillType.MINING, null),
    MINE_ORE_ALERT(SkillType.MINING, Talent.MINE_STRIP),
    MINE_XRAY(SkillType.MINING, Talent.MINE_ORE_ALERT),
    MINE_SILK_STRIP(SkillType.MINING, Talent.MINE_STRIP),
    MINE_SILK_MULTI(SkillType.MINING, Talent.MINE_SILK_STRIP),
    // Farming
    FARM_GROWSTICK_RADIUS(SkillType.FARMING, null),
    FARM_PLANT_RADIUS(SkillType.FARMING, Talent.FARM_GROWSTICK_RADIUS),
    FARM_CROP_DROPS(SkillType.FARMING, Talent.FARM_GROWSTICK_RADIUS),
    FARM_DIAMOND_DROPS(SkillType.FARMING, Talent.FARM_CROP_DROPS),
    FARM_TALENT_POINTS(SkillType.FARMING, Talent.FARM_DIAMOND_DROPS),
    // Combat
    COMBAT_FIRE(SkillType.COMBAT, null),
    COMBAT_SILENCE(SkillType.COMBAT, Talent.COMBAT_FIRE),
    COMBAT_SPIDERS(SkillType.COMBAT, Talent.COMBAT_SILENCE),
    COMBAT_GOD_MODE(SkillType.COMBAT, Talent.COMBAT_SPIDERS),
    COMBAT_ARCHER_ZONE(SkillType.COMBAT, Talent.COMBAT_FIRE);

    public final String key;
    public final SkillType skill;
    public final Talent depends;
    public final String displayName;
    public static final int COUNT = 15;
    public static final Map<SkillType, Set<Talent>> SKILL_MAP = new EnumMap<>(SkillType.class);

    Talent(@NonNull final SkillType skillType, final Talent depends) {
        this.key = name().toLowerCase();
        this.skill = skillType;
        this.depends = depends;
        this.displayName = Util.niceEnumName(this);
    }

    static {
        for (Talent talent : Talent.values()) {
            SKILL_MAP.computeIfAbsent(talent.skill, sk -> EnumSet.of(talent))
                .add(talent);
        }
    }

    public static Talent of(@NonNull String key) {
        for (Talent t : Talent.values()) {
            if (key.equals(t.key)) return t;
        }
        return null;
    }

    public boolean isTerminal() {
        for (Talent talent : Talent.values()) {
            if (talent.depends == this) return false;
        }
        return true;
    }
}
