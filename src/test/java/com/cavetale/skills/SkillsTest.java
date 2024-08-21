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
            for (TalentType a : talents) {
                if (a.slot.getX() == 5 && a.slot.getZ() == 3) {
                    throw new IllegalStateException("Centered x,z: " + a);
                }
                if (a.slot.getX() < 0 || a.slot.getX() > 8) {
                    throw new IllegalStateException("x out of bounds: " + a);
                }
                if (a.slot.getZ() < 0 || a.slot.getZ() > 5) {
                    throw new IllegalStateException("z out of bounds: " + a);
                }
                for (TalentType b : talents) {
                    if (a == b) continue;
                    if (a.slot.getX() == b.slot.getX() && a.slot.getZ() == b.slot.getZ()) {
                        throw new IllegalStateException("Duplicate x,z: " + a + "/" + b
                                                        + ": " + a.slot.getX() + "," + a.slot.getZ());
                    }
                }
            }
            System.out.println(skillType + ": " + talents.size() + " talents");
        }
    }
}
