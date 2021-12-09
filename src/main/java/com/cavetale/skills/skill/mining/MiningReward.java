package com.cavetale.skills.skill.mining;

import lombok.Value;
import org.bukkit.Material;

@Value
final class MiningReward {
    protected final Material material;
    protected final int sp;
    protected final int exp;
    protected final Material item;
    protected final int drops;
    protected final Material replaceable;

    protected boolean dropSelf() {
        switch (material) {
        case DEEPSLATE_IRON_ORE:
        case IRON_ORE:
        case DEEPSLATE_COPPER_ORE:
        case COPPER_ORE:
        case DEEPSLATE_GOLD_ORE:
        case GOLD_ORE:
            return true;
        default: return false;
        }
    }
}
