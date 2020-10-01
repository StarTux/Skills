package com.cavetale.skills;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

public final class Info {
    public final String title;
    public final String description;

    public Info(@NonNull final ConfigurationSection config) {
        title = config.getString("title");
        description = config.getString("description");
    }
}
