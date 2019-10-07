package com.cavetale.skills;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

final class Info {
    String title;
    String description;

    Info(@NonNull final ConfigurationSection config) {
        title = config.getString("title");
        description = config.getString("description");
    }
}
