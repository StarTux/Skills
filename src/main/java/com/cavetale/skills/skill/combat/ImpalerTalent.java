package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class ImpalerTalent extends Talent {
    protected ImpalerTalent() {
        super(TalentType.IMPALER);
    }

    @Override
    public String getDisplayName() {
        return "Impaler";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Consecutive fully charged hits with an Impaling weapon"
                       + " against the same foe deal increasing damage",
                       "+1 damage for each consecutive hit, up to +6");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.TRIDENT);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (item == null || item.getType() == Material.ENCHANTED_BOOK || item.getEnchantmentLevel(Enchantment.IMPALING) <= 0
                        || player.getAttackCooldown() != 1.0) return;
        Session session = sessionOf(player);
        if (mob.getEntityId() != session.combat.getImpalerTargetId()) {
            session.combat.setImpalerTargetId(mob.getEntityId());
            session.combat.setImpalerStack(0);
        } else {
            event.setDamage(event.getDamage() + session.combat.getImpalerStack());
            if (session.combat.getImpalerStack() < 6) session.combat.setImpalerStack(session.combat.getImpalerStack() + 1);
        }
    }
}
