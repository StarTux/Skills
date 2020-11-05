package com.cavetale.skills;

import org.junit.Test;

public final class SkillsTest {
    @Test
    public void test() {
        int old = -1;
        for (int i = 0; i < 200; i += 1) {
            int exp = SkillsPlugin.expBonusForLevel(i);
            if (exp == old) continue;
            old = exp;
            System.out.println("" + i + ": " + exp);
        }
    }
}
