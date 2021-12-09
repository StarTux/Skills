package com.cavetale.skills.skill;

import com.cavetale.skills.Util;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;

@Getter
public enum TalentType {
    // Mining
    MINE_STRIP(SkillType.MINING, null),
    MINE_ORE_ALERT(SkillType.MINING, TalentType.MINE_STRIP),
    MINE_XRAY(SkillType.MINING, TalentType.MINE_ORE_ALERT),
    MINE_SILK_STRIP(SkillType.MINING, TalentType.MINE_STRIP),
    MINE_SILK_MULTI(SkillType.MINING, TalentType.MINE_SILK_STRIP),
    // Farming
    FARM_GROWSTICK_RADIUS(SkillType.FARMING, null),
    FARM_PLANT_RADIUS(SkillType.FARMING, TalentType.FARM_GROWSTICK_RADIUS),
    FARM_CROP_DROPS(SkillType.FARMING, TalentType.FARM_GROWSTICK_RADIUS),
    FARM_DIAMOND_DROPS(SkillType.FARMING, TalentType.FARM_CROP_DROPS),
    FARM_TALENT_POINTS(SkillType.FARMING, TalentType.FARM_DIAMOND_DROPS),
    // Combat
    COMBAT_FIRE(SkillType.COMBAT, null),
    COMBAT_SILENCE(SkillType.COMBAT, TalentType.COMBAT_FIRE),
    COMBAT_SPIDERS(SkillType.COMBAT, TalentType.COMBAT_SILENCE),
    COMBAT_GOD_MODE(SkillType.COMBAT, TalentType.COMBAT_SPIDERS),
    COMBAT_ARCHER_ZONE(SkillType.COMBAT, TalentType.COMBAT_FIRE);

    public final String key;
    public final SkillType skill;
    public final TalentType depends;
    public final String displayName;
    public static final int COUNT = 15;
    public static final Map<SkillType, Set<TalentType>> SKILL_MAP = new EnumMap<>(SkillType.class);
    private Talent talent;

    TalentType(@NonNull final SkillType skillType, final TalentType depends) {
        this.key = name().toLowerCase();
        this.skill = skillType;
        this.depends = depends;
        this.displayName = Util.niceEnumName(this);
    }

    static {
        for (TalentType talent : TalentType.values()) {
            SKILL_MAP.computeIfAbsent(talent.skill, sk -> EnumSet.of(talent))
                .add(talent);
        }
    }

    public static TalentType of(@NonNull String key) {
        for (TalentType t : TalentType.values()) {
            if (key.equals(t.key)) return t;
        }
        return null;
    }

    public boolean isTerminal() {
        for (TalentType it : TalentType.values()) {
            if (it.depends == this) return false;
        }
        return true;
    }

    protected void register(final Talent newTalent) {
        this.talent = newTalent;
    }
}
