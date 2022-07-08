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
                if (a.slot.x() == 5 && a.slot.y() == 3) {
                    throw new IllegalStateException("Centered x,y: " + a);
                }
                if (a.slot.x() < 0 || a.slot.x() > 8) {
                    throw new IllegalStateException("x out of bounds: " + a);
                }
                if (a.slot.y() < 0 || a.slot.y() > 5) {
                    throw new IllegalStateException("y out of bounds: " + a);
                }
                for (TalentType b : talents) {
                    if (a == b) continue;
                    if (a.slot.x() == b.slot.x() && a.slot.y() == b.slot.y()) {
                        throw new IllegalStateException("Duplicate x,y: " + a + "/" + b
                                                        + ": " + a.slot.x() + "," + a.slot.y());
                    }
                }
            }
            System.out.println(skillType + ": " + points + " talent points, " + talents.size() + " talents");
        }
    }
}
