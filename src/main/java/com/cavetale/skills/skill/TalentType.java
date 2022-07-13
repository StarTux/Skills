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
import static com.cavetale.skills.skill.SkillType.*;
import static com.cavetale.skills.util.Vec2i.v;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;

@Getter
public enum TalentType implements ComponentLike {
    // Mining
    MINE_MAGNET(MINING, null, 1, v(5, 4)),

    STRIP_MINING(MINING, null, 1, v(6, 3)),
    DEEP_MINING(MINING, STRIP_MINING, 2, v(6, 2)),
    VEIN_MINING(MINING, STRIP_MINING, 1, v(7, 3)),
    VEIN_METALS(MINING, VEIN_MINING, 2, v(8, 3)),
    VEIN_GEMS(MINING, VEIN_MINING, 2, v(8, 4)),
    RUBY(MINING, VEIN_GEMS, 2, v(8, 5)),

    MINER_SIGHT(MINING, null, 1, v(4, 3)),

    SUPER_VISION(MINING, MINER_SIGHT, 3, v(4, 2)),
    NETHER_VISION(MINING, SUPER_VISION, 4, v(4, 1)),
    DEEP_VISION(MINING, NETHER_VISION, 5, v(3, 1)),

    ORE_ALERT(MINING, MINER_SIGHT, 3, v(3, 3)),
    EMERALD_ALERT(MINING, ORE_ALERT, 4, v(2, 3)),
    DEBRIS_ALERT(MINING, EMERALD_ALERT, 5, v(1, 3)),

    SILK_STRIP(MINING, ORE_ALERT, 2, v(3, 4)),
    SILK_MULTI(MINING, SILK_STRIP, 3, v(3, 5)),
    SILK_METALS(MINING, SILK_MULTI, 4, v(2, 5)),

    // Combat
    SEARING(COMBAT, null, 1, v(5, 4)),
    PYROMANIAC(COMBAT, SEARING, 2, v(5, 5)),
    DENIAL(COMBAT, null, 1, v(6, 3)), // +slow?
    IRON_AGE(COMBAT, null, 1, v(4, 3)),
    EXECUTIONER(COMBAT, TalentType.IRON_AGE, 3, v(3, 3)),
    IMPALER(COMBAT, TalentType.IRON_AGE, 3, v(3, 2)),
    TOXICIST(COMBAT, TalentType.DENIAL, 2, v(7, 2)),
    TOXIC_FUROR(COMBAT, TalentType.TOXICIST, 3, v(8, 2)),
    GOD_MODE(COMBAT, TalentType.DENIAL, 3, v(7, 3)),

    // Archery

    // Right: Bow Precision (16)
    ARCHER_ZONE(ARCHERY, null, 1, v(6, 3)),
    ARCHER_ZONE_DEATH(ARCHERY, ARCHER_ZONE, 1, v(7, 3)),
    ARROW_SWIFTNESS(ARCHERY, ARCHER_ZONE_DEATH, 2, v(8, 3)),
    ARROW_DAMAGE(ARCHERY, ARROW_SWIFTNESS, 3, v(8, 2)), // up
    BONUS_ARROW(ARCHERY, ARROW_DAMAGE, 4, v(8, 1)),
    ARROW_VELOCITY(ARCHERY, BONUS_ARROW, 5, v(7, 1)),
    // Left: Crossbow AoE (15/15)
    XBOW_INFINITY(ARCHERY, null, 1, v(4, 3)),
    XBOW_VOLLEY(ARCHERY, XBOW_INFINITY, 2, v(3, 3)),
    XBOW_FLAME(ARCHERY, XBOW_VOLLEY, 3, v(2, 3)),
    XBOW_PIERCE(ARCHERY, XBOW_FLAME, 4, v(2, 4)), // down
    XBOW_DUAL(ARCHERY, XBOW_PIERCE, 5, v(2, 5)),
    XBOW_HAIL(ARCHERY, XBOW_FLAME, 4, v(2, 2)), // up
    XBOW_LINGER(ARCHERY, XBOW_HAIL, 5, v(2, 1)),
    // Down: Tipped (8)
    TIPPED_INFINITY(ARCHERY, null, 2, v(5, 4)),
    SPECTRAL_INFINITY(ARCHERY, TIPPED_INFINITY, 2, v(5, 5)),
    GLOW_MARK(ARCHERY, SPECTRAL_INFINITY, 4, v(4, 5)),
    // Up: Utility (9)
    ARROW_MAGNET(ARCHERY, null, 1, v(5, 2)),
    INFINITY_MENDING(ARCHERY, ARROW_MAGNET, 3, v(5, 1)),
    INSTANT_HIT(ARCHERY, INFINITY_MENDING, 5, v(4, 1)),
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
