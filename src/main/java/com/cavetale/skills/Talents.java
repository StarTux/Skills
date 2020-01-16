package com.cavetale.skills;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
final class Talents {
    final SkillsPlugin plugin;
    Map<String, TalentInfo> talentInfos;

    // Never returns null
    TalentInfo getInfo(String name) {
        if (talentInfos == null) {
            talentInfos = new HashMap<>();
            ConfigurationSection conf = plugin.yaml.load("talents.yml");
            for (String key : conf.getKeys(false)) {
                talentInfos.put(key, new TalentInfo(conf.getConfigurationSection(key)));
            }
        }
        TalentInfo result = talentInfos.get(name);
        if (result == null) {
            plugin.getLogger().warning("Missing talent info: " + name);
            result = new TalentInfo(name);
            talentInfos.put(name, result);
        }
        return result;
    }

    boolean unlock(@NonNull Player player, @NonNull Talent talent) {
        Session session = plugin.sessions.of(player);
        int cost = session.getTalentCost();
        if (session.getTalentPoints() < cost) return false;
        if (session.hasTalent(talent)) return false;
        if (!session.canAccessTalent(talent)) return false;
        session.playerRow.talentPoints -= cost;
        session.talents.add(talent);
        session.playerRow.talents = session.talents.size();
        session.playerRow.modified = true;
        session.tag.modified = true;
        session.saveData();
        plugin.advancements.give(player, talent);
        return true;
    }

    boolean rollPoint(@NonNull Player player, int increase) {
        final int total = 800;
        Session session = plugin.sessions.of(player);
        session.playerRow.talentChance += increase;
        session.playerRow.modified = true;
        int chance;
        if (session.talents.isEmpty() && session.getTalentPoints() == 0) {
            chance = total / 2;
        } else {
            chance = session.playerRow.talentChance - 5;
            chance = Math.max(0, chance);
            chance = Math.min(chance, total / 2);
        }
        int roll = plugin.random.nextInt(total);
        if (roll >= chance) return false;
        plugin.talents.addPoints(player, 1);
        return true;
    }

    void addPoints(@NonNull Player player, final int amount) {
        if (amount == 0) return;
        Session session = plugin.sessions.of(player);
        int points = session.playerRow.talentPoints + amount;
        session.playerRow.talentPoints = points;
        session.playerRow.talentChance = 0;
        session.playerRow.modified = true;
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
