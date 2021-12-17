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
    // Farming
    FARM_GROWSTICK_RADIUS(TalentTag.FARM_GROWSTICK_RADIUS, SkillType.FARMING, null),
    FARM_PLANT_RADIUS(TalentTag.FARM_PLANT_RADIUS, SkillType.FARMING, TalentType.FARM_GROWSTICK_RADIUS),
    FARM_CROP_DROPS(TalentTag.FARM_CROP_DROPS, SkillType.FARMING, TalentType.FARM_GROWSTICK_RADIUS),
    FARM_DIAMOND_DROPS(TalentTag.FARM_DIAMOND_DROPS, SkillType.FARMING, TalentType.FARM_CROP_DROPS),
    FARM_TALENT_POINTS(TalentTag.FARM_TALENT_POINTS, SkillType.FARMING, TalentType.FARM_DIAMOND_DROPS),
    // Combat
    COMBAT_FIRE(TalentTag.COMBAT_FIRE, SkillType.COMBAT, null),
    COMBAT_SILENCE(TalentTag.COMBAT_SILENCE, SkillType.COMBAT, TalentType.COMBAT_FIRE),
    COMBAT_SPIDERS(TalentTag.COMBAT_SPIDERS, SkillType.COMBAT, TalentType.COMBAT_SILENCE),
    COMBAT_GOD_MODE(TalentTag.COMBAT_GOD_MODE, SkillType.COMBAT, TalentType.COMBAT_SPIDERS),
    COMBAT_ARCHER_ZONE(TalentTag.COMBAT_ARCHER_ZONE, SkillType.COMBAT, TalentType.COMBAT_FIRE);

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

    public static record TalentTag(String title, Material icon, int x, int y,
                                   String description, String... moreText) {
        public static final TalentTag FARM_GROWSTICK_RADIUS = new
            TalentTag("Spoutcraft",
                      Material.STICK, 5, 3,
                      "Effective growstick radius +1",
                      "Up your gardening powers."
                      + " Save lots of time by watering"
                      + " adjacent crops and soil,"
                      + " all at once!");

        public static final TalentTag FARM_CROP_DROPS = new
            TalentTag("Cultivator",
                      Material.WHEAT, 5, 4,
                      "Increased crop yields",
                      "Each fully grown watered plant"
                      + " yields additional drops when"
                      + " you harvest them.");

        public static final TalentTag FARM_DIAMOND_DROPS = new
            TalentTag("Gem Growth",
                      Material.DIAMOND, 6, 4,
                      "Diamond drops 100% more common",
                      "Fully grown watered plants sometimes yield diamonds."
                      + " This talent increases your chances drastically.");

        public static final TalentTag FARM_TALENT_POINTS = new
            TalentTag("Grand Granger",
                      Material.CORNFLOWER, 7, 4,
                      "Talent points drop more often",
                      "Whenever a fully grown and watered plant drops a diamond,"
                      + " there is also a small progress made toward your next talent point."
                      + " Unlocking this skill increases your chances."
                      + " This means even more talent points!");

        public static final TalentTag FARM_PLANT_RADIUS = new
            TalentTag("Springtime",
                      Material.WHEAT_SEEDS, 5, 2,
                      "Plant seeds in a 3x3 area",
                      "Save time with this talent."
                      + " Plant any seed"
                      + " (wheat, carrot, potato, beetroot, nether wart)"
                      + " and the surrounding 8 blocks will also be seeded where possible.",
                      " Plant without this feature by sneaking.");

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
                      "Right-click with a Silk Touch pickaxe to do use your"
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

        public static final TalentTag COMBAT_FIRE = new
            TalentTag("Pyromaniac",
                      Material.CAMPFIRE, 5, 3,
                      "Monsters set on fire deal -50% melee damage."
                      + " Monsters set on fire take +50% damage");

        public static final TalentTag COMBAT_SILENCE = new
            TalentTag("Denial",
                      Material.BARRIER, 5, 2,
                      "Monsters knocked back cannot shoot arrows or throw projectiles for 20 seconds",
                      "Use a Knockback weapon on an enemy to give it this status effect.");

        public static final TalentTag COMBAT_SPIDERS = new
            TalentTag("Vamonos",
                      Material.SPIDER_EYE, 6, 2,
                      "Bane of Arthropods slows spiders and denies their poison effect for 30 seconds");

        public static final TalentTag COMBAT_GOD_MODE = new
            TalentTag("God Mode",
                      Material.TOTEM_OF_UNDYING, 7, 2,
                      "Melee kills give 3 seconds of immortality");

        public static final TalentTag COMBAT_ARCHER_ZONE = new
            TalentTag("In The Zone",
                      Material.SPECTRAL_ARROW, 5, 4,
                      "Ranged kills give 5 seconds of double damage to ranged attacks");
    }
}
