package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class ArcherZoneTalent extends Talent {
    protected ArcherZoneTalent() {
        super(TalentType.ARCHER_ZONE);
    }

    @Override
    public String getDisplayName() {
        return "In The Zone";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Ranged kills give 5 seconds of double damage to ranged attacks");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.SPECTRAL_ARROW);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile == null) return;
        Session session = sessionOf(player);
        if (session.getArcherZone() <= 0) return;
        event.setDamage(event.getFinalDamage() * 2.0);
    }

    protected void onArcherKill(Player player, Mob mob, Projectile projectile, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile == null) return;
        Session session = sessionOf(player);
        session.setArcherZone(5 * 20);
        session.setArcherZoneKills(session.getArcherZoneKills() + 1);
        player.sendActionBar(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                    Component.text("In The Zone! ", NamedTextColor.RED),
                    Component.text(session.getArcherZoneKills(), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
                }));
        Effects.archerZone(player);
    }
}
