package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.mining.SuperVisionTalent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class MiningSession extends SkillSession {
    private SuperVisionTalent.Tag superVisionTag;

    protected MiningSession(final Session session) {
        super(session, SkillType.MINING);
    }
}
