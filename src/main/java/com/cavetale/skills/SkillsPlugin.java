package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkBlock;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkillsPlugin extends JavaPlugin {
    // Utility
    final Random random = ThreadLocalRandom.current();
    final Yaml yaml = new Yaml(this);
    final Json json = new Json(this);
    final Metadata meta = new Metadata(this);
    // Listeners
    final SkillsCommand skillsCommand = new SkillsCommand(this);
    final EventListener eventListener = new EventListener(this);
    // Skills
    final Farming farming = new Farming(this);
    final Mining mining = new Mining(this);
    final Combat combat = new Combat(this);
    // Components
    final SQL sql = new SQL(this);
    final Sessions sessions = new Sessions(this);
    final Talents talents = new Talents(this);
    final Advancements advancements = new Advancements(this);
    final Infos infos = new Infos(this);
    long ticks = 0;

    @Override
    public void onEnable() {
        getCommand("skills").setExecutor(skillsCommand);
        getServer().getPluginManager().registerEvents(eventListener, this);
        talents.load();
        sql.enable();
        advancements.loadAll();
        infos.load();
        for (Player player : getServer().getOnlinePlayers()) {
            sessions.load(player);
        }
        getServer().getScheduler().runTaskTimer(this, this::onTick, 1, 1);
    }

    @Override
    public void onDisable() {
        sessions.disable();
    }

    void onTick() {
        ticks += 1;
        sessions.tick();
        if ((ticks % 10) == 0) {
            for (Player player : getServer().getOnlinePlayers()) {
                tickPlayer(player);
            }
        }
    }

    // Show ambient particle effects of nearby blocks
    void tickPlayer(@NonNull Player player) {
        if (sessions.of(player).noParticles) return;
        List<MarkBlock> blocks =
            BlockMarker.getNearbyBlocks(player.getLocation().getBlock(), 24)
            .stream().filter(mb -> mb.hasId() && mb.getId().startsWith("skills:"))
            .collect(Collectors.toList());
        if (blocks.isEmpty()) return;
        Collections.shuffle(blocks, random);
        final int max = Math.min(blocks.size(), 48);
        for (int i = 0; i < max; i += 1) {
            MarkBlock markBlock = blocks.get(i);
            switch (markBlock.getId()) {
            case Farming.WATERED_CROP:
                Effects.wateredCropAmbient(player, markBlock.getBlock());
                break;
            case Farming.GROWN_CROP:
                Effects.grownCropAmbient(player, markBlock.getBlock());
                break;
            default: break;
            }
        }
    }

    static int pointsForLevelUp(final int lvl) {
        return lvl * 50 + lvl * lvl * 10;
    }

    void addSkillPoints(@NonNull Player player, @NonNull SkillType skill, final int add) {
        Session session = sessions.of(player);
        SQLSkill col = session.skillRows.get(skill);
        int points = col.points + add;
        int req = pointsForLevelUp(col.level + 1);
        if (points >= req) {
            points -= req;
            col.level += 1;
            session.playerRow.levels += 1;
            session.playerRow.modified = true;
            Effects.levelup(player);
            player.sendTitle(ChatColor.GOLD + skill.displayName,
                             ChatColor.WHITE + "Level " + col.level);
        }
        col.points = points;
        col.modified = true;
        sessions.of(player).showSkillBar(player, skill, col.level, points, req, add);
    }
}
