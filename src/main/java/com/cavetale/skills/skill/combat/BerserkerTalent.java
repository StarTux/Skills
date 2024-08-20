package com.cavetale.skills.skill.combat;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
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
    protected BerserkerTalent() {
        super(TalentType.BERSERKER);
    }

    @Override
    public String getDisplayName() {
        return "Berserker";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Build up rage by hitting enemies with a full charge. Full rage can double your damage output");
    }

    @Override
    public ItemStack createIcon() {
        return Mytems.LIGHTNING.createIcon();
    }

    private int rageToPercentage(double rage) {
        final double value = rage * 0.1;
        return Math.max(0, Math.min(100, (int) value));
    }

    protected void onPlayerDamageMob(Player player, Mob mob, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (player.getAttackCooldown() < 1f) return;
        final Session session = Session.of(player);
        final double rage = session.combat.increaseRage(event.getDamage());
        if (rage < 0.01) return;
        player.sendActionBar(textOfChildren(Mytems.LIGHTNING,
                                            text("Rage ", GRAY),
                                            text(rageToPercentage(rage) + "%", YELLOW)));
    }

    protected void onPlayerItemHeld(Player player) {
        if (!isPlayerEnabled(player)) return;
        final Session session = Session.of(player);
        final double rage = session.combat.getRage();
        if (rage < 0.01) return;
        session.combat.resetRage();
        player.sendActionBar(textOfChildren(Mytems.LIGHTNING.asComponent().color(DARK_GRAY),
                                            text("Rage 0%", DARK_GRAY))
                             .decorate(STRIKETHROUGH));
    }

    protected void onPlayerDeath(Player player) {
        if (!isPlayerEnabled(player)) return;
        final Session session = Session.of(player);
        session.combat.resetRage();
    }

    protected void onDamageCalculation(Player player, DamageCalculationEvent event) {
        if (!isPlayerEnabled(player)) return;
        final Session session = Session.of(player);
        final double rage = session.combat.getRage();
        final int ragePercentage = rageToPercentage(rage);
        if (ragePercentage < 1) return;
        event.getCalculation().getOrCreateBaseDamageModifier().addFactorBonus(ragePercentage * 0.01, "skills:berserker");
        event.setHandled(true);
    }
}
