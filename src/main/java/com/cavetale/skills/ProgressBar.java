package com.cavetale.skills;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

final class ProgressBar {
    private BossBar bossBar;
    int lifespan = -1;
    double progressTarget;
    @Setter private Runnable postAnimation;
    int animationTicks;
    SkillType skill;

    ProgressBar(final String name, final BarColor color, final BarStyle style) {
        bossBar = Bukkit.getServer().createBossBar(name, color, style);
    }

    ProgressBar(final String name) {
        this(name, BarColor.BLUE, BarStyle.SOLID);
    }

    void add(Player player) {
        bossBar.addPlayer(player);
    }

    void remove(Player player) {
        bossBar.removePlayer(player);
    }

    void clear() {
        bossBar.removeAll();
    }

    void show() {
        bossBar.setVisible(true);
    }

    void hide() {
        bossBar.setVisible(false);
    }

    void setTitle(String title) {
        bossBar.setTitle(title);
    }

    /**
     * Set the current progress.  This cancels the ongoing animation,
     * if any.
     *
     * @param progress the progress
     */
    void setProgress(double progress) {
        bossBar.setProgress(progress);
        animationTicks = 0;
        postAnimation = null;
    }

    /**
     * Reach a progress within some ticks.
     *
     * @param progress the progress
     * @param ticks the ticks
     */
    void animateProgress(double progress, int ticks) {
        progressTarget = progress;
        animationTicks = ticks;
    }

    /**
     * Cancel the current animation.
     */
    void cancelAnimation() {
        animationTicks = 0;
    }

    /**
     * Reduce lifespan by 1 and hide if 0 is reached.
     *
     * @return true if still alive, false otherwise.
     */
    boolean tick() {
        if (lifespan == 0) return false;
        if (lifespan > 0) {
            lifespan -= 1;
            if (lifespan == 0) {
                hide();
                return false;
            }
        }
        if (animationTicks == 1) {
            animationTicks = 0;
            bossBar.setProgress(progressTarget);
            if (postAnimation != null) {
                postAnimation.run();
                postAnimation = null;
            }
        } else if (animationTicks > 0) {
            double current = bossBar.getProgress();
            double step = progressTarget - current;
            step /= (double) animationTicks;
            bossBar.setProgress(current + step);
            animationTicks -= 1;
        }
        return true;
    }

    /**
     * @return true if lifespan greater than 0, false otherwise.
     */
    boolean isAlive() {
        return lifespan > 0;
    }

    boolean isAnimating() {
        return animationTicks > 0;
    }

    /**
     * Set the lifespan.  A positive livespan implies the bar is
     * visible but will hide once the life ticks run out.  A zeri
     * lifespan implies hiding.  A negative lifespan implies infinite
     * life but manual hiding or showing.
     *
     * @args life the lifespan ticks
     */
    void setLifespan(final int life) {
        if (life > 0) {
            show();
        } else if (life == 0) {
            hide();
        }
        lifespan = life;
    }

    double getProgress() {
        if (animationTicks > 0) return progressTarget;
        return bossBar.getProgress();
    }
}
