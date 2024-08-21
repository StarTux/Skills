package com.cavetale.skills.sql;

import com.cavetale.skills.skill.TalentType;
import com.winthier.sql.SQLRow;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

/**
 * Each row represents one unlocked talent for one player.
 */
@Data @NotNull @Name("talents")
@UniqueKey({"player", "talent"})
public final class SQLTalent implements SQLRow {
    @Id private Integer id;
    private UUID player;
    @VarChar(40) private String skill;
    @VarChar(40) private String talent;
    @Default("1") private int level;
    @Default("0") private boolean enabled;
    @Default("NOW()") private Date created;

    public SQLTalent() { }

    public SQLTalent(final UUID player, final TalentType talentType, final int level) {
        this.player = player;
        this.skill = talentType.skillType.key;
        this.talent = talentType.key;
        this.level = level;
        this.enabled = true;
        this.created = new Date();
    }

    public TalentType getTalentType() {
        return TalentType.of(talent);
    }
}
