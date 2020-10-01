package com.cavetale.skills;

import com.cavetale.skills.util.Util;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class StatusEffect {
    private long silence;
    private long noPoison;

    public boolean hasSilence() {
        if (silence == 0L) return false;
        if (silence < Util.now()) {
            silence = 0L;
            return false;
        }
        return true;
    }

    public boolean hasNoPoison() {
        if (noPoison == 0L) return false;
        if (noPoison < Util.now()) {
            noPoison = 0L;
            return false;
        }
        return true;
    }
}
