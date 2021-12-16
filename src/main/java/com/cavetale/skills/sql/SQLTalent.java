package com.cavetale.skills.sql;

import com.cavetale.skills.skill.TalentType;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 * Each row represents one unlocked talent for one player.
 */
@Data
@Table(name = "talents",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player", "talent"}))
public final class SQLTalent {
    @Id
    private Integer id;
    @Column(nullable = false)
    private UUID player;
    @Column(nullable = false, length = 32)
    private String talent;
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false)
    private Date created;

    public SQLTalent() { }

    public SQLTalent(final UUID player, final TalentType talentType) {
        this.player = player;
        this.talent = talentType.key;
        this.enabled = true;
        this.created = new Date();
    }

    public TalentType getTalentType() {
        return TalentType.of(talent);
    }
}
