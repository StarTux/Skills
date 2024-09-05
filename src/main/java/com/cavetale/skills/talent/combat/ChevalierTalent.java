package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.util.Text.formatDouble;

public final class ChevalierTalent extends Talent {
    public ChevalierTalent() {
        super(TalentType.CHEVALIER, "Chevalier",
              "Bonus damage while mounted on a :saddle:horse");
        addLevel(1, "Add :iron_horse_armor:horse armor");
        addLevel(1, "Add horse :jump_boost_effect:jump strength");
        addLevel(1, "Add horse :speed_effect:movement speed ability");
        addLevel(1, "25% more damage from all of the above");
        addLevel(1, "50% more damage from all of the above");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.IRON_KNIGHT_HELMET);
    }


    public void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!(player.getVehicle() instanceof AbstractHorse horse)) return;
        final int level = Session.of(player).getTalentLevel(talentType);
        if (level < 1) return;
        event.setHandled(true);
        double factor = 1.0;
        if (level >= 5) {
            factor = 1.25;
        } else if (level >= 4) {
            factor = 1.5;
        }
        double horseArmor = 0.5 * horse.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        event.getCalculation().getOrCreateBaseDamageModifier().addFlatBonus(factor * horseArmor, "skills:chevalier_armor");
        double horseJumpStrength = 0;
        if (level >= 2) {
            horseJumpStrength = 5 * horse.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).getValue();
            event.getCalculation().getOrCreateBaseDamageModifier().addFlatBonus(factor * horseJumpStrength, "skills:chevalier_jump");
        }
        double horseMovementSpeed = 0;
        if (level >= 3) {
            horseMovementSpeed = 10 * horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
            event.getCalculation().getOrCreateBaseDamageModifier().addFlatBonus(factor * horseMovementSpeed, "skills:chevalier_speed");
        }
        if (isDebugTalent(player)) {
            player.sendMessage(talentType.name()
                               + " jump:" + formatDouble(horseJumpStrength)
                               + " speed:" + formatDouble(horseMovementSpeed)
                               + " factor:" + formatDouble(factor));
        }
    }
}
