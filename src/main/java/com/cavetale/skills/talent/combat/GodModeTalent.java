package com.cavetale.skills.talent.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;

public final class GodModeTalent extends Talent {
    public GodModeTalent() {
        super(TalentType.GOD_MODE, "God Mode",
              "Melee kills give temporary immortality.",
              "Immortality resurrects you once in case you die.");
        addLevel(1, levelToSeconds(1) + " seconds of immortality");
        addLevel(1, levelToSeconds(2) + " seconds of immortality");
        addLevel(1, levelToSeconds(3) + " seconds of immortality");
        addLevel(1, levelToSeconds(4) + " seconds of immortality");
    }

    private static int levelToSeconds(int level) {
        return level * 3;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.TOTEM_OF_UNDYING);
    }

    /**
     * Kills grant god mode.
     */
    public void onMeleeKill(Player player, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        final Session session = Session.of(player);
        final int level = session.getTalentLevel(talentType);
        if (level < 1) return;
        final int seconds = levelToSeconds(level);
        session.combat.setGodModeDuration(System.currentTimeMillis() + (long) seconds * 1000L);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " lvl:" + level + " secs:" + seconds);
        }
    }

    /**
     * Resurrect without a totem.
     */
    public void onEntityResurrect(Player player, EntityResurrectEvent event) {
        if (event.getHand() != null) return;
        if (!isPlayerEnabled(player)) return;
        final Session session = Session.of(player);
        final long duration = session.combat.getGodModeDuration();
        if (duration < System.currentTimeMillis()) return;
        session.combat.setGodModeDuration(0L);
        event.setCancelled(false);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " resurrect");
        }
    }
}
