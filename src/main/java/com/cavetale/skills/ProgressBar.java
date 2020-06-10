package com.cavetale.skills;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

final class ProgressBar {
    private BossBar bossBar;
    int lifespan = -1;
    SkillType skillType;
    List<Task> tasks = new ArrayList<>();

    ProgressBar(final SkillType skillType, final String name, final BarColor color, final BarStyle style) {
        this.skillType = skillType;
        bossBar = Bukkit.getServer().createBossBar(name, color, style);
    }

    ProgressBar(final SkillType skillType, final String name) {
        this(skillType, name, BarColor.BLUE, BarStyle.SOLID);
    }

    ProgressBar add(Player player) {
        bossBar.addPlayer(player);
        return this;
    }

    ProgressBar remove(Player player) {
        bossBar.removePlayer(player);
        return this;
    }

    ProgressBar clear() {
        bossBar.removeAll();
        return this;
    }

    ProgressBar show() {
        bossBar.setVisible(true);
        return this;
    }

    ProgressBar hide() {
        bossBar.setVisible(false);
        return this;
    }

    ProgressBar title(String title) {
        bossBar.setTitle(title);
        return this;
    }

    /**
     * Set the current progress.
     *
     * @param progress the progress
     */
    void setProgress(double progress) {
        bossBar.setProgress(progress);
    }

    /**
     * @return true if still alive, false otherwise.
     */
    boolean tick() {
        if (!tasks.isEmpty()) {
            boolean r = tasks.get(0).tick();
            if (!r) tasks.remove(0);
            show();
            lifespan = 100;
        } else if (lifespan > 0) {
            lifespan -= 1;
            if (lifespan == 0) {
                hide();
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if lifespan greater than 0, false otherwise.
     */
    boolean isAlive() {
        return lifespan > 0 || !tasks.isEmpty();
    }

    double getProgress() {
        return bossBar.getProgress();
    }

    interface Task {
        /**
         * Return true if this object is continues to be alive, false
         * otherwise. Returning false will remove it from the tasks
         * list.
         */
        boolean tick();
    }

    void skillPointsProgress(final int level, final int from, final int to, final int max) {
        for (Task task : tasks) {
            if (!(task instanceof SkillPointsProgressTask)) continue;
            SkillPointsProgressTask prog = (SkillPointsProgressTask) task;
            if (prog.level != level) continue;
            // Found it!
            prog.to = to;
            return;
        }
        // New
        SkillPointsProgressTask prog = new SkillPointsProgressTask(level);
        prog.from = from;
        prog.current = from;
        prog.to = to;
        prog.max = max;
        tasks.add(prog);
    }

    @RequiredArgsConstructor
    final class SkillPointsProgressTask implements Task {
        final int level;
        int ticks = 0;
        int from;
        int current;
        int to;
        int max;

        @Override
        public boolean tick() {
            if (ticks > 0) current += 1;
            bossBar.setProgress((double) current / (double) max);
            bossBar.setTitle(ChatColor.GRAY + skillType.displayName + " " + level);
            bossBar.setColor(BarColor.WHITE);
            bossBar.setStyle(BarStyle.SEGMENTED_20);
            ticks += 1;
            return current < to;
        }
    }
}
