package com.cavetale.skills;

import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import java.util.Collection;
import org.junit.Test;

public final class SkillsTest {
    @Test
    public void testTalentTypes() {
        for (SkillType skillType : SkillType.values()) {
            Collection<TalentType> talents = TalentType.SKILL_MAP.get(skillType);
            int points = 0;
            for (TalentType a : talents) {
                points += a.talentPointCost;
                if (a.tag.x() == 4 && a.tag.y() == 3) {
                    throw new IllegalStateException("Centered x,y: " + a + ": " + a.tag.x() + "," + a.tag.y());
                }
                for (TalentType b : talents) {
                    if (a == b) continue;
                    if (a.tag.x() == b.tag.x() && a.tag.y() == b.tag.y()) {
                        throw new IllegalStateException("Duplicate x,y: " + a + "/" + b
                                                        + ": " + a.tag.x() + "," + a.tag.y());
                    }
                }
            }
            System.out.println(skillType + ": " + points + " talent points, " + talents.size() + " talents");
        }
    }
}
