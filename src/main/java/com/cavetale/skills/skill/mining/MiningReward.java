package com.cavetale.skills.skill.mining;

import lombok.Value;
import org.bukkit.Material;

@Value
final class MiningReward {
    protected final Material material;
    protected final int sp;
    protected final double money;
    protected final int exp;
    protected final Material item;
    protected final int drops;
    protected final Material replaceable;
}
