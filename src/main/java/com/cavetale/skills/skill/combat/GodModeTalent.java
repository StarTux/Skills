package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class GodModeTalent extends Talent implements Listener {
    protected final CombatSkill combatSkill;

    protected GodModeTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.COMBAT_GOD_MODE);
        this.combatSkill = combatSkill;
    }

    @Override
    protected void enable() { }

    protected void onMeleeKill(Player player, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        Session session = plugin.sessions.of(player);
        if (session.getImmortal() <= 0) {
            player.sendActionBar(Component.text("God Mode!", NamedTextColor.GOLD));
        }
        session.setImmortal(3 * 20);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        Session session = plugin.sessions.of(player);
        if (session.getImmortal() <= 0) return;
        final double health = player.getHealth();
        if (health - event.getFinalDamage() >= 1.0) return;
        event.setDamage(Math.max(0.0, health - 1.0));
        Effects.godMode(player);
        player.sendActionBar(Component.text("God Mode Save!", NamedTextColor.GOLD));
    }
}
