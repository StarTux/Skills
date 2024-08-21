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
import java.util.function.Supplier;
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
@Getter
public abstract class Talent {
    protected final TalentType talentType;
    protected final String displayName;
    protected final List<String> rawDescription;
    protected final List<Component> description;
    protected final List<TalentLevel> levels = new ArrayList<>();

    protected Talent(final TalentType talentType, final String displayName, final String... rawDescription) {
        this.talentType = talentType;
        this.displayName = displayName;
        this.rawDescription = List.of(rawDescription);
        this.description = makeDescription(rawDescription);
        talentType.register(this);
    }

    /**
     * Create the icon.
     */
    public abstract ItemStack createIcon();

    public final TalentLevel getLevel(int level) {
        if (level < 1) {
            return null;
        } else if (level > levels.size()) {
            return levels.get(levels.size() - 1);
        } else {
            return levels.get(level - 1);
        }
    }

    public final TalentLevel getMaxLevel() {
        return levels.get(levels.size() - 1);
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

    /**
     * Get the anvil enchantments which this talent unlocks.  Session
     * collects them for a player.  CraftingListener wants to know in
     * order to modify the recipe.
     */
    public List<AnvilEnchantment> getAnvilEnchantments(Session session) {
        return List.of();
    }

    protected final void addLevel(final int talentPointCost, final Supplier<ItemStack> iconSupplier, String... rawLevelDescription) {
        TalentLevel level = new TalentLevel(levels.size() + 1, talentPointCost, iconSupplier, List.of(rawLevelDescription), makeDescription(rawLevelDescription));
        levels.add(level);
    }

    protected final void addLevel(final int talentPointCost, String... rawLevelDescription) {
        TalentLevel level = new TalentLevel(levels.size() + 1, talentPointCost, this::createIcon, List.of(rawLevelDescription), makeDescription(rawLevelDescription));
        levels.add(level);
    }

    /**
     * Get the description as components.
     */
    private static List<Component> makeDescription(String[] raw) {
        List<Component> result = new ArrayList<>();
        for (String string : raw) {
            result.add(Emoji.replaceText(string, GlyphPolicy.HIDDEN, false).asComponent());
        }
        return result;
    }

    protected static ItemStack createIcon(Material material) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return item;
    }

    protected static ItemStack createIcon(Mytems mytems) {
        ItemStack item = mytems.createItemStack();
        item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return item;
    }
}
