package com.cavetale.skills.session;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * The serializable save tag of a session.
 */
@Data
public final class Tag {
    protected Set<String> talents = new HashSet<>();
    protected transient boolean modified;
}
