package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class ArcherySession extends SkillSession {
    @Setter protected int archerZone = 0;
    @Setter protected boolean bonusArrowFiring;

    protected ArcherySession(final Session session) {
        super(session, SkillType.ARCHERY);
    }
}
