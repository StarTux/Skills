package com.cavetale.skills;

final class StatusEffect {
    long silence;
    long noPoison;

    boolean hasSilence() {
        if (silence == 0L) return false;
        if (silence < Util.now()) {
            silence = 0L;
            return false;
        }
        return true;
    }

    boolean hasNoPoison() {
        if (noPoison == 0L) return false;
        if (noPoison < Util.now()) {
            noPoison = 0L;
            return false;
        }
        return true;
    }
}
