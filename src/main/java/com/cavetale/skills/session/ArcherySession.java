package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ArcherySession extends SkillSession {
    protected int inTheZone = 0;
    protected boolean legolasFiring;
    protected int legolasCount;

    protected ArcherySession(final Session session) {
        super(session, SkillType.ARCHERY);
    }
}
