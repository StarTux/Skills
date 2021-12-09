package com.cavetale.skills.advancement;

import java.util.Map;
import lombok.Data;

@Data
final class CriteriaJson {
    protected Map<String, String> impossible = Map.of("trigger", "minecraft:impossible");
}
