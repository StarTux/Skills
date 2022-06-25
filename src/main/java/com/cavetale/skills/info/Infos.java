package com.cavetale.skills.info;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public final class Infos {
    protected final SkillsPlugin plugin;
    protected final Map<String, Info> infoMap = new HashMap<>();

    public void enable() {
        for (SkillType skillType : SkillType.values()) {
            List<Component> pages = new ArrayList<>();
            pages.add(Component.text(skillType.tag.description()));
            for (String text : skillType.tag.moreText()) {
                pages.add(Component.text(text));
            }
            Info info = new Info(skillType.displayName, "Skill", pages);
            infoMap.put(skillType.name().toLowerCase(), info);
        }
        for (TalentType talentType : TalentType.values()) {
            List<Component> pages = new ArrayList<>();
            pages.add(Component.text(talentType.getTalent().getDescription()));
            pages.addAll(talentType.getTalent().getInfoPages());
            Info info = new Info(talentType.tag.title(), "Talent", pages);
            infoMap.put(info.title.toLowerCase().replace(" ", "_"), info);
        }
    }

    public Info get(String key) {
        return infoMap.get(key);
    }

    public List<String> keys() {
        return new ArrayList<>(infoMap.keySet());
    }
}
