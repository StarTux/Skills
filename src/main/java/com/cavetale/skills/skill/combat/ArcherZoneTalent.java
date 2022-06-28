package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class ArcherZoneTalent extends Talent {
    protected final CombatSkill combatSkill;

    protected ArcherZoneTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.ARCHER_ZONE);
        this.combatSkill = combatSkill;
    }

    @Override
    protected void enable() { }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile == null) return;
        Session session = plugin.sessions.of(player);
        if (session.getArcherZone() <= 0) return;
        event.setDamage(event.getFinalDamage() * 2.0);
    }

    protected void onArcherKill(Player player, Mob mob, Projectile projectile, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile == null) return;
        Session session = plugin.sessions.of(player);
        session.setArcherZone(5 * 20);
        session.setArcherZoneKills(session.getArcherZoneKills() + 1);
        player.sendActionBar(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                    Component.text("In The Zone! ", NamedTextColor.RED),
                    Component.text(session.getArcherZoneKills(), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
                }));
        Effects.archerZone(player);
    }
}
