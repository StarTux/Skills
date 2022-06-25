package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import java.time.Duration;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class GodModeTalent extends Talent implements Listener {
    protected final CombatSkill combatSkill;
    protected final Duration duration = Duration.ofSeconds(3);

    protected GodModeTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.GOD_MODE);
        this.combatSkill = combatSkill;
        this.description = "Melee kills give " + duration.toSeconds() + " seconds of immortality";
        this.infoPages = List.of(Component.text("Immortality stops you from dying, but"
                                                + " you will still take damage!"));
    }

    @Override
    protected void enable() { }

    /**
     * Kills grant god mode.
     */
    protected void onMeleeKill(Player player, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        Session session = plugin.sessions.of(player);
        if (session.combat.getGodModeDuration() == 0) {
            player.sendActionBar(Component.text("God Mode!", NamedTextColor.GOLD, TextDecoration.BOLD));
        }
        session.combat.setGodModeDuration(System.currentTimeMillis() + duration.toMillis());
    }

    /**
     * Intercept killing blows and reduce their damage to 1 minus
     * health.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        Session session = plugin.sessions.of(player);
        long godModeDuration = session.combat.getGodModeDuration();
        if (godModeDuration == 0L) return;
        if (godModeDuration < System.currentTimeMillis()) {
            session.combat.setGodModeDuration(0L);
            return;
        }
        final double health = player.getHealth();
        if (health - event.getFinalDamage() >= 1.0) return;
        event.setDamage(Math.max(0.0, health - 1.0));
        Effects.godMode(player);
        player.sendActionBar(Component.text("God Mode Save!", NamedTextColor.GOLD));
    }
}
