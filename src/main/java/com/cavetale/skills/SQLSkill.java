package com.cavetale.skills;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Table(name = "skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player", "skill"}))
public final class SQLSkill {
    @Id
    Integer id;
    @Column(nullable = false)
    UUID player;
    @Column(nullable = false, length = 16)
    String skill;
    @Column(nullable = false)
    int points = 0;
    @Column(nullable = false)
    int level = 0;
    transient boolean modified;

    public SQLSkill() { }

    SQLSkill(@NonNull final UUID player, @NonNull final String skill) {
        this.player = player;
        this.skill = skill;
    }

    SkillType getSkillType() {
        return SkillType.ofKey(skill);
    }
}
