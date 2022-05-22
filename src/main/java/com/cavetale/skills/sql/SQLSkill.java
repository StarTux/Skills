package com.cavetale.skills.sql;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.SkillType;
import com.winthier.sql.SQLRow;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter
@Table(name = "skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player", "skill"}))
public final class SQLSkill implements SQLRow {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false, length = 16)
    private String skill;
    @Column(nullable = false)
    private int points = 0;
    @Column(nullable = false)
    private int level = 0;
    private transient boolean modified;

    public SQLSkill() { }

    public SQLSkill(@NonNull final UUID player, @NonNull final String skill) {
        this.player = player;
        this.skill = skill;
    }

    public SkillType getSkillType() {
        return SkillType.ofKey(skill);
    }

    public int getTotalPoints() {
        int result = points;
        for (int i = 1; i < level; i += 1) {
            result += SkillsPlugin.pointsForLevelUp(i);
        }
        return result;
    }
}
