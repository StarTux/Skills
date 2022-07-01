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
    MINE_STRIP(TalentTag.MINE_STRIP, SkillType.MINING, null),
    MINE_ORE_ALERT(TalentTag.MINE_ORE_ALERT, SkillType.MINING, TalentType.MINE_STRIP),
    MINE_XRAY(TalentTag.MINE_XRAY, SkillType.MINING, TalentType.MINE_ORE_ALERT),
    MINE_SILK_STRIP(TalentTag.MINE_SILK_STRIP, SkillType.MINING, TalentType.MINE_STRIP),
    MINE_SILK_MULTI(TalentTag.MINE_SILK_MULTI, SkillType.MINING, TalentType.MINE_SILK_STRIP),
    // Combat
    SEARING(TalentTag.SEARING, SkillType.COMBAT, null),
    PYROMANIAC(TalentTag.PYROMANIAC, SkillType.COMBAT, SEARING),
    DENIAL(TalentTag.DENIAL, SkillType.COMBAT, null), // +slow?
    GOD_MODE(TalentTag.GOD_MODE, SkillType.COMBAT, TalentType.DENIAL),
    ARCHER_ZONE(TalentTag.ARCHER_ZONE, SkillType.COMBAT, null),
    IRON_AGE(TalentTag.IRON_AGE, SkillType.COMBAT, null),
    EXECUTIONER(TalentTag.EXECUTIONER, SkillType.COMBAT, TalentType.IRON_AGE),
    IMPALER(TalentTag.IMPALER, SkillType.COMBAT, TalentType.IRON_AGE),
    TOXICIST(TalentTag.TOXICIST, SkillType.COMBAT, TalentType.DENIAL),
    TOXIC_FUROR(TalentTag.TOXIC_FUROR, SkillType.COMBAT, TalentType.TOXICIST);

    public final TalentTag tag;
    public final String key;
    public final SkillType skillType;
    public final TalentType depends;
    public final String displayName;
    public static final int COUNT = 15;
    public static final Map<SkillType, Set<TalentType>> SKILL_MAP = new EnumMap<>(SkillType.class);
    private Talent talent;

    TalentType(final TalentTag tag, final SkillType skillType, final TalentType depends) {
        this.tag = tag;
        this.key = name().toLowerCase();
        this.skillType = skillType;
        this.depends = depends;
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

        public static final TalentTag MINE_STRIP = new
            TalentTag("Strip Mining",
                      Material.DIAMOND_PICKAXE, 5, 3,
                      "Mining with an Efficiency pickaxe breaks many block",
                      "Unleash the full power of the Efficency enchantment."
                      + " Mining stone type blocks will break several blocks"
                      + " within your line of sight while mining straight."
                      + " Breaking ores will attempt to break the entire vein."
                      + " This only works deep underground.",
                      "Mine without this feature by sneaking.");

        public static final TalentTag MINE_ORE_ALERT = new
            TalentTag("Ore Alert",
                      Material.DIAMOND_ORE, 5, 4,
                      "Get alerts when diamond ore is nearby",
                      "Never miss diamonds near your mine again!"
                      + " Whenever you mine stone at diamond level,"
                      + " and there is diamond ore nearby,"
                      + " an alert sound will notify you of its existence."
                      + " Follow that lead to earn more diamonds.");

        public static final TalentTag MINE_XRAY = new
            TalentTag("Super Vision",
                      Material.GLOWSTONE, 6, 4,
                      "Mining stone with a Fortune pickaxe"
                      + " allows you to see through solid stone",
                      "Nearby stone will be rendered see-through"
                      + " for a few seconds so you can identify ores more easily.");

        public static final TalentTag MINE_SILK_STRIP = new
            TalentTag("Silk Stripping",
                      Material.GOLD_NUGGET, 5, 2,
                      "Use a Silk Touch pickaxe to strip an ore of its gems",
                      "Right-click with a Silk Touch pickaxe to use your"
                      + " fine motory skills and remove those"
                      + " gems right from the ore block."
                      + "With any luck, you may repeat the procedure"
                      + " as long as the ore stays intact,"
                      + " getting more and more drops."
                      + " Eventually, the ore will turn into stone and"
                      + " you get the usual skill points for mining.",
                      "This method may yield as much reward as Fortune 3"
                      + " but is more random."
                      + " It allows multiplying drops from ores usually"
                      + " unaffected by Fortune: Iron and gold.");

        public static final TalentTag MINE_SILK_MULTI = new
            TalentTag("Silk Fortune",
                      Material.GOLD_INGOT, 6, 2,
                      "Silk stripping may yield even more drops from the same ore",
                      "While using your Silk Touch pickaxe on ores,"
                      + " this talent gives you an even greater chance"
                      + " at getting multiple drops,"
                      + " surpassing the yield capabilities of Fortune 3.",
                      "The yields of this method may exceed those of Fortune 3"
                      + " but are more random."
                      + " It allows multiple drops from ores usually"
                      + " unaffected by Fortune: Iron and gold.");

        public static final TalentTag SEARING = new
            TalentTag("Searing", Material.SOUL_CAMPFIRE, 4, 4, "Monsters set on fire deal -30% melee damage");

        public static final TalentTag PYROMANIAC = new
            TalentTag("Pyromaniac", Material.CAMPFIRE, 4, 5, "Monsters set on fire take +30% damage");

        public static final TalentTag DENIAL = new
            TalentTag("Denial", Material.BARRIER, 5, 3, "Knockback denies mob spells, projectiles, poison");

        public static final TalentTag TOXICIST = new
            TalentTag("Toxicist", Material.POISONOUS_POTATO, 6, 2, "Bane of Arthropods deals extra damage against poisoned mobs");

        public static final TalentTag TOXIC_FUROR = new
            TalentTag("Toxic Furor", Material.EXPERIENCE_BOTTLE, 7, 2, "Deal extra damage while affected by Poison, Wither or Nausea");

        public static final TalentTag GOD_MODE = new
            TalentTag("God Mode", Material.TOTEM_OF_UNDYING, 6, 3, "");

        public static final TalentTag ARCHER_ZONE = new
            TalentTag("In The Zone",
                      Material.SPECTRAL_ARROW, 4, 2,
                      "Ranged kills give 5 seconds of double damage to ranged attacks");

        public static final TalentTag IRON_AGE = new
            TalentTag("Iron Age",
                      Material.IRON_SWORD, 3, 3,
                      "Iron weapons deal +1 base damage");

        public static final TalentTag EXECUTIONER = new
            TalentTag("Executioner",
                      Material.IRON_AXE, 2, 3,
                      "Fully charged axe attacks kill mobs under 10% health");

        public static final TalentTag IMPALER = new
            TalentTag("Impaler",
                      Material.TRIDENT, 2, 2,
                      "Consecutive hits with a fully charged Impaling weapon against the same foe deal increasing damage");
    }
}
