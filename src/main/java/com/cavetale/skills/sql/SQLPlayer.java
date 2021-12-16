package com.cavetale.skills.sql;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NonNull;

@Data @Table(name = "players")
public final class SQLPlayer {
    @Id
    private Integer id;
    @Column(nullable = false, unique = true)
    private UUID uuid;
    @Column(nullable = false)
    private int levels = 0;
    @Column(nullable = false)
    private int talents = 0;

    public SQLPlayer() { }

    public SQLPlayer(@NonNull final UUID uuid) {
        this.uuid = uuid;
    }
}
