package com.cavetale.skills;

import com.cavetale.skills.util.Rnd;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class Talents {
    final SkillsPlugin plugin;

    public boolean has(Player player, Talent talent) {
        return plugin.sessions.of(player).hasTalent(talent);
    }

    public boolean unlock(@NonNull Player player, @NonNull Talent talent) {
        Session session = plugin.sessions.of(player);
        int cost = session.getTalentCost();
        if (session.getTalentPoints() < cost) return false;
        if (session.hasTalent(talent)) return false;
        if (!session.canAccessTalent(talent)) return false;
        session.playerRow.talentPoints -= cost;
        session.talents.add(talent);
        session.playerRow.talents = session.talents.size();
        session.playerRow.dirty = true;
        session.saveData();
        plugin.advancements.give(player, talent);
        return true;
    }

    public boolean rollPoint(@NonNull Player player, int increase) {
        final int total = 800;
        Session session = plugin.sessions.of(player);
        session.playerRow.talentChance += increase;
        session.playerRow.dirty = true;
        int chance;
        if (session.talents.isEmpty() && session.getTalentPoints() == 0) {
            chance = total / 2;
        } else {
            chance = session.playerRow.talentChance - 5;
            chance = Math.max(0, chance);
            chance = Math.min(chance, total / 2);
        }
        int roll = Rnd.random().nextInt(total);
        if (roll >= chance) return false;
        plugin.talents.addPoints(player, 1);
        return true;
    }

    public void addPoints(@NonNull Player player, final int amount) {
        if (amount == 0) return;
        Session session = plugin.sessions.of(player);
        int points = session.playerRow.talentPoints + amount;
        session.playerRow.talentPoints = points;
        session.playerRow.talentChance = 0;
        session.playerRow.dirty = true;
        session.saveData();
        if (amount < 1) return;
        boolean noEffect = plugin.advancements.give(player, null);
        int cost = session.getTalentCost();
        if (points >= cost) {
            if (!noEffect) Effects.talentUnlock(player);
            player.sendTitle(ChatColor.LIGHT_PURPLE + "Talent",
                             ChatColor.WHITE + "New Unlock Available");
        } else {
            if (!noEffect) Effects.talentPoint(player);
            player.sendTitle(ChatColor.LIGHT_PURPLE + "Talent Points",
                             ChatColor.WHITE + "Progress " + points + "/" + cost);
        }
    }
}
