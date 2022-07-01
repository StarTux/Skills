package com.cavetale.skills.skill;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.util.Enums;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;

@Getter
public enum TalentType {
    // Mining
    STRIP_MINING(TalentTag.STRIP_MINING, SkillType.MINING, null, 1),
    VEIN_MINING(TalentTag.VEIN_MINING, SkillType.MINING, STRIP_MINING, 1),
    VEIN_GEMS(TalentTag.VEIN_GEMS, SkillType.MINING, VEIN_MINING, 2),
    VEIN_METALS(TalentTag.VEIN_METALS, SkillType.MINING, VEIN_MINING, 2),
    SILK_STRIP(TalentTag.SILK_STRIP, SkillType.MINING, VEIN_MINING, 2),
    SILK_METALS(TalentTag.SILK_METALS, SkillType.MINING, SILK_STRIP, 3),
    SILK_MULTI(TalentTag.SILK_MULTI, SkillType.MINING, SILK_STRIP, 3),
    MINER_SIGHT(TalentTag.MINER_SIGHT, SkillType.MINING, null, 1),
    SUPER_VISION(TalentTag.SUPER_VISION, SkillType.MINING, MINER_SIGHT, 5),
    NETHER_VISION(TalentTag.NETHER_VISION, SkillType.MINING, SUPER_VISION, 5),
    ORE_ALERT(TalentTag.ORE_ALERT, SkillType.MINING, MINER_SIGHT, 3),
    EMERALD_ALERT(TalentTag.EMERALD_ALERT, SkillType.MINING, ORE_ALERT, 4),
    DEBRIS_ALERT(TalentTag.DEBRIS_ALERT, SkillType.MINING, EMERALD_ALERT, 5),
    // Combat
    SEARING(TalentTag.SEARING, SkillType.COMBAT, null, 1),
    PYROMANIAC(TalentTag.PYROMANIAC, SkillType.COMBAT, SEARING, 2),
    DENIAL(TalentTag.DENIAL, SkillType.COMBAT, null, 1), // +slow?
    GOD_MODE(TalentTag.GOD_MODE, SkillType.COMBAT, TalentType.DENIAL, 3),
    ARCHER_ZONE(TalentTag.ARCHER_ZONE, SkillType.COMBAT, null, 3),
    IRON_AGE(TalentTag.IRON_AGE, SkillType.COMBAT, null, 1),
    EXECUTIONER(TalentTag.EXECUTIONER, SkillType.COMBAT, TalentType.IRON_AGE, 3),
    IMPALER(TalentTag.IMPALER, SkillType.COMBAT, TalentType.IRON_AGE, 3),
    TOXICIST(TalentTag.TOXICIST, SkillType.COMBAT, TalentType.DENIAL, 2),
    TOXIC_FUROR(TalentTag.TOXIC_FUROR, SkillType.COMBAT, TalentType.TOXICIST, 3);

    public final TalentTag tag;
    public final String key;
    public final SkillType skillType;
    public final TalentType depends;
    public final int talentPointCost;
    public final String displayName;
    public static final int COUNT = 15; // unused
    public static final Map<SkillType, Set<TalentType>> SKILL_MAP = new EnumMap<>(SkillType.class);
    private Talent talent;

    TalentType(final TalentTag tag, final SkillType skillType, final TalentType depends, final int talentPointCost) {
        this.tag = tag;
        this.key = name().toLowerCase();
        this.skillType = skillType;
        this.depends = depends;
        this.talentPointCost = talentPointCost;
        this.displayName = Enums.human(this);
    }

    static {
        for (TalentType talent : TalentType.values()) {
            SKILL_MAP.computeIfAbsent(talent.skillType, sk -> EnumSet.of(talent))
                .add(talent);
        }
    }

    public static TalentType of(@NonNull String key) {
        for (TalentType t : TalentType.values()) {
            if (key.equals(t.key)) return t;
        }
        return null;
    }

    public boolean isTerminal() {
        for (TalentType it : TalentType.values()) {
            if (it.depends == this) return false;
        }
        return true;
    }

    protected void register(final Talent newTalent) {
        if (this.talent != null) {
            SkillsPlugin.getInstance().getLogger().warning("Duplicate talent registration: " + this);
        }
        this.talent = newTalent;
    }

    public record TalentTag(String title, Material icon, int x, int y,
                                   String legacyDescription, String... legacyMoreText) {
    //STRIP_MINING(TalentTag.STRIP_MINING, SkillType.MINING, null, 1),
        public static final TalentTag STRIP_MINING = new
            TalentTag("Strip Mining",
                      Material.STONE_PICKAXE, 5, 3,
                      "Mining with an Efficiency pickaxe breaks many block",
                      "Unleash the full power of the Efficency enchantment."
                      + " Mining stone type blocks will break several blocks"
                      + " within a line while mining straight."
                      + " Stone includes: Stone, Andesite, Diorite, Granite,"
                      + " Tuff, Deepslate",
                      "Mine without this feature by sneaking.");
    //VEIN_MINING(TalentTag.VEIN_MINING, SkillType.MINING, STRIP_MINING, 1),
        public static final TalentTag VEIN_MINING = new
            TalentTag("Vein Mining - Basic",
                      Material.IRON_PICKAXE, 6, 3,
                      "Mining certain ores will attempt to break the entire vein",
                      "Works on Coal, Redstone and Lapis Lazuli Ores."
                      + "Requires the Efficiency enchantment on your pickaxe.",
                      "Mine without this feature by sneaking.");
    //VEIN_GEMS(TalentTag.VEIN_GEMS, SkillType.MINING, VEIN_MINING, 2),
        public static final TalentTag VEIN_GEMS = new
            TalentTag("Vein Mining - Gems",
                      Material.DIAMOND_PICKAXE, 7, 2,
                      "Mining certain ores will attempt to break the entire vein",
                      "Works on Diamond, Emerald and Quartz Ores."
                      + "Requires the Efficiency enchantment on your pickaxe.",
                      "Mine without this feature by sneaking.");
    //VEIN_METALS(TalentTag.VEIN_METALS, SkillType.MINING, VEIN_MINING, 2),
        public static final TalentTag VEIN_METALS = new
            TalentTag("Vein Mining - Metals",
                      Material.NETHERITE_PICKAXE, 7, 3,
                      "Mining certain ores will attempt to break the entire vein",
                      "Works on Ancient Debris, Copper, Iron and Gold Ores"
                      + "Requires the Efficiency enchantment on your pickaxe.",
                      "Mine without this feature by sneaking.");
    //SILK_STRIP(TalentTag.SILK_STRIP, SkillType.MINING, VEIN_MINING, 2),
        public static final TalentTag SILK_STRIP = new
            TalentTag("Silk Stripping",
                      Material.GOLD_NUGGET, 6, 4,
                      "Use a Silk Touch pickaxe to strip an ore of its contents",
                      "Right-click with a Silk Touch pickaxe to use your"
                      + " fine motory skills and remove those"
                      + " treasures right from the ore block."
                      + "With any luck, you may repeat the procedure"
                      + " as long as the ore stays intact,"
                      + " getting more and more drops."
                      + " Eventually, the ore will turn into stone and"
                      + " you get the usual skill points for mining.",
                      "This method may yield as much reward as Fortune 3"
                      + " but is more random.");
    //SILK_METALS(TalentTag.SILK_METALS, SkillType.MINING, SILK_STRIP, 3),
        public static final TalentTag SILK_METALS = new
            TalentTag("Silk Extraction",
                      Material.NETHERITE_SCRAP, 7, 5,
                      "Get more metal from Silk Stripping",
                      "Upgrade Silk Stripping to get more drops"
                      + " from metal ores. Works on Ancient Debris,"
                      + " Copper, Iron and Gold Ores",
                      "This method may yield as much reward as Fortune 4"
                      + " would, but it is more random.");

        public static final TalentTag SILK_MULTI = new
            TalentTag("Silk Fortune",
                      Material.GOLD_INGOT, 6, 5,
                      "Get more non-metallic drops from Silk Stripping",
                      "Upgrade Silk Stripping to get more drops"
                      + " from non-metallic ores. Works on everything"
                      + " but Ancient Debris, Copper, Iron and Gold Ores",
                      "This method may yield as much reward as Fortune 4"
                      + " would, but it is more random.");

        public static final TalentTag MINER_SIGHT = new
            TalentTag("Miner's Sight",
                      Material.TORCH, 3, 3,
                      "Mining stone with a pickaxe grants you Night Vision",
                      "Stone includes: Stone, Andesite, Diorite, Granite,"
                      + " Tuff, Deepslate");

        public static final TalentTag SUPER_VISION = new
            TalentTag("Super Vision",
                      Material.LANTERN, 3, 2,
                      "Mining stone with a Fortune pickaxe"
                      + " allows you to see through solid stone",
                      "Nearby stone will be rendered see-through"
                      + " for a few seconds so you can identify ores more easily.");

        public static final TalentTag NETHER_VISION = new
            TalentTag("Nether Vision",
                      Material.SOUL_LANTERN, 2, 2,
                      "Mining nether stone with a Fortune pickaxe"
                      + " allows you to see through nether stones",
                      "Nearby nether stone will be rendered see-through"
                      + " for a few seconds so you can identify ores more easily."
                      + " Nether stones include: Netherrack, Basalt, Blackstone");

        public static final TalentTag ORE_ALERT = new
            TalentTag("Diamond Ore Alert",
                      Material.DIAMOND_ORE, 3, 4,
                      "Get alerts when Diamond Ore is nearby",
                      "Whenever you break stone with a pickaxe and there is"
                      + " Diamond Ore nearby,"
                      + " an alert sound will notify you of its existence."
                      + " Follow that lead to earn more Diamonds.");

        public static final TalentTag EMERALD_ALERT = new
            TalentTag("Emerald Ore Alert",
                      Material.EMERALD_ORE, 2, 4,
                      "Get alerts when Emerald Ore is nearby"
                      + "Whenever you break stone with a pickaxe and there is"
                      + " Emerald Ore nearby,"
                      + " an alert sound will notify you of its existence."
                      + " Follow that lead to earn more Emeralds.");

        public static final TalentTag DEBRIS_ALERT = new
            TalentTag("Ancient Debris Alert",
                      Material.ANCIENT_DEBRIS, 1, 4,
                      "Get alerts when Ancient Debris is nearby"
                      + "Whenever you break nether stones with a pickaxe and"
                      + " there is Ancient Debris nearby,"
                      + " an alert sound will notify you of its existence."
                      + " Follow that lead to earn more Ancient Debris."
                      + " Nether stones include: Netherrack, Basalt, Blackstone");

        public static final TalentTag SEARING = new
            TalentTag("Searing",
                      Material.SOUL_CAMPFIRE, 4, 4,
                      "Monsters set on fire deal -30% melee damage");

        public static final TalentTag PYROMANIAC = new
            TalentTag("Pyromaniac",
                      Material.CAMPFIRE, 4, 5,
                      "Monsters set on fire take +30% damage");

        public static final TalentTag DENIAL = new
            TalentTag("Denial",
                      Material.BARRIER, 5, 3,
                      "Knockback denies mob spells, projectiles, poison"
                      + " for 20 seconds");

        public static final TalentTag TOXICIST = new
            TalentTag("Toxicist",
                      Material.POISONOUS_POTATO, 6, 2,
                      "Bane of Arthropods deals extra damage"
                      + " against poisoned mobs",
                      "You deal +1 damage for every level of the"
                      + " Bane of Arthropods enchantment on your weapon");

        public static final TalentTag TOXIC_FUROR = new
            TalentTag("Toxic Furor",
                      Material.EXPERIENCE_BOTTLE, 7, 2,
                      "Deal extra damage while you are affected"
                      + " by Poison, Wither or Nausea",
                      "You deal +1 damage for every level of"
                      + " each of the listed effects");

        public static final TalentTag GOD_MODE = new
            TalentTag("God Mode",
                      Material.TOTEM_OF_UNDYING, 6, 3,
                      "Melee kills make you Immortal for 3 seconds",
                      "While Immportal, you can not die, but you will"
                      + " still take damage");

        public static final TalentTag ARCHER_ZONE = new
            TalentTag("In The Zone",
                      Material.SPECTRAL_ARROW, 4, 2,
                      "Ranged kills give 5 seconds of double damage"
                      + " to ranged attacks");

        public static final TalentTag IRON_AGE = new
            TalentTag("Iron Age",
                      Material.IRON_SWORD, 3, 3,
                      "Iron weapons deal +1 base damage",
                      "Iron weapons are Iron Sword and Iron Axe");

        public static final TalentTag EXECUTIONER = new
            TalentTag("Executioner",
                      Material.IRON_AXE, 2, 3,
                      "Fully charged axe attacks kill mobs under 10% health");

        public static final TalentTag IMPALER = new
            TalentTag("Impaler",
                      Material.TRIDENT, 2, 2,
                      "Consecutive fully charged hits with a Impaling weapon"
                      + " against the same foe deal increasing damage",
                      "+1 damage for each consecutive hit, up to +6");
    }
}
