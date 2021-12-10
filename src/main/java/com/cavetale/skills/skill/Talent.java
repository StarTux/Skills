package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Util;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Implementation of a talent.  Talents are constructed by their
 * Skill.  The super constructor registers the talent with the
 * TalentType enum.  The Skills object will enable them all and
 * register their events if they implement Listener.  Thus, enable()
 * will oftentimes be empty.
 */
public abstract class Talent {
    protected final SkillsPlugin plugin;
    @Getter protected final TalentType talentType;

    protected Talent(final SkillsPlugin plugin, final TalentType talentType) {
        this.plugin = plugin;
        this.talentType = talentType;
        talentType.register(this);
    }

    protected abstract void enable();

    /**
     * Check if a player should be able to use this talent right now.  This implies:
     * - Player has the basic Skills permission
     * - Player is in corrent GameMode
     * - Talent is unlocked
     * - Talent is not disabled
     */
    protected final boolean isPlayerEnabled(Player player) {
        return Util.playMode(player)
            && plugin.sessions.isTalentEnabled(player, talentType);
    }
}
