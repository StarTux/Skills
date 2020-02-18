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
    Map<Talent, TalentInfo> infos = new HashMap<>();
    TalentInfo baseInfo;

    /**
     * Load ALL the talent infos.
     */
    void load() {
        infos = new HashMap<>();
        ConfigurationSection conf = plugin.yaml.load("talents.yml");
        for (String key : conf.getKeys(false)) {
            Talent talent = Talent.of(key.toLowerCase());
            if (talent == null) {
                plugin.getLogger().warning("talents.yml: Invalid key:" + key);
                continue;
            }
            if (!conf.isConfigurationSection(talent.key)) {
                plugin.getLogger().warning("talents.yml: Not a section:" + key);
                continue;
            }
            TalentInfo info = new TalentInfo(conf.getConfigurationSection(talent.key));
            infos.put(talent, info);
        }
        for (Talent talent : Talent.values()) {
            if (infos.containsKey(talent)) continue;
            plugin.getLogger().warning("talents.yml: Missing section: " + talent.key);
            infos.put(talent, new TalentInfo(talent));
        }
        final String talents = "talents";
        if (conf.isConfigurationSection(talents)) {
            baseInfo = new TalentInfo(conf.getConfigurationSection(talents));
        } else {
            plugin.getLogger().warning("talents.yml: Missing section: talents");
            baseInfo = new TalentInfo("Talents");
        }
    }

    // Never returns null
    TalentInfo getInfo(Talent talent) {
        return infos.get(talent);
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
