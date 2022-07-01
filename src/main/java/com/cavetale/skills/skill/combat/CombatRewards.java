package com.cavetale.skills.skill.combat;

import java.util.EnumMap;
import lombok.Value;
import org.bukkit.entity.EntityType;
import static org.bukkit.entity.EntityType.*;

@Value
final class CombatRewards {
    protected final EnumMap<EntityType, CombatReward> rewards = new EnumMap<>(EntityType.class);

    protected void enable() {
        reward(ZOMBIE, 1, 1.0);
        reward(SKELETON, 1, 1.0);
        reward(CREEPER, 2, 2.0);
        reward(SLIME, 1, 1.0);
        reward(SILVERFISH, 1, 1.0);
        reward(POLAR_BEAR, 2, 2.0);
        reward(SHULKER, 2, 2.0);
        reward(SPIDER, 2, 2.0);
        reward(CAVE_SPIDER, 2, 2.0);
        reward(WITCH, 5, 5.0);
        reward(ZOMBIE_VILLAGER, 2, 2.0);
        reward(ENDERMITE, 2, 2.0);
        reward(BLAZE, 3, 3.0);
        reward(ELDER_GUARDIAN, 3, 3.0);
        reward(EVOKER, 3, 3.0);
        reward(GUARDIAN, 3, 3.0);
        reward(HUSK, 3, 3.0);
        reward(MAGMA_CUBE, 3, 3.0);
        reward(PHANTOM, 3, 3.0);
        reward(VEX, 2, 2.0);
        reward(VINDICATOR, 3, 3.0);
        reward(WITHER_SKELETON, 4, 4.0);
        reward(GHAST, 5, 5.0);
        reward(STRAY, 1, 1.0);
        reward(DROWNED, 1, 1.0);
        reward(ILLUSIONER, 1, 1.0);
        reward(GIANT, 1, 1.0);
        reward(PIGLIN, 1, 1.0);
        reward(PIGLIN_BRUTE, 5, 5.0);
        reward(ZOMBIFIED_PIGLIN, 1, 1.0);
        reward(ENDERMAN, 1, 1.0);
        reward(ENDER_DRAGON, 10, 10.0);
        reward(WITHER, 10, 10.0);
        reward(HOGLIN, 3, 3.0);
        reward(ZOGLIN, 3, 3.0);
        reward(PILLAGER, 3, 3.0);
        reward(RAVAGER, 5, 5.0);
        reward(WARDEN, 10, 10.0);
    }

    private void reward(EntityType type, final int sp, final double money) {
        rewards.put(type, new CombatReward(type, sp * 2, money));
    }
}
