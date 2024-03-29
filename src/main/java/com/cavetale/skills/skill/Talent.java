package com.cavetale.skills.skill;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.font.Emoji;
import com.cavetale.core.font.GlyphPolicy;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.crafting.AnvilEnchantment;
import com.cavetale.skills.session.Session;
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
    public final boolean isPlayerEnabled(Player player) {
        switch (NetworkServer.current()) {
        case FESTIVAL:
            return false;
        default:
            return Players.playMode(player)
                && sessions().isTalentEnabled(player, talentType);
        }
    }

    protected final ItemStack createIcon(Material material) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return item;
    }

    protected final ItemStack createIcon(Mytems mytems) {
        ItemStack item = mytems.createItemStack();
        item.editMeta(meta -> {
                meta.displayName(null);
                meta.lore(List.of());
                meta.addItemFlags(ItemFlag.values());
            });
        return item;
    }

    /**
     * Get the anvil enchantments which this talent unlocks.  Session
     * collects them for a player.  CraftingListener wants to know in
     * order to modify the recipe.
     */
    public List<AnvilEnchantment> getAnvilEnchantments(Session session) {
        return List.of();
    }
}
