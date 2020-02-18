package com.cavetale.skills;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

final class TalentInfo {
    String icon;
    String iconNBT;
    String title;
    String description;
    String background;

    TalentInfo(@NonNull final ConfigurationSection config) {
        icon = config.getString("icon");
        iconNBT = config.getString("iconNBT");
        title = config.getString("title");
        description = config.getString("description");
        background = config.getString("background");
    }

    TalentInfo(@NonNull final Talent talent) {
        title = talent.displayName;
        description = talent.displayName;
        icon = "barrier";
    }

    TalentInfo(@NonNull final String name) {
        title = name;
        description = name;
        icon = "barrier";
    }
}
