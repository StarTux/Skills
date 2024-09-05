package com.cavetale.skills.skill.mining;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.bukkit.Material;

@Value
@Builder
public final class MiningReward {
    @NonNull protected final Material material;
    protected final int sp;
    protected final double money;
    protected final int exp;
    protected final int veinExp;
    protected final Material silkStripItem;
    protected final int drops;
    protected final Material replaceable;
}
