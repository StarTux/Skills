package com.cavetale.skills.skill;

import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.Emoji;
import com.cavetale.core.font.GlyphPolicy;
import com.cavetale.core.item.ItemKinds;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;

@Getter
public enum SkillType implements ComponentLike {
    MINING("Mining", NamedTextColor.DARK_AQUA, BossBar.Color.BLUE) {
        @Override public ItemStack createIcon() {
            return icon(Material.GOLDEN_PICKAXE);
        }
        @Override public List<String> getRawDescription() {
            return List.of("Mine ores to level up."
                           + " Talents help you find and exploit ores.",
                           "Every ore you mine yields some skill points."
                           + " Diamond and emerald ore give you an"
                           + " additional chance to earn talent points.");
        }
    },
    COMBAT("Combat", NamedTextColor.RED, BossBar.Color.RED) {
        @Override public ItemStack createIcon() {
            return icon(Material.GOLDEN_SWORD);
        }
        @Override public List<String> getRawDescription() {
            return List.of("Kill monsters to level up."
                           + " Talents enhance your combat strength.",
                           "Each monster kill gives some skill points."
                           + " However, they decrease quickly if you stay in place.");
        }
    },
    ;

    public final String key;
    public final String displayName;
    public final TextColor textColor;
    public final BossBar.Color bossBarColor;
    public final List<Component> description;
    private Skill skill;

    SkillType(final String displayName, final TextColor textColor, final BossBar.Color bossBarColor) {
        this.key = name().toLowerCase();
        this.displayName = displayName;
        this.textColor = textColor;
        this.bossBarColor = bossBarColor;
        this.description = List.copyOf(computeDescription());
    }

    public abstract ItemStack createIcon();

    public abstract List<String> getRawDescription();

    public Component getIconComponent() {
        return ItemKinds.icon(createIcon());
    }

    public Component getDisplayComponent() {
        return text(displayName, textColor);
    }

    @Override
    public Component asComponent() {
        String cmd = "/sk " + key;
        return join(noSeparators(), getIconComponent(), getDisplayComponent())
            .hoverEvent(showText(text(cmd, textColor)))
            .clickEvent(runCommand(cmd));
    }

    public static SkillType ofKey(String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }

    public static SkillType require(String key) {
        SkillType result = ofKey(key);
        if (result == null) throw new CommandWarn("Unknown skill type: " + key);
        return result;
    }

    protected void register(final Skill theSkill) {
        if (this.skill != null) {
            skillsPlugin().getLogger().warning("Duplicate skill registration: " + this);
        }
        this.skill = theSkill;
    }

    private static ItemStack icon(Material mat) {
        ItemStack item = new ItemStack(mat);
        item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return item;
    }

    protected List<Component> computeDescription() {
        List<Component> result = new ArrayList<>();
        for (String string : getRawDescription()) {
            result.add(Emoji.replaceText(string, GlyphPolicy.HIDDEN, false).asComponent());
        }
        return result;
    }
}
