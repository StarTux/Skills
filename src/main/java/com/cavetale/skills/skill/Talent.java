package com.cavetale.skills.skill;

import com.cavetale.core.font.Emoji;
import com.cavetale.core.font.GlyphPolicy;
import com.cavetale.skills.util.Players;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessions;

/**
 * Implementation of a talent.  Talents are constructed by their
 * Skill.  The super constructor registers the talent with the
 * TalentType enum.  The Skills object will enable them all and
 * register their events if they implement Listener.  Thus, enable()
 * will oftentimes be empty.
 */
public abstract class Talent {
    @Getter protected final TalentType talentType;
    @Getter protected final List<Component> description;

    protected Talent(final TalentType talentType) {
        this.talentType = talentType;
        this.description = computeDescription();
        talentType.register(this);
    }

    /**
     * Get the display name.
     */
    public abstract String getDisplayName();

    /**
     * Get the raw description.
     */
    public abstract List<String> getRawDescription();

    /**
     * Create the icon.
     */
    public abstract ItemStack createIcon();

    /**
     * Get the description as components.
     */
    public List<Component> computeDescription() {
        List<Component> result = new ArrayList<>();
        for (String string : getRawDescription()) {
            result.add(Emoji.replaceText(string, GlyphPolicy.HIDDEN, false).asComponent());
        }
        return result;
    }

    /**
     * Check if a player should be able to use this talent right now.  This implies:
     * - Player has the basic Skills permission
     * - Player is in corrent GameMode
     * - Talent is unlocked
     * - Talent is not disabled
     */
    protected final boolean isPlayerEnabled(Player player) {
        return Players.playMode(player)
            && sessions().isTalentEnabled(player, talentType);
    }

    protected final ItemStack createIcon(Material material) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return item;
    }
}
