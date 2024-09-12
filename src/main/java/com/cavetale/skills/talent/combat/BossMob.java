package com.cavetale.skills.talent.combat;

import com.cavetale.worldmarker.entity.EntityMarker;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;

public final class BossMob {
    public static boolean isBossMob(Entity entity) {
        return entity instanceof Boss
            || EntityMarker.hasId(entity, "boss");
    }

    private BossMob() { }
}
