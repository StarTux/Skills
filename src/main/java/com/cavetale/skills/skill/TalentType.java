package com.cavetale.skills.skill;

import com.cavetale.core.item.ItemKinds;
import com.cavetale.skills.util.Vec2i;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.util.Vec2i.v;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;

@Getter
public enum TalentType implements ComponentLike {
    // Mining
    MINE_MAGNET(SkillType.MINING, null, 1, v(5, 4)),

    STRIP_MINING(SkillType.MINING, null, 1, v(6, 3)),
    DEEP_MINING(SkillType.MINING, STRIP_MINING, 2, v(6, 2)),
    VEIN_MINING(SkillType.MINING, STRIP_MINING, 1, v(7, 3)),
    VEIN_METALS(SkillType.MINING, VEIN_MINING, 2, v(8, 3)),
    VEIN_GEMS(SkillType.MINING, VEIN_MINING, 2, v(8, 4)),
    RUBY(SkillType.MINING, VEIN_GEMS, 2, v(8, 5)),

    MINER_SIGHT(SkillType.MINING, null, 1, v(4, 3)),

    SUPER_VISION(SkillType.MINING, MINER_SIGHT, 3, v(4, 2)),
    NETHER_VISION(SkillType.MINING, SUPER_VISION, 4, v(4, 1)),
    DEEP_VISION(SkillType.MINING, NETHER_VISION, 5, v(3, 1)),

    ORE_ALERT(SkillType.MINING, MINER_SIGHT, 3, v(3, 3)),
    EMERALD_ALERT(SkillType.MINING, ORE_ALERT, 4, v(2, 3)),
    DEBRIS_ALERT(SkillType.MINING, EMERALD_ALERT, 5, v(1, 3)),

    SILK_STRIP(SkillType.MINING, ORE_ALERT, 2, v(3, 4)),
    SILK_MULTI(SkillType.MINING, SILK_STRIP, 3, v(3, 5)),
    SILK_METALS(SkillType.MINING, SILK_MULTI, 4, v(2, 5)),

    // Combat
    SEARING(SkillType.COMBAT, null, 1, v(5, 4)),
    PYROMANIAC(SkillType.COMBAT, SEARING, 2, v(5, 5)),
    DENIAL(SkillType.COMBAT, null, 1, v(6, 3)), // +slow?
    IRON_AGE(SkillType.COMBAT, null, 1, v(4, 3)),
    EXECUTIONER(SkillType.COMBAT, TalentType.IRON_AGE, 3, v(3, 3)),
    IMPALER(SkillType.COMBAT, TalentType.IRON_AGE, 3, v(3, 2)),
    TOXICIST(SkillType.COMBAT, TalentType.DENIAL, 2, v(7, 2)),
    TOXIC_FUROR(SkillType.COMBAT, TalentType.TOXICIST, 3, v(8, 2)),
    GOD_MODE(SkillType.COMBAT, TalentType.DENIAL, 3, v(7, 3)),

    // Archery

    // Right: Arrow Precision
    ARCHER_ZONE(SkillType.ARCHERY, null, 1, v(6, 3)),
    ARCHER_ZONE_DEATH(SkillType.ARCHERY, ARCHER_ZONE, 1, v(7, 3)),
    ARROW_SWIFTNESS(SkillType.ARCHERY, ARCHER_ZONE_DEATH, 2, v(8, 3)),
    ARROW_DAMAGE(SkillType.ARCHERY, ARROW_SWIFTNESS, 3, v(8, 2)),
    BONUS_ARROW(SkillType.ARCHERY, ARROW_DAMAGE, 4, v(8, 1)),

    // Up: Shotgun
    // BOW_SHOTGUN(SkillType.ARCHERY, null, 1, v(5, 2)),
    // BOW_EXPLOSION(SkillType.ARCHERY, null, 2, v(5, 1)),

    // Down: Tipped
    // TIPPED_INFINITY(SkillType.ARCHERY, null, 1, v(5, 4)),
    // GLOW_INFINITY(SkillType.ARCHERY, null, 2, v(5, 5)),
    // GLOW_MARK(SkillType.ARCHERY, null, 4, v(6, 5)),
    // TIPPED_LINGER(SkillType.ARCHERY, null, 4, v(7, 5)),

    // Utility
    ARROW_MAGNET(SkillType.ARCHERY, null, 1, v(5, 2)),

    // Left: Crossbows
    // XBOW_RAY_TRACE(SkillType.ARCHERY, null, 3, v(4, 3)),
    // XBOW_INFINITY(SkillType.ARCHERY, null, 3, v(3, 3)),
    ;

    public final String key;
    public final SkillType skillType;
    public final TalentType depends;
    public final int talentPointCost;
    public final Vec2i slot;
    public static final Map<SkillType, Set<TalentType>> SKILL_MAP = new EnumMap<>(SkillType.class);
    private Talent talent;
    private boolean enabled;

    TalentType(final SkillType skillType, final TalentType depends, final int talentPointCost, final Vec2i slot) {
        this.key = name().toLowerCase();
        this.skillType = skillType;
        this.depends = depends;
        this.talentPointCost = talentPointCost;
        this.slot = slot;
        new DefaultTalent(this);
    }

    static {
        for (SkillType skillType : SkillType.values()) {
            SKILL_MAP.put(skillType, EnumSet.noneOf(TalentType.class));
        }
        for (TalentType talent : TalentType.values()) {
            SKILL_MAP.get(talent.skillType).add(talent);
        }
    }

    public Component getIconComponent() {
        return ItemKinds.icon(talent.createIcon());
    }

    public Component getDisplayComponent() {
        return text(talent.getDisplayName(), skillType.textColor);
    }

    @Override
    public Component asComponent() {
        return join(noSeparators(), getIconComponent(), getDisplayComponent());
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
        if (enabled) {
            skillsPlugin().getLogger().warning("Duplicate talent registration: " + this);
        }
        this.talent = newTalent;
        if (!(newTalent instanceof DefaultTalent)) {
            this.enabled = true;
        }
    }

    public static List<TalentType> getTalents(SkillType skillType) {
        return List.copyOf(SKILL_MAP.get(skillType));
    }

    public static List<String> getTalentKeys(SkillType skillType) {
        List<TalentType> talents = getTalents(skillType);
        List<String> result = new ArrayList<>(talents.size());
        for (TalentType talent : talents) {
            result.add(talent.key);
        }
        return result;
    }
}
