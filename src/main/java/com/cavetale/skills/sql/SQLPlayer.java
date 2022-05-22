package com.cavetale.skills.sql;

import com.winthier.sql.SQLRow;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter @Table(name = "players")
public final class SQLPlayer implements SQLRow {
    @Id
    private Integer id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(nullable = false)
    private int talentPoints = 0;
    @Column(nullable = false)
    private int talentChance = 0;
    @Column(nullable = false)
    private int levels = 0;
    @Column(nullable = false)
    private int talents = 0;
    @Column(nullable = true, length = 4096)
    private String json;
    private transient boolean modified;

    public SQLPlayer() { }

    public SQLPlayer(@NonNull final UUID uuid) {
        this.uuid = uuid;
    }
}
