package com.cavetale.skills.sql;

import com.cavetale.skills.skill.SkillType;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import com.winthier.sql.SQLRow.UniqueKey;
import com.winthier.sql.SQLRow;
import java.util.UUID;
import lombok.Data;
import lombok.NonNull;

@Data @NotNull @Name("skills")
@UniqueKey({"player", "skill"})
public final class SQLSkill implements SQLRow {
    @Id private Integer id;
    private UUID player;
    @VarChar(40) private String skill;
    @Default("0") private int skillPoints;
    @Default("0") private int requiredSkillPoints;
    @Default("0") private int totalSkillPoints;
    @Default("0") private int level;
    @Default("0") private int talents;
    @Default("0") private int talentPoints;
    @Default("0") private int totalTalentPoints;
    @Default("0") private int moneyBonus;
    @Default("0") private int expBonus;

    public SQLSkill() { }

    public SQLSkill(@NonNull final UUID player, @NonNull final SkillType skillType) {
        this.player = player;
        this.skill = skillType.key;
    }

    public SkillType getSkillType() {
        return SkillType.ofKey(skill);
    }
}
