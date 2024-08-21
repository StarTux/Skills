package com.cavetale.skills.skill;

import com.cavetale.core.item.ItemKinds;
import com.cavetale.core.struct.Vec2i;
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
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;

@Getter
public enum TalentType implements ComponentLike {
    // Mining
    MINE_MAGNET(MINING, null, Vec2i.of(5, 4)),

    STRIP_MINING(MINING, null, Vec2i.of(6, 3)),
    DEEP_MINING(MINING, STRIP_MINING, Vec2i.of(6, 2)),
    VEIN_MINING(MINING, STRIP_MINING, Vec2i.of(7, 3)),
    VEIN_METALS(MINING, VEIN_MINING, Vec2i.of(8, 3)),
    VEIN_GEMS(MINING, VEIN_MINING, Vec2i.of(8, 4)),
    RUBY(MINING, VEIN_GEMS, Vec2i.of(8, 5)),

    MINER_SIGHT(MINING, null, Vec2i.of(4, 3)),

    SUPER_VISION(MINING, MINER_SIGHT, Vec2i.of(4, 2)),
    NETHER_VISION(MINING, SUPER_VISION, Vec2i.of(4, 1)),
    DEEP_VISION(MINING, NETHER_VISION, Vec2i.of(3, 1)),

    ORE_ALERT(MINING, MINER_SIGHT, Vec2i.of(3, 3)),
    EMERALD_ALERT(MINING, ORE_ALERT, Vec2i.of(2, 3)),
    DEBRIS_ALERT(MINING, EMERALD_ALERT, Vec2i.of(1, 3)),

    SILK_STRIP(MINING, ORE_ALERT, Vec2i.of(3, 4)),
    SILK_MULTI(MINING, SILK_STRIP, Vec2i.of(3, 5)),
    SILK_METALS(MINING, SILK_MULTI, Vec2i.of(2, 5)),

    // Combat

    // Up: Damage
    BERSERKER(COMBAT, null, Vec2i.of(5, 2)),
    // Down: Fire
    SEARING(COMBAT, null, Vec2i.of(5, 4)),
    PYROMANIAC(COMBAT, SEARING, Vec2i.of(5, 5)),
    // Right: Magic
    DENIAL(COMBAT, null, Vec2i.of(6, 3)), // +slow?
    GOD_MODE(COMBAT, TalentType.DENIAL, Vec2i.of(7, 3)),
    TOXICIST(COMBAT, TalentType.DENIAL, Vec2i.of(7, 2)),
    TOXIC_FUROR(COMBAT, TalentType.TOXICIST, Vec2i.of(8, 2)),
    // Left: Weapons
    IRON_AGE(COMBAT, null, Vec2i.of(4, 3)),
    EXECUTIONER(COMBAT, TalentType.IRON_AGE, Vec2i.of(3, 3)),
    IMPALER(COMBAT, TalentType.IRON_AGE, Vec2i.of(3, 2)),

    // Archery

    // Right: Bow Precision (16)
    ARCHER_ZONE(ARCHERY, null, Vec2i.of(6, 3)),
    ARCHER_ZONE_DEATH(ARCHERY, ARCHER_ZONE, Vec2i.of(7, 3)),
    ARROW_SWIFTNESS(ARCHERY, ARCHER_ZONE_DEATH, Vec2i.of(8, 3)),
    ARROW_DAMAGE(ARCHERY, ARROW_SWIFTNESS, Vec2i.of(8, 2)), // up
    BONUS_ARROW(ARCHERY, ARROW_DAMAGE, Vec2i.of(8, 1)),
    ARROW_VELOCITY(ARCHERY, BONUS_ARROW, Vec2i.of(7, 1)),
    // Left: Crossbow AoE (15/15)
    XBOW_INFINITY(ARCHERY, null, Vec2i.of(4, 3)),
    XBOW_VOLLEY(ARCHERY, XBOW_INFINITY, Vec2i.of(3, 3)),
    XBOW_FLAME(ARCHERY, XBOW_VOLLEY, Vec2i.of(2, 3)),
    XBOW_PIERCE(ARCHERY, XBOW_FLAME, Vec2i.of(2, 4)), // down
    XBOW_DUAL(ARCHERY, XBOW_PIERCE, Vec2i.of(2, 5)),
    XBOW_HAIL(ARCHERY, XBOW_FLAME, Vec2i.of(2, 2)), // up
    XBOW_LINGER(ARCHERY, XBOW_HAIL, Vec2i.of(2, 1)),
    // Down: Tipped (8)
    TIPPED_INFINITY(ARCHERY, null, Vec2i.of(5, 4)),
    SPECTRAL_INFINITY(ARCHERY, TIPPED_INFINITY, Vec2i.of(5, 5)),
    GLOW_MARK(ARCHERY, SPECTRAL_INFINITY, Vec2i.of(4, 5)),
    // Up: Utility (9)
    ARROW_MAGNET(ARCHERY, null, Vec2i.of(5, 2)),
    INFINITY_MENDING(ARCHERY, ARROW_MAGNET, Vec2i.of(5, 1)),
    INSTANT_HIT(ARCHERY, INFINITY_MENDING, Vec2i.of(4, 1)),
    ;

    public final String key;
    public final SkillType skillType;
    public final TalentType depends;
    public final Vec2i slot;
    public static final Map<SkillType, Set<TalentType>> SKILL_MAP = new EnumMap<>(SkillType.class);
    private Talent talent;
    private boolean enabled;

    TalentType(final SkillType skillType, final TalentType depends, final Vec2i slot) {
        this.key = name().toLowerCase();
        this.skillType = skillType;
        this.depends = depends;
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
