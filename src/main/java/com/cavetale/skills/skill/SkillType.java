package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Util;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

@Getter
public enum SkillType {
    MINING(SkillTag.MINING),
    FARMING(SkillTag.FARMING),
    COMBAT(SkillTag.COMBAT);

    public final String key;
    public final String displayName;
    public final SkillTag tag;
    private Skill skill;

    SkillType(final SkillTag tag) {
        this.key = name().toLowerCase();
        this.displayName = Util.niceEnumName(this);
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
    public final record SkillTag(String title, String description, Material icon, String background, String... moreText) {
        private static final SkillTag MINING = new
            SkillTag("Mining",
                     "Mine ores to get SP."
                     + " Talents help you find and exploit ores.",
                     Material.GOLDEN_PICKAXE,
                     "minecraft:textures/block/deepslate_diamond_ore.png",
                     "Every ore you mine yields some skill points."
                     + " Diamond and emerald ore give you an"
                     + " additional chance to earn talent points.");

        private static final SkillTag FARMING = new
            SkillTag("Farming",
                     "Water your crops with the growstick"
                     + " and harvest them once they are fully grown.",
                     Material.GOLDEN_HOE,
                     "minecraft:textures/block/farmland_moist.png",
                     "Use a stick on any freshly planted crop to water it."
                     + " In the daylight it will grow quickly."
                     + " Fully grown watered crops yield a"
                     + " chance at dropping diamonds and talent points.");

        private static final SkillTag COMBAT = new
            SkillTag("Combat",
                     "Kill monsters to get SP."
                     + " Unlock talents to enhance your combat strength.",
                     Material.GOLDEN_SWORD,
                     "minecraft:textures/block/iron_block.png",
                     "Each monster kill gives some skill points."
                     + " However, they decrease quickly if you stay in place.");
    }
}
