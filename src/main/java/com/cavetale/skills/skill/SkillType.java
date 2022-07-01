package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.util.Enums;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

@Getter
public enum SkillType {
    MINING(SkillTag.MINING),
    COMBAT(SkillTag.COMBAT);

    public final String key;
    public final String displayName;
    public final SkillTag tag;
    private Skill skill;

    SkillType(final SkillTag tag) {
        this.key = name().toLowerCase();
        this.displayName = Enums.human(this);
        this.tag = tag;
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

    // This tag is used for advancements and GUIs.
    public record SkillTag(String title, TextColor color, BossBar.Color bossBarColor,
                                 String description,
                                 Material icon, String background,
                                 String... moreText) {
        private static final SkillTag MINING = new
            SkillTag("Mining", NamedTextColor.DARK_AQUA, BossBar.Color.BLUE,
                     "Mine ores to get SP."
                     + " Talents help you find and exploit ores.",
                     Material.GOLDEN_PICKAXE,
                     "minecraft:textures/block/deepslate_diamond_ore.png",
                     "Every ore you mine yields some skill points."
                     + " Diamond and emerald ore give you an"
                     + " additional chance to earn talent points.");

        private static final SkillTag COMBAT = new
            SkillTag("Combat", NamedTextColor.RED, BossBar.Color.RED,
                     "Kill monsters to get SP."
                     + " Unlock talents to enhance your combat strength.",
                     Material.GOLDEN_SWORD,
                     "minecraft:textures/block/iron_block.png",
                     "Each monster kill gives some skill points."
                     + " However, they decrease quickly if you stay in place.");
    }
}
