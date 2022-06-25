package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.util.Players;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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
    @Getter protected String description = "";
    @Getter protected List<Component> infoPages = List.of();

    protected Talent(final SkillsPlugin plugin, final TalentType talentType) {
        this.plugin = plugin;
        this.talentType = talentType;
        this.description = talentType.tag.legacyDescription();
        this.infoPages = new ArrayList<>();
        for (String text : talentType.tag.legacyMoreText()) {
            infoPages.add(Component.text(text));
        }
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
        return Players.playMode(player)
            && plugin.sessions.isTalentEnabled(player, talentType);
    }
}
