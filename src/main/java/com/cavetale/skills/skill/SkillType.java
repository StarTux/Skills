package com.cavetale.skills.skill;

import com.cavetale.core.item.ItemKinds;
import com.cavetale.skills.SkillsPlugin;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;

@Getter
public enum SkillType implements ComponentLike {
    MINING(SkillTag.MINING),
    COMBAT(SkillTag.COMBAT);

    public final String key;
    public final String displayName;
    public final SkillTag tag;
    private Skill skill;

    SkillType(final SkillTag tag) {
        this.key = name().toLowerCase();
        this.displayName = toCamelCase(" ", this);
        this.tag = tag;
    }

    public ItemStack createIcon() {
        return tag.iconSupplier.get();
    }

    public Component getIconComponent() {
        return ItemKinds.icon(tag.iconSupplier.get());
    }

    public Component getDisplayComponent() {
        return text(displayName, tag.color());
    }

    @Override
    public Component asComponent() {
        String cmd = "/sk " + key;
        return join(noSeparators(), getIconComponent(), getDisplayComponent())
            .hoverEvent(showText(text(cmd, tag.color())))
            .clickEvent(runCommand(cmd));
    }

    public static SkillType ofKey(@NonNull String key) {
        for (SkillType s : SkillType.values()) {
            if (key.equals(s.key)) return s;
        }
        return null;
    }

    protected void register(final Skill theSkill) {
        if (this.skill != null) {
            SkillsPlugin.getInstance().getLogger().warning("Duplicate skill registration: " + this);
        }
        this.skill = theSkill;
    }

    private static ItemStack item(Material mat) {
        ItemStack item = new ItemStack(mat);
        item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return item;
    }

    // This tag is used for GUIs.
    public record SkillTag(String title, TextColor color, BossBar.Color bossBarColor,
                           String description,
                           Supplier<ItemStack> iconSupplier, String background,
                           String... moreText) {
        private static final SkillTag MINING = new
            SkillTag("Mining", NamedTextColor.DARK_AQUA, BossBar.Color.BLUE,
                     "Mine ores to get SP."
                     + " Talents help you find and exploit ores.",
                     () -> item(Material.GOLDEN_PICKAXE),
                     "minecraft:textures/block/deepslate_diamond_ore.png",
                     "Every ore you mine yields some skill points."
                     + " Diamond and emerald ore give you an"
                     + " additional chance to earn talent points.");

        private static final SkillTag COMBAT = new
            SkillTag("Combat", NamedTextColor.RED, BossBar.Color.RED,
                     "Kill monsters to get SP."
                     + " Unlock talents to enhance your combat strength.",
                     () -> item(Material.GOLDEN_SWORD),
                     "minecraft:textures/block/iron_block.png",
                     "Each monster kill gives some skill points."
                     + " However, they decrease quickly if you stay in place.");
    }
}
