package com.cavetale.skills.sql;

import com.cavetale.skills.skill.SkillType;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NonNull;

@Data
@Table(name = "skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player", "skill"}))
public final class SQLSkill {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false, length = 16)
    private String skill;
    @Column(nullable = false)
    private int skillPoints = 0;
    @Column(nullable = false)
    private int requiredSkillPoints = 0;
    @Column(nullable = false)
    private int totalSkillPoints = 0;
    @Column(nullable = false)
    private int level = 0;
    @Column(nullable = false)
    private int talents = 0;
    @Column(nullable = false)
    private int talentPoints = 0;

    public SQLSkill() { }

    public SQLSkill(@NonNull final UUID player, @NonNull final SkillType skillType) {
        this.player = player;
        this.skill = skillType.key;
    }

    public SkillType getSkillType() {
        return SkillType.ofKey(skill);
    }
}
