package com.cavetale.skills.skill.combat;

import java.util.EnumMap;
import lombok.Value;
import org.bukkit.entity.EntityType;

@Value
final class CombatRewards {
    protected final EnumMap<EntityType, CombatReward> rewards = new EnumMap<>(EntityType.class);

    protected void enable() {
        reward(EntityType.ZOMBIE, 1);
        reward(EntityType.SKELETON, 1);
        reward(EntityType.CREEPER, 2);
        reward(EntityType.SLIME, 1);
        reward(EntityType.SILVERFISH, 1);
        reward(EntityType.POLAR_BEAR, 2);
        reward(EntityType.SHULKER, 2);
        reward(EntityType.SPIDER, 2);
        reward(EntityType.CAVE_SPIDER, 2);
        reward(EntityType.WITCH, 5);
        reward(EntityType.ZOMBIE_VILLAGER, 2);
        reward(EntityType.ENDERMITE, 2);
        reward(EntityType.BLAZE, 3);
        reward(EntityType.ELDER_GUARDIAN, 3);
        reward(EntityType.EVOKER, 3);
        reward(EntityType.GUARDIAN, 3);
        reward(EntityType.HUSK, 3);
        reward(EntityType.MAGMA_CUBE, 3);
        reward(EntityType.PHANTOM, 3);
        reward(EntityType.VEX, 2);
        reward(EntityType.VINDICATOR, 3);
        reward(EntityType.WITHER_SKELETON, 4);
        reward(EntityType.GHAST, 5);
        reward(EntityType.STRAY, 1);
        reward(EntityType.DROWNED, 1);
        reward(EntityType.ILLUSIONER, 1);
        reward(EntityType.GIANT, 1);
        reward(EntityType.PIGLIN, 1);
        reward(EntityType.PIGLIN_BRUTE, 5);
        reward(EntityType.ZOMBIFIED_PIGLIN, 1);
        reward(EntityType.ENDERMAN, 1);
        reward(EntityType.ENDER_DRAGON, 10);
        reward(EntityType.WITHER, 10);
        reward(EntityType.HOGLIN, 3);
        reward(EntityType.ZOGLIN, 3);
        reward(EntityType.PILLAGER, 3);
        reward(EntityType.RAVAGER, 5);
        reward(EntityType.WARDEN, 10);
    }

    private void reward(EntityType type, final int sp) {
        rewards.put(type, new CombatReward(type, sp));
    }
}
