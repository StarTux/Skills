package com.cavetale.skills.talent;

import com.cavetale.core.item.ItemKinds;
import com.cavetale.core.struct.Vec2i;
import com.cavetale.skills.skill.SkillType;
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
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;

@Getter
public enum TalentType implements ComponentLike {
    // Mining
    MINE_MAGNET(SkillType.MINING, null, Vec2i.of(4, 1)),
    STRIP_MINING(SkillType.MINING, null, Vec2i.of(2, 3)),
    DEEP_MINING(SkillType.MINING, STRIP_MINING, Vec2i.of(2, 1)),
    VEIN_MINING(SkillType.MINING, STRIP_MINING, Vec2i.of(2, 5)),
    MINER_SIGHT(SkillType.MINING, null, Vec2i.of(6, 3)),
    SUPER_VISION(SkillType.MINING, MINER_SIGHT, Vec2i.of(6, 1)),
    ORE_ALERT(SkillType.MINING, MINER_SIGHT, Vec2i.of(8, 3)),
    SILK_STRIP(SkillType.MINING, ORE_ALERT, Vec2i.of(8, 5)),

    // Combat

    COMBAT_MAGNET(SkillType.COMBAT, null, Vec2i.of(4, 1)),
    BERSERKER(SkillType.COMBAT, null, Vec2i.of(6, 2)),
    PYROMANIAC(SkillType.COMBAT, null, Vec2i.of(6, 5)),
    TOXICIST(SkillType.COMBAT, TalentType.PYROMANIAC, Vec2i.of(7, 5)),
    DENIAL(SkillType.COMBAT, null, Vec2i.of(7, 3)), // +slow?
    GOD_MODE(SkillType.COMBAT, TalentType.DENIAL, Vec2i.of(8, 3)),
    EXECUTIONER(SkillType.COMBAT, null, Vec2i.of(4, 3)),

    // Archery

    // Up
    ARROW_MAGNET(SkillType.ARCHERY, null, Vec2i.of(4, 1)),
    // Left: Crossbow
    VOLLEY(SkillType.ARCHERY, null, Vec2i.of(2, 3)),
    GUNSLINGER(SkillType.ARCHERY, VOLLEY, Vec2i.of(2, 5)),
    WATER_BOMB(SkillType.ARCHERY, VOLLEY, Vec2i.of(2, 1)),
    // Right: Bow
    ARROW_SPEED(SkillType.ARCHERY, null, Vec2i.of(6, 3)),
    IN_THE_ZONE(SkillType.ARCHERY, ARROW_SPEED, Vec2i.of(8, 3)),
    SNIPER(SkillType.ARCHERY, IN_THE_ZONE, Vec2i.of(8, 1)),
    LEGOLAS(SkillType.ARCHERY, SNIPER, Vec2i.of(6, 1)),
    INSTANT_HIT(SkillType.ARCHERY, IN_THE_ZONE, Vec2i.of(8, 5)),
    HOMING_ARROW(SkillType.ARCHERY, INSTANT_HIT, Vec2i.of(6, 5)),
    // Down
    GLOW_MARK(SkillType.ARCHERY, null, Vec2i.of(4, 5)),
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
