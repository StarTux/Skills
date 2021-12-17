package com.cavetale.skills.info;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Infos {
    protected final SkillsPlugin plugin;
    protected final Map<String, Info> infoMap = new HashMap<>();

    public void enable() {
        for (SkillType skillType : SkillType.values()) {
            List<String> lines = new ArrayList<>();
            lines.add(skillType.tag.description());
            for (String line : skillType.tag.moreText()) {
                lines.add(line);
            }
            Info info = new Info(skillType.displayName,
                                 "Skill",
                                 String.join("\n\n", lines));
            infoMap.put(skillType.name().toLowerCase(), info);
        }
        for (TalentType talentType : TalentType.values()) {
            List<String> lines = new ArrayList<>();
            lines.add(talentType.tag.description());
            for (String line : talentType.tag.moreText()) {
                lines.add(line);
            }
            Info info = new Info(talentType.tag.title(),
                                 "Talent",
                                 String.join("\n\n", lines));
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
