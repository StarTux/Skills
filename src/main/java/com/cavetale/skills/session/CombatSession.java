package com.cavetale.skills.session;

import com.cavetale.skills.skill.SkillType;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import static com.cavetale.skills.SkillsPlugin.combatSkill;

@Getter
@Setter
public final class CombatSession extends SkillSession {
    /** Remember for DenialTalent. */
    private boolean poisonFreebie = false;
    /** Cooldown in Epoch Millis. */
    private long godModeDuration = 0;
    /**  Impaler's target and current impale stacks. */
    private int impalerTargetId = 0;
    private int impalerStack = 0;
    private double rage = 0;
    private Instant lastRage;

    protected CombatSession(final Session session) {
        super(session, SkillType.COMBAT);
    }

    @Override
    public void tick(final Player player) {
        if (rage > 0.0) {
            if (lastRage == null || Duration.between(lastRage, Instant.now()).toSeconds() >= 10) {
                lastRage = null;
                rage = Math.max(0.0, rage - 0.5);
                if (rage == 0.0) {
                    combatSkill().getBerserkerTalent().resetRage(player);
                } else {
                    combatSkill().getBerserkerTalent().sendRageUpdate(player, session, rage);
                }
            }
        }
    }

    public double increaseRage(double value) {
        lastRage = Instant.now();
        rage += value;
        return rage;
    }

    public void resetRage() {
        lastRage = null;
        rage = 0.0;
    }
}
