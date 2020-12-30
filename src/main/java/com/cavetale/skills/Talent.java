package com.cavetale.skills;

import com.cavetale.skills.util.Items;
import com.cavetale.skills.util.Msg;
import com.cavetale.skills.util.Text;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Getter
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
    public final SkillType skillType;
    private String displayName;
    private String description = "";
    private Material material = Material.STICK;
    private String iconNBT = null;
    private Talent depends = null;
    private boolean terminal = true;
    private int guiIndex;
    private static final HashMap<String, Talent> KEY_MAP = new HashMap<>();

    Talent() {
        key = name().toLowerCase();
        this.displayName = Msg.enumToCamelCase(this);
        switch (name().split("_", 2)[0]) {
        case "MINE": skillType = SkillType.MINING; break;
        case "FARM": skillType = SkillType.FARMING; break;
        case "COMBAT": skillType = SkillType.COMBAT; break;
        default: skillType = null;
        }
    }

    static {
        for (Talent talent : values()) {
            KEY_MAP.put(talent.key, talent);
            KEY_MAP.put(talent.key.replace("_", ""), talent);
        }
    }

    private static String lore(String... in) {
        return String.join("\n\n", in);
    }

    private static String par(String... in) {
        return String.join(" ", in);
    }

    @SuppressWarnings("checkstyle:LineLength")
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

    static void loadStatic(ConfigurationSection config) {
        for (Talent talent : Talent.values()) {
            ConfigurationSection section = config.getConfigurationSection(talent.key);
            if (section == null) {
                throw new IllegalStateException("Section missing: " + talent.key);
            }
            talent.load(section);
        }
    }

    void load(ConfigurationSection section) {
        displayName = section.getString("displayName");
        description = section.getString("description");
        String itemKey = section.getString("icon.material");
        material = Material.getMaterial(itemKey);
        if (material == null) material = Material.STONE;
        iconNBT = section.getString("icon.nbt");
    }

    private static void chain(Talent... talents) {
        for (int i = 0; i < talents.length - 1; i += 1) {
            talents[i + 1].depends = talents[i];
            talents[i].terminal = false;
        }
    }

    public static Talent of(@NonNull String key) {
        return KEY_MAP.get(key);
    }

    public List<Talent> getDependants() {
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
