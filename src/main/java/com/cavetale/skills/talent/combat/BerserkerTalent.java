package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import lombok.Getter;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

@Getter
public final class BerserkerTalent extends Talent {
    public BerserkerTalent() {
        super(TalentType.BERSERKER, "Berserker",
              "Build up rage by hitting enemies with a full charge. Rage increases damage output.",
              "Rage is increased every time you hit a mob with a melee weapon. It is lost whenever you lay down said weapon or when you die.");
        addLevel(1, "Up to " + levelToBonusPercentage(1), "% bonus damage");
        addLevel(1, "Up to " + levelToBonusPercentage(2), "% bonus damage");
        addLevel(1, "Up to " + levelToBonusPercentage(3), "% bonus damage");
        addLevel(1, "Up to " + levelToBonusPercentage(4), "% bonus damage");
        addLevel(1, "Up to " + levelToBonusPercentage(5), "% bonus damage");
    }

    private static int levelToBonusPercentage(int level) {
        return level * 20;
    }

    private static int rageToPercentage(double rage) {
        final double value = rage * 0.1;
        return Math.max(0, Math.min(100, (int) value));
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.IRON_BROAD_AXE);
    }

    public void onPlayerDamageMob(Player player, Mob mob, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (player.getAttackCooldown() < 1f) return;
        final Session session = Session.of(player);
        final double rage = session.combat.increaseRage(event.getDamage());
        if (rage < 0.01) return;
        final int level = session.getTalentLevel(talentType);
        if (level < 1) return;
        final int ragePercentage = rageToPercentage(rage);
        final int bonusPercentage = levelToBonusPercentage(level);
        player.sendActionBar(textOfChildren(Mytems.LIGHTNING,
                                            text("Rage ", GRAY),
                                            text(Math.min(ragePercentage, bonusPercentage) + "%", YELLOW)));
    }

    public void resetRage(Player player) {
        final Session session = Session.of(player);
        final double rage = session.combat.getRage();
        if (rage < 0.01) return;
        session.combat.resetRage();
        player.sendActionBar(textOfChildren(Mytems.LIGHTNING.asComponent().color(DARK_GRAY),
                                            text("Rage 0%", DARK_GRAY))
                             .decorate(STRIKETHROUGH));
    }

    public void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        final Session session = Session.of(player);
        final double rage = session.combat.getRage();
        final int ragePercentage = rageToPercentage(rage);
        if (ragePercentage < 1) return;
        final int level = session.getTalentLevel(talentType);
        if (level < 1) return;
        final int bonusPercentage = levelToBonusPercentage(level);
        final int percentage = Math.min(ragePercentage, bonusPercentage);
        event.getCalculation().getOrCreateBaseDamageModifier().addFactorBonus(percentage * 0.01, "skills:berserker");
        event.setHandled(true);
    }
}
