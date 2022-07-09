package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class CombatSession extends SkillSession {
    /** Remember for DenialTalent. */
    @Setter protected boolean poisonFreebie = false;
    /** Cooldown in Epoch Millis. */
    @Setter protected long godModeDuration = 0;
    /**  Impaler's target and current impale stacks. */
    @Setter protected int impalerTargetId = 0;
    @Setter protected int impalerStack = 0;

    protected CombatSession(final Session session) {
        super(session, SkillType.COMBAT);
    }
}
