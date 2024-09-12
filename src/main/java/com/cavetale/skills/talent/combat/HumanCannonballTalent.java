package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.util.Text.formatDouble;

public final class HumanCannonballTalent extends Talent {
    public HumanCannonballTalent() {
        super(TalentType.HUMAN_CANNONBALL, "Human Cannonball",
              "When flying with :elytra:Elytra, your attack damage is multiplied with your flying speed.");
        addLevel(1, "Human Cannonball");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.ELYTRA);
    }

    public void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!player.isGliding()) return;
        if (!isPlayerEnabled(player)) return;
        if (!MeleeWeapon.hasMeleeWeapon(player)) return;
        final double speed = player.getVelocity().length();
        if (speed < 0.01) return;
        event.getCalculation().getOrCreateBaseDamageModifier().addFactorBonus(speed, "skills:human_cannonball");
        event.setHandled(true);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType.name() + " speed:" + formatDouble(speed));
        }
    }
}
