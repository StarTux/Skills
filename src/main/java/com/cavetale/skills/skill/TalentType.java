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
    MINE_MAGNET(MINING, null, Vec2i.of(4, 5)),
    STRIP_MINING(MINING, null, Vec2i.of(2, 3)),
    DEEP_MINING(MINING, STRIP_MINING, Vec2i.of(2, 1)),
    VEIN_MINING(MINING, STRIP_MINING, Vec2i.of(2, 5)),
    MINER_SIGHT(MINING, null, Vec2i.of(6, 3)),
    SUPER_VISION(MINING, MINER_SIGHT, Vec2i.of(6, 1)),
    ORE_ALERT(MINING, MINER_SIGHT, Vec2i.of(8, 3)),
    SILK_STRIP(MINING, ORE_ALERT, Vec2i.of(8, 5)),

    // Combat

    // Up: Damage
    BERSERKER(COMBAT, null, Vec2i.of(6, 2)),
    // Down: Fire
    PYROMANIAC(COMBAT, null, Vec2i.of(6, 5)),
    TOXICIST(COMBAT, TalentType.PYROMANIAC, Vec2i.of(7, 5)),
    // Right: Magic
    DENIAL(COMBAT, null, Vec2i.of(7, 3)), // +slow?
    GOD_MODE(COMBAT, TalentType.DENIAL, Vec2i.of(8, 3)),
    // Left: Weapons
    EXECUTIONER(COMBAT, null, Vec2i.of(4, 3)),

    // Archery

    // Left: Crossbow
    VOLLEY(ARCHERY, null, Vec2i.of(2, 3)),
    GUNSLINGER(ARCHERY, VOLLEY, Vec2i.of(2, 5)),
    WATER_BOMB(ARCHERY, VOLLEY, Vec2i.of(2, 1)),
    // Right: Bow
    ARROW_SPEED(ARCHERY, null, Vec2i.of(6, 3)),
    IN_THE_ZONE(ARCHERY, ARROW_SPEED, Vec2i.of(8, 3)),
    SNIPER(ARCHERY, IN_THE_ZONE, Vec2i.of(8, 1)),
    LEGOLAS(ARCHERY, SNIPER, Vec2i.of(6, 1)),
    INSTANT_HIT(ARCHERY, IN_THE_ZONE, Vec2i.of(8, 5)),
    HOMING_ARROW(ARCHERY, INSTANT_HIT, Vec2i.of(6, 5)),
    // Down
    GLOW_MARK(ARCHERY, null, Vec2i.of(4, 5)),
    // Up
    ARROW_MAGNET(ARCHERY, null, Vec2i.of(4, 1)),
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
