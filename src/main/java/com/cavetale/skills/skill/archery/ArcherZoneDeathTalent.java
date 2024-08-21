package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.archerySkill;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;

public final class ArcherZoneDeathTalent extends Talent {
    public ArcherZoneDeathTalent() {
        super(TalentType.ARCHER_ZONE_DEATH, "Dead Zone",
              "Arrow kills increase bow damage",
              "In the Zone damage increases even more every time you kill a mob which yields SP with an arrow.");
        addLevel(1, "One charge per kill");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.BELL);
    }

    protected void onBowKill(Player player, AbstractArrow arrow, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if (combatReward(mob) == null) return;
        archerySkill().archerZoneTalent.increaseZone(player);
    }
};
