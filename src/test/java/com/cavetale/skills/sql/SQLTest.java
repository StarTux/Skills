package com.cavetale.skills.sql;

import com.winthier.sql.SQLDatabase;

public final class SQLTest {
    public void testTalentTypes() {
        System.out.println(SQLDatabase.testTableCreation(SQLPlayer.class));
        System.out.println(SQLDatabase.testTableCreation(SQLSkill.class));
        System.out.println(SQLDatabase.testTableCreation(SQLTalent.class));
    }
}
