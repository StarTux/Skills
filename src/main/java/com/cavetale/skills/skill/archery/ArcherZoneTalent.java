package com.cavetale.skills.skill.archery;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class ArcherZoneTalent extends Talent implements Listener {
    protected ArcherZoneTalent() {
        super(TalentType.ARCHER_ZONE, "In the Zone",
              "Increase :bow:bow damage by landing an unbroken series of :target:hits",
              "Any :arrow:arrow hitting a hostile mob will increase :bow:bow damage. Breaking your focus will reset the damage bonus. Break focus by switching items, taking damage, or missing a shot.");
        addLevel(1, levelToPercentage(1) + "% damage increase");
        addLevel(1, levelToPercentage(2) + "% damage increase");
        addLevel(1, levelToPercentage(3) + "% damage increase");
        addLevel(1, levelToPercentage(4) + "% damage increase");
        addLevel(1, levelToPercentage(5) + "% damage increase");
    }

    private static int levelToPercentage(int level) {
        return level * 5;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.TARGET);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        resetZone(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerEnabled(player)) return;
        resetZone(player);
    }

    protected void onBowDamage(Player player, AbstractArrow arrow, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if (combatReward(mob) == null) return;
        if (!ArrowType.PRIMARY.is(arrow)) return;
        increaseZone(player);
    }

    protected void onArrowHitBlock(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (!ArrowType.PRIMARY.is(arrow)) return;
        resetZone(player);
    }

    protected void onShootBow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        final Session session = Session.of(player);
        final int zone = session.archery.getArcherZone();
        if (zone < 1) return;
        final int level = session.getTalentLevel(talentType);
        if (level < 1) return;
        final int percentage = levelToPercentage(level);
        arrow.setDamage(arrow.getDamage() + (double) zone * (double) percentage * 0.01);
        if (isDebugTalent(player)) player.sendMessage(talentType + " +" + zone);
    }

    protected void increaseZone(Player player) {
        final Session session = Session.of(player);
        int zone = session.archery.getArcherZone() + 1;
        session.archery.setArcherZone(zone);
        player.sendActionBar(join(separator(space()),
                                  text(zone, talentType.skillType.textColor, BOLD),
                                  text("In The Zone", talentType.skillType.textColor)));
    }

    protected void resetZone(Player player) {
        final Session session = Session.of(player);
        if (session.archery.getArcherZone() == 0) return;
        session.archery.setArcherZone(0);
        if (isPlayerEnabled(player)) {
            player.sendActionBar(text("In The Zone", DARK_GRAY, STRIKETHROUGH));
        }
    }
}
