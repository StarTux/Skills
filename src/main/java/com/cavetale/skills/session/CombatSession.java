package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CombatSession extends SkillSession {
    /** Remember for DenialTalent. */
    protected boolean poisonFreebie = false;
    /** Cooldown in Epoch Millis. */
    protected long godModeDuration = 0;
    /**  Impaler's target and current impale stacks. */
    protected int impalerTargetId = 0;
    protected int impalerStack = 0;
    protected double rage = 0;

    protected CombatSession(final Session session) {
        super(session, SkillType.COMBAT);
    }

    public double increaseRage(double value) {
        rage += value;
        return rage;
    }

    public void resetRage() {
        rage = 0.0;
    }
}
