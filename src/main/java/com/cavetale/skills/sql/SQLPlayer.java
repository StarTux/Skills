package com.cavetale.skills.sql;

import com.winthier.sql.SQLRow;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import java.util.UUID;
import lombok.Data;
import lombok.NonNull;

@Data @NotNull @Name("players")
public final class SQLPlayer implements SQLRow {
    @Id private Integer id;
    @Unique private UUID uuid;
    private int levels = 0;
    private int talents = 0;

    public SQLPlayer() { }

    public SQLPlayer(@NonNull final UUID uuid) {
        this.uuid = uuid;
    }
}
