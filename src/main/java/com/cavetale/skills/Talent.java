package com.cavetale.skills;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Talent {
    ROOT,
    // Mining
    MINE_STRIP,
    MINE_ORE_ALERT,
    MINE_XRAY,
    MINE_SILK_STRIP,
    MINE_SILK_MULTI,
    // Farming
    FARM_GROWSTICK_RADIUS,
    FARM_PLANT_RADIUS,
    FARM_CROP_DROPS,
    FARM_DIAMOND_DROPS,
    FARM_TALENT_POINTS,
    FARM_IRON_GROWTH,
    FARM_GOLD_GROWTH,
    FARM_DIAMOND_GROWTH,
    // Combat
    COMBAT_FIRE,
    COMBAT_SILENCE,
    COMBAT_SPIDERS,
    COMBAT_GOD_MODE,
    COMBAT_ARCHER_ZONE;

    public final String key;
    String displayName;
    String description = "";
    Material material = Material.STICK;
    String iconNBT = null;
    Talent depends = null;
    SkillType skill = null;
    boolean terminal = true;
    int guiIndex;
    private static final HashMap<String, Talent> KEY_MAP = new HashMap<>();

    static {
        for (Talent talent : values()) {
            KEY_MAP.put(talent.key, talent);
            KEY_MAP.put(talent.key.replace("_", ""), talent);
        }
    }

    static void setup() {
        // Chaining
        chain(ROOT, MINE_STRIP);
        chain(ROOT, FARM_GROWSTICK_RADIUS);
        chain(ROOT, COMBAT_FIRE);
        chain(MINE_STRIP,
              MINE_ORE_ALERT,
              MINE_XRAY);
        chain(MINE_STRIP,
              MINE_SILK_STRIP,
              MINE_SILK_MULTI);
        chain(FARM_GROWSTICK_RADIUS,
              FARM_PLANT_RADIUS);
        chain(FARM_GROWSTICK_RADIUS,
              FARM_CROP_DROPS,
              FARM_DIAMOND_DROPS,
              FARM_TALENT_POINTS);
        chain(COMBAT_FIRE,
              COMBAT_SILENCE,
              COMBAT_SPIDERS,
              COMBAT_GOD_MODE);
        chain(COMBAT_FIRE,
              COMBAT_ARCHER_ZONE);
        // Data Input
        ROOT.displayName = "Skill Talents";
        ROOT.description = "Earn talent points to unlock new talents and improve your skillful abilities!";
        ROOT.material = Material.GOLDEN_APPLE;
        MINE_STRIP.displayName = "Strip Mining";
        MINE_STRIP.description = "Mining with an Efficiency pickaxe breaks many blocks\n\nUnleash the full power of the Efficency enchantment. Mining stone type blocks will break several blocks within your line of sight while mining straight. Breaking ores will attempt to break the entire vein. This only works deep underground.\n\nMine without this feature by sneaking.";
        MINE_STRIP.material = Material.DIAMOND_PICKAXE;
        MINE_ORE_ALERT.displayName = "Ore Alert";
        MINE_ORE_ALERT.description = "Get alerts when diamond ore is nearby.\n\nNever miss diamonds near your mine again! Whenever you mine stone at diamond level, and there is diamond ore nearby, an alert sound will notify you of its existence. Follow that lead to earn more diamonds. This only works deep underground.";
        MINE_ORE_ALERT.material = Material.DIAMOND_ORE;
        MINE_XRAY.displayName = "X-Ray";
        MINE_XRAY.description = "Mining stone with a Fortune pickaxe allows you to see through solid stone.\n\nNearby stone will be rendered see-through for a few seconds so you can identify ores more easily.\n\nThis only works while mining stone near diamond level.";
        MINE_XRAY.material = Material.GLOWSTONE;
        MINE_SILK_STRIP.displayName = "Silk Stripping";
        MINE_SILK_STRIP.description = "Use a Silk Touch pickaxe to strip an ore of its gems.\n\nRight-click with a Silk Touch pickaxe to do use your fine motory skills and remove those gems right from the ore block. With any luck, you may repeat the procedure as long as the ore stays intact, getting more and more drops. Eventually, the ore will turn into stone and you get the usual skill points for mining.\n\nThis method may yield as much reward as Fortune 3 but is more random. It allows multiplying drops from ores usually unaffected by Fortune: Iron and gold.";
        MINE_SILK_STRIP.material = Material.GOLD_NUGGET;
        MINE_SILK_MULTI.displayName = "Silk Fortune";
        MINE_SILK_MULTI.description = "Silk stripping may yield even more drops from the same ore.\n\nWhile using your Silk Touch pickaxe on ores, this talent gives you an even greater chance at getting multiple drops, surpassing the yield capabilities of Fortune 3.\n\nThe yields of this method may exceed those of Fortune 3 but are more random. It allows multiple drops from ores usually unaffected by Fortune: Iron and gold.";
        MINE_SILK_MULTI.material = Material.GOLD_INGOT;
        FARM_GROWSTICK_RADIUS.displayName = "Spoutcraft";
        FARM_GROWSTICK_RADIUS.description = "Effective growstick radius +1.\n\nUp your gardening powers. Save lots of time by watering adjacent crops and soil, all at once!";
        FARM_GROWSTICK_RADIUS.material = Material.STICK;
        FARM_CROP_DROPS.displayName = "Cultivator";
        FARM_CROP_DROPS.description = "Increased crop yields.\n\nEach fully grown watered plant yields additional drops when you harvest them.";
        FARM_CROP_DROPS.material = Material.WHEAT;
        FARM_DIAMOND_DROPS.displayName = "Gem Growth";
        FARM_DIAMOND_DROPS.description = "Diamond drops 100% more common.\n\nFully grown watered plants sometimes yield diamonds. This talent increases your chances drastically.";
        FARM_DIAMOND_DROPS.material = Material.DIAMOND;
        FARM_TALENT_POINTS.displayName = "Grand Granger";
        FARM_TALENT_POINTS.description = "Talent points drop more often.\n\nWhenever a fully grown and watered plant drops a diamond, there is also a small progress made toward your next talent point. Unlocking this skill increases your chances. This means even more talent points!";
        FARM_TALENT_POINTS.material = Material.CORNFLOWER;
        FARM_PLANT_RADIUS.displayName = "Springtime";
        FARM_PLANT_RADIUS.description = "Plant seeds in a 3x3 area.\n\nSave time with this talent. Plant any seed (wheat, carrot, potato, beetroot, nether wart) and the surrounding 8 blocks will also be seeded where possible.\n\nPlant without this feature by sneaking.";
        FARM_PLANT_RADIUS.material = Material.WHEAT_SEEDS;
        COMBAT_FIRE.displayName = "Pyromaniac";
        COMBAT_FIRE.description = "Monsters set on fire deal -50% melee damage.\nMonsters set on fire take +50% damage.";
        COMBAT_FIRE.material = Material.CAMPFIRE;
        COMBAT_SILENCE.displayName = "Denial";
        COMBAT_SILENCE.description = "Monsters knocked back cannot shoot arrows or throw projectiles for 20 seconds.\n\nUse a Knockback weapon on an enemy to give it this status effect.";
        COMBAT_SILENCE.material = Material.BARRIER;
        COMBAT_SPIDERS.displayName = "Vamonos";
        COMBAT_SPIDERS.description = "Bane of Arthropods slows spiders and denies their poison effect for 30 seconds.";
        COMBAT_SPIDERS.material = Material.SPIDER_EYE;
        COMBAT_GOD_MODE.displayName = "God Mode";
        COMBAT_GOD_MODE.description = "Melee kills give 3 seconds of immortality.";
        COMBAT_GOD_MODE.material = Material.TOTEM_OF_UNDYING;
        COMBAT_ARCHER_ZONE.displayName = "In The Zone";
        COMBAT_ARCHER_ZONE.description = "Ranged kills give 5 seconds of double damage to ranged attacks.";
        COMBAT_ARCHER_ZONE.material = Material.SPECTRAL_ARROW;
        // Gui (generated)
        MINE_SILK_MULTI.guiIndex = 1;
        MINE_SILK_STRIP.guiIndex = 11;
        MINE_XRAY.guiIndex = 19;
        MINE_ORE_ALERT.guiIndex = 20;
        MINE_STRIP.guiIndex = 21;
        ROOT.guiIndex = 22;
        FARM_GROWSTICK_RADIUS.guiIndex = 23;
        FARM_PLANT_RADIUS.guiIndex = 24;
        COMBAT_FIRE.guiIndex = 32;
        FARM_CROP_DROPS.guiIndex = 33;
        COMBAT_ARCHER_ZONE.guiIndex = 41;
        COMBAT_SILENCE.guiIndex = 42;
        FARM_DIAMOND_DROPS.guiIndex = 43;
        COMBAT_GOD_MODE.guiIndex = 51;
        COMBAT_SPIDERS.guiIndex = 52;
        FARM_TALENT_POINTS.guiIndex = 53;
    }

    private static void chain(Talent... talents) {
        for (int i = 0; i < talents.length - 1; i += 1) {
            talents[i + 1].depends = talents[i];
            talents[i].terminal = false;
        }
    }

    Talent() {
        key = name().toLowerCase();
        this.displayName = Msg.enumToCamelCase(this);
    }

    public static Talent of(@NonNull String key) {
        return KEY_MAP.get(key);
    }

    List<Talent> getDependants() {
        return Stream.of(Talent.values())
            .filter(t -> t.depends == this)
            .collect(Collectors.toList());
    }

    public ItemStack getIcon() {
        return Items.of(material)
            .name(ChatColor.GOLD + displayName)
            .lore(wrap(description))
            .hide()
            .create();
    }

    public String getDisplayName() {
        return displayName;
    }

    static List<String> wrap(String in) {
        return Text.wrapMultiline(in, 28).stream()
            .map(s -> ChatColor.RESET + s)
            .collect(Collectors.toList());
    }
}
