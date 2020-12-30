package com.cavetale.skills;

import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

public final class SkillsTest {
    @Test
    public void test() throws Exception {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setSplitLines(false);
        final Yaml yaml = new Yaml(options);
        Talent.setup();
        Map<String, Object> config = new LinkedHashMap<>();
        for (Talent talent : Talent.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            config.put(talent.key, map);
            map.put("description", talent.getDescription());
            map.put("displayName", talent.getDisplayName());
            Map<String, Object> icon = new LinkedHashMap<>();
            map.put("icon", icon);
            icon.put("material", talent.getMaterial().getKey().toString());
            icon.put("nbt", talent.getIconNBT());
        }
        yaml.dump(config, new FileWriter("target/test.yml"));
    }
}
