package com.cavetale.skills;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter @Table(name = "players")
public final class SQLPlayer {
    @Id
    Integer id;
    @Column(nullable = false, unique = true)
    UUID uuid;
    @Column(nullable = false)
    int talentPoints = 0;
    @Column(nullable = false)
    int talentChance = 0;
    @Column(nullable = false)
    int levels = 0;
    @Column(nullable = false)
    int talents = 0;
    @Column(nullable = true, length = 4096)
    String json;
    transient Tag tag = new Tag();
    transient boolean dirty;

    public SQLPlayer() { }

    static final class Tag {
        Set<String> talents = new HashSet<>();
        transient boolean modified;
    }

    SQLPlayer(@NonNull final UUID uuid) {
        this.uuid = uuid;
    }

    void pack() {
        json = Json.serialize(tag);
    }

    void unpack() {
        tag = json != null
            ? Json.deserialize(json, Tag.class, Tag::new)
            : new Tag();
    }
}
