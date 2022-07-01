package com.cavetale.skills.skill.combat;

import java.util.EnumMap;
import lombok.Value;
import org.bukkit.entity.EntityType;

@Value
final class CombatRewards {
    protected final EnumMap<EntityType, CombatReward> rewards = new EnumMap<>(EntityType.class);

    protected void enable() {
        reward(EntityType.ZOMBIE, 1, 1.0);
        reward(EntityType.SKELETON, 1, 1.0);
        reward(EntityType.CREEPER, 2, 2.0);
        reward(EntityType.SLIME, 1, 1.0);
        reward(EntityType.SILVERFISH, 1, 1.0);
        reward(EntityType.POLAR_BEAR, 2, 2.0);
        reward(EntityType.SHULKER, 2, 2.0);
        reward(EntityType.SPIDER, 2, 2.0);
        reward(EntityType.CAVE_SPIDER, 2, 2.0);
        reward(EntityType.WITCH, 5, 5.0);
        reward(EntityType.ZOMBIE_VILLAGER, 2, 2.0);
        reward(EntityType.ENDERMITE, 2, 2.0);
        reward(EntityType.BLAZE, 3, 3.0);
        reward(EntityType.ELDER_GUARDIAN, 3, 3.0);
        reward(EntityType.EVOKER, 3, 3.0);
        reward(EntityType.GUARDIAN, 3, 3.0);
        reward(EntityType.HUSK, 3, 3.0);
        reward(EntityType.MAGMA_CUBE, 3, 3.0);
        reward(EntityType.PHANTOM, 3, 3.0);
        reward(EntityType.VEX, 2, 2.0);
        reward(EntityType.VINDICATOR, 3, 3.0);
        reward(EntityType.WITHER_SKELETON, 4, 4.0);
        reward(EntityType.GHAST, 5, 5.0);
        reward(EntityType.STRAY, 1, 1.0);
        reward(EntityType.DROWNED, 1, 1.0);
        reward(EntityType.ILLUSIONER, 1, 1.0);
        reward(EntityType.GIANT, 1, 1.0);
        reward(EntityType.PIGLIN, 1, 1.0);
        reward(EntityType.PIGLIN_BRUTE, 5, 5.0);
        reward(EntityType.ZOMBIFIED_PIGLIN, 1, 1.0);
        reward(EntityType.ENDERMAN, 1, 1.0);
        reward(EntityType.ENDER_DRAGON, 10, 10.0);
        reward(EntityType.WITHER, 10, 10.0);
        reward(EntityType.HOGLIN, 3, 3.0);
        reward(EntityType.ZOGLIN, 3, 3.0);
        reward(EntityType.PILLAGER, 3, 3.0);
        reward(EntityType.RAVAGER, 5, 5.0);
        reward(EntityType.WARDEN, 10, 10.0);
    }

    private void reward(EntityType type, final int sp, final double money) {
        rewards.put(type, new CombatReward(type, sp, money));
    }
}
