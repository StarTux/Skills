package com.cavetale.skills;

import lombok.Data;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

@Data
public final class TalentInfo {
    protected String icon;
    protected String iconNBT;
    protected String title;
    protected String description;
    protected String background;

    TalentInfo(@NonNull final ConfigurationSection config) {
        icon = config.getString("icon");
        iconNBT = config.getString("iconNBT");
        title = config.getString("title");
        description = config.getString("description");
        background = config.getString("background");
    }

    TalentInfo(@NonNull final String name) {
        title = name;
        description = name;
        icon = "barrier";
    }
}
