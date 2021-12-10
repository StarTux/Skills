package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import java.time.Duration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class VamonosTalent extends Talent implements Listener {
    protected final CombatSkill combatSkill;
    protected final Duration effectDuration = Duration.ofSeconds(30);

    protected VamonosTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.COMBAT_SPIDERS);
        this.combatSkill = combatSkill;
    }

    @Override
    protected void enable() { }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile,
                                     EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile != null) return;
        if (item == null || item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS) == 0) return;
        if (mob.getCategory() != EntityCategory.ARTHROPOD) return;
        MobStatusEffect.NO_POISON.set(mob, effectDuration);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                                             (int) effectDuration.toSeconds() * 20, 3 - 1,
                                             true, false));
        Effects.applyStatusEffect(mob);
    }

    /**
     * This event is called before the poison is applied.  We remember
     * this by setting the PoisonFreebie, so that the effect below,
     * which does not know the causing entity, can pick up on it.
     */
    protected void onMobDamagePlayer(Player player, Mob mob, Projectile projectile, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile != null) return;
        if (mob.getCategory() != EntityCategory.ARTHROPOD) return;
        if (!MobStatusEffect.NO_POISON.has(mob)) return;
        Session session = plugin.sessions.of(player);
        session.setPoisonFreebie(true);
    }

    /**
     * Disable the event if PoisonFreebie is set, see above.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        if (event.getCause() != EntityPotionEffectEvent.Cause.ATTACK) return;
        switch (event.getAction()) {
        case ADDED: case CHANGED: break;
        default: return;
        }
        Session session = plugin.sessions.of(player);
        if (!session.isPoisonFreebie()) return;
        session.setPoisonFreebie(false);
        event.setCancelled(true);
    }
}
