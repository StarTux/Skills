package com.cavetale.skills.talent.archery;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.archery.ArrowType;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class InTheZoneTalent extends Talent implements Listener {
    public InTheZoneTalent() {
        super(TalentType.IN_THE_ZONE, "In the Zone",
              "Increase :bow:bow damage by landing an unbroken series of :target:hits",
              "Any :arrow:arrow hitting a hostile mob will increase :bow:bow damage. Lose all stacks if you die, or give or take melee damage.");
        addLevel(1, levelToPercentage(1) + "% damage increase");
        addLevel(1, levelToPercentage(2) + "% damage increase");
        addLevel(1, levelToPercentage(3) + "% damage increase");
        addLevel(1, levelToPercentage(4) + "% damage increase");
        addLevel(1, levelToPercentage(5) + "% damage increase");
    }

    private static int levelToPercentage(int level) {
        return level * 10;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.TARGET);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Player gets damaged by mob or projectile: reset
            switch (event.getCause()) {
            case ENTITY_ATTACK:
            case PROJECTILE:
                if (!isPlayerEnabled(player)) return;
                resetZone(player);
            default: return;
            }
        }
        if (event.getDamager() instanceof Player player) {
            // Player deals damage via melee: reset
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (!isPlayerEnabled(player)) return;
                resetZone(player);
            default: return;
            }
        }
    }

    public void onBowDamage(Player player, AbstractArrow arrow, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if (combatReward(mob) == null) return;
        if (!ArrowType.PRIMARY.is(arrow)) return;
        increaseZone(player);
    }

    public void onShootBow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        final Session session = Session.of(player);
        final int zone = session.archery.getInTheZone();
        if (zone < 1) return;
        final int level = session.getTalentLevel(talentType);
        if (level < 1) return;
        final int percentage = levelToPercentage(level);
        arrow.setDamage(arrow.getDamage() + (double) zone * (double) percentage * 0.01);
        if (isDebugTalent(player)) player.sendMessage(talentType + " +" + zone);
    }

    public void increaseZone(Player player) {
        final Session session = Session.of(player);
        int zone = session.archery.getInTheZone() + 1;
        session.archery.setInTheZone(zone);
        player.sendActionBar(join(separator(space()),
                                  text(zone, talentType.skillType.textColor, BOLD),
                                  text("In The Zone", talentType.skillType.textColor)));
    }

    public void resetZone(Player player) {
        final Session session = Session.of(player);
        if (session.archery.getInTheZone() == 0) return;
        session.archery.setInTheZone(0);
        if (isPlayerEnabled(player)) {
            player.sendActionBar(text("In The Zone", DARK_GRAY, STRIKETHROUGH));
        }
    }
}
