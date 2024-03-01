package com.cavetale.skills.skill.combat;

import com.cavetale.worldmarker.util.Tags;
import java.util.EnumMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import static com.cavetale.mytems.MytemsPlugin.namespacedKey;
import static org.bukkit.entity.EntityType.*;

@RequiredArgsConstructor
public final class CombatReward {
    private static final EnumMap<EntityType, CombatReward> REWARDS = new EnumMap<>(EntityType.class);
    public final EntityType type;
    public final int sp;
    public final double money;

    static {
        // Regular
        reward(5, 20.0, DROWNED);
        reward(5, 20.0, HUSK);
        reward(5, 20.0, SKELETON);
        reward(5, 20.0, STRAY);
        reward(5, 20.0, ZOMBIE);
        reward(5, 20.0, ZOMBIE_VILLAGER);

        // Slimes
        reward(3, 20.0, SLIME);
        reward(3, 20.0, MAGMA_CUBE);

        // Tiny
        reward(1, 20.0, VEX);
        reward(1, 20.0, SILVERFISH);
        reward(1, 20.0, ENDERMITE);

        // Spiders
        reward(5, 20.0, CAVE_SPIDER);
        reward(5, 20.0, SPIDER);

        // Overworld hard
        reward(10, 20.0, CREEPER);
        reward(15, 20.0, ENDERMAN);
        reward(15, 20.0, PHANTOM);

        // Illagers
        reward(15, 50.0, EVOKER);
        reward(15, 50.0, ILLUSIONER);
        reward(15, 50.0, PILLAGER);
        reward(15, 50.0, VINDICATOR);
        reward(15, 50.0, WITCH);
        reward(50, 100.0, RAVAGER);

        // Pigs
        reward(15, 20.0, HOGLIN);
        reward(15, 20.0, ZOGLIN);
        reward(25, 20.0, PIGLIN);
        reward(25, 20.0, ZOMBIFIED_PIGLIN);
        reward(30, 20.0, PIGLIN_BRUTE);

        // Situational
        reward(20, 50.0, BLAZE);
        reward(20, 50.0, GHAST);
        reward(20, 50.0, GUARDIAN);
        reward(20, 50.0, SHULKER);
        reward(20, 50.0, WITHER_SKELETON);

        // Bosses
        reward(50, 100.0, ELDER_GUARDIAN);
        reward(100, 100.0, WARDEN);
        reward(100, 100.0, ENDER_DRAGON);
        reward(100, 100.0, WITHER);

        // Removed
        reward(1, 1.0, GIANT);
    }

    private static void reward(final int sp, final double money, EntityType type) {
        REWARDS.put(type, new CombatReward(type, sp, money));
    }

    public static CombatReward combatReward(Entity entity) {
        Integer skillPoints = Tags.getInt(entity.getPersistentDataContainer(), namespacedKey("skillPoints"));
        if (skillPoints != null) {
            // WART: Money equals skillPoints
            return new CombatReward(entity.getType(), skillPoints.intValue(), skillPoints.doubleValue());
        }
        SpawnReason spawnReason = entity.getEntitySpawnReason();
        if (spawnReason != null) {
            switch (spawnReason) {
            case SLIME_SPLIT:
            case SPAWNER:
            case SPELL:
            case REINFORCEMENTS:
                return null;
            default: break;
            }
        }
        if (entity.fromMobSpawner()) return null;
        return REWARDS.get(entity.getType());
    }
}
