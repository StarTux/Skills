package com.cavetale.skills.sql;

import com.winthier.sql.SQLDatabase;
import org.junit.Test;

public final class SQLTest {
    @Test
    public void testTalentTypes() {
        System.out.println(SQLDatabase.testTableCreation(SQLPlayer.class));
        System.out.println(SQLDatabase.testTableCreation(SQLSkill.class));
        System.out.println(SQLDatabase.testTableCreation(SQLTalent.class));
    }
}
