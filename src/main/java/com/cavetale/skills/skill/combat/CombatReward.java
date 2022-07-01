package com.cavetale.skills.skill.combat;

import lombok.Value;
import org.bukkit.entity.EntityType;

@Value
final class CombatReward {
    protected final EntityType type;
    protected final int sp;
    protected final double money;
}
