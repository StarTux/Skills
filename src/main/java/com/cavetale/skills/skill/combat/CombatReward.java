package com.cavetale.skills.skill.combat;

import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import static org.bukkit.entity.EntityType.*;

@RequiredArgsConstructor
public final class CombatReward {
    private static final EnumMap<EntityType, CombatReward> REWARDS = new EnumMap<>(EntityType.class);
    public final EntityType type;
    public final int sp;
    public final double money;

    static {
        // Regular
        reward(1, 1.0, DROWNED);
        reward(1, 1.0, HUSK);
        reward(1, 1.0, SKELETON);
        reward(1, 1.0, STRAY);
        reward(1, 1.0, ZOMBIE);
        reward(1, 1.0, ZOMBIE_VILLAGER);

        // Slimes
        reward(1, 1.0, SLIME);
        reward(1, 1.0, MAGMA_CUBE);

        // Tiny
        reward(1, 1.0, VEX);
        reward(1, 1.0, SILVERFISH);
        reward(1, 1.0, ENDERMITE);

        // Spiders
        reward(2, 1.0, CAVE_SPIDER);
        reward(2, 1.0, SPIDER);

        // Overworld hard
        reward(3, 1.0, CREEPER);
        reward(3, 1.0, ENDERMAN);
        reward(3, 1.0, PHANTOM);

        // Illagers
        reward(3, 1.0, EVOKER);
        reward(3, 1.0, ILLUSIONER);
        reward(3, 1.0, PILLAGER);
        reward(3, 1.0, VINDICATOR);
        reward(3, 1.0, WITCH);
        reward(10, 1.0, RAVAGER);

        // Pigs
        reward(3, 1.0, HOGLIN);
        reward(3, 1.0, PIGLIN);
        reward(3, 1.0, ZOGLIN);
        reward(3, 1.0, ZOMBIFIED_PIGLIN);
        reward(3, 1.0, PIGLIN_BRUTE);

        // Situational
        reward(5, 1.0, BLAZE);
        reward(5, 1.0, GHAST);
        reward(5, 1.0, GUARDIAN);
        reward(5, 1.0, SHULKER);
        reward(5, 1.0, WITHER_SKELETON);

        // Bosses
        reward(10, 1.0, ELDER_GUARDIAN);
        reward(20, 1.0, WARDEN);
        reward(50, 1.0, ENDER_DRAGON);
        reward(50, 1.0, WITHER);
    }

    private static void reward(final int sp, final double moneyFactor, EntityType type) {
        REWARDS.put(type, new CombatReward(type, sp * 5, 2.0 * (double) sp * moneyFactor));
    }

    public static CombatReward combatReward(Entity entity) {
        SpawnReason spawnReason = entity.getEntitySpawnReason();
        if (spawnReason != null) {
            switch (spawnReason) {
            case SLIME_SPLIT:
            case SPAWNER:
                return null;
            default: break;
            }
        }
        if (entity.fromMobSpawner()) return null;
        return REWARDS.get(entity.getType());
    }
}
