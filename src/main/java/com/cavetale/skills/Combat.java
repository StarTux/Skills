package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkChunk;
import java.util.EnumMap;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.Value;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

final class Combat {
    final SkillsPlugin plugin;
    final EnumMap<EntityType, Reward> rewards = new EnumMap<>(EntityType.class);
    static final String CHONK = "chonk";

    @Value
    static class Reward {
        final EntityType type;
        final int sp;
    }

    static class Chonk {
        int kills;
    }

    static Reward reward(@NonNull EntityType type, final int sp) {
        return new Reward(type, sp);
    }

    Combat(@NonNull final SkillsPlugin plugin) {
        this.plugin = plugin;
        Stream
            .of(reward(EntityType.ZOMBIE, 1),
                reward(EntityType.SKELETON, 1),
                reward(EntityType.CREEPER, 2),
                reward(EntityType.SLIME, 1),
                reward(EntityType.SILVERFISH, 1),
                reward(EntityType.POLAR_BEAR, 2),
                reward(EntityType.SHULKER, 2),
                reward(EntityType.SPIDER, 2),
                reward(EntityType.CAVE_SPIDER, 2),
                reward(EntityType.WITCH, 2),
                reward(EntityType.ZOMBIE_VILLAGER, 2),
                reward(EntityType.ENDERMITE, 2),
                reward(EntityType.BLAZE, 3),
                reward(EntityType.ELDER_GUARDIAN, 3),
                reward(EntityType.EVOKER, 3),
                reward(EntityType.GUARDIAN, 3),
                reward(EntityType.HUSK, 3),
                reward(EntityType.MAGMA_CUBE, 3),
                reward(EntityType.PHANTOM, 3),
                reward(EntityType.VEX, 3),
                reward(EntityType.VINDICATOR, 3),
                reward(EntityType.WITHER_SKELETON, 4),
                reward(EntityType.GHAST, 4),
                reward(EntityType.STRAY, 1),
                reward(EntityType.ILLUSIONER, 1),
                reward(EntityType.GIANT, 1),
                reward(EntityType.PIG_ZOMBIE, 1),
                reward(EntityType.ENDERMAN, 1),
                reward(EntityType.ENDER_DRAGON, 10),
                reward(EntityType.WITHER, 10))
            .forEach(reward -> rewards.put(reward.type, reward));
    }

    void kill(@NonNull Player player, @NonNull LivingEntity entity) {
        Reward reward = rewards.get(entity.getType());
        if (reward == null) return;
        Chunk chunk = entity.getLocation().getChunk();
        Chonk chonk = BlockMarker.getChunk(chunk)
            .getTransientData(CHONK, Chonk.class, Chonk::new);
        chonk.kills += 1;
        if (chonk.kills > 5) return;
        plugin.sessionOf(player).bossProgress += reward.sp;
        plugin.addSkillPoints(player, SkillType.COMBAT, reward.sp);
        player.sendMessage("" + plugin.sessionOf(player).bossProgress);
        Effects.kill(entity);
    }

    void onTick(@NonNull MarkChunk markChunk) {
        if ((markChunk.getTicksLoaded() % 200) == 0) {
            Chonk chonk = markChunk.getTransientData(CHONK, Chonk.class, Chonk::new);
            if (chonk.kills > 0) chonk.kills -= 1;
        }
    }
}
