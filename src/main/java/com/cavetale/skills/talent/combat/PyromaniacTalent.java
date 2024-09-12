package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PyromaniacTalent extends Talent {
    public PyromaniacTalent() {
        super(TalentType.PYROMANIAC, "Pyromaniac",
              "Monsters set on fire take more damage.");
        addLevel(1, "+" + levelToPercentage(1) + "% damage to monsters set on fire");
        addLevel(1, "+" + levelToPercentage(2) + "% damage to monsters set on fire");
        addLevel(1, "+" + levelToPercentage(3) + "% damage to monsters set on fire");
        addLevel(1, "+" + levelToPercentage(4) + "% damage to monsters set on fire");
        addLevel(1, "+" + levelToPercentage(5) + "% damage to monsters set on fire");
    }

    private static int levelToPercentage(int level) {
        return level * 10;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.CAMPFIRE);
    }

    public void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (event.getTarget().getFireTicks() <= 0) return;
        if (!MeleeWeapon.hasMeleeWeapon(player)) return;
        final int level = Session.of(player).getTalentLevel(talentType);
        if (level < 1) return;
        final int percentage = levelToPercentage(level);
        if (percentage < 1) return;
        event.getCalculation().getOrCreateBaseDamageModifier().addFactorBonus(percentage * 0.01, "skills:pyromaniac");
        event.setHandled(true);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " lvl:" + level + " percentage:" + percentage);
        }
    }
}
