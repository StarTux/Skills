package com.cavetale.skills.info;

import java.util.List;
import lombok.Value;
import net.kyori.adventure.text.Component;

@Value
public final class Info {
    public final String title;
    public final String category;
    public final List<Component> pages;
}
