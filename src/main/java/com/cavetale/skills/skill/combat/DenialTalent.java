package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.cavetale.worldmarker.entity.EntityMarker;
import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent;
import java.time.Duration;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

public final class DenialTalent extends Talent implements Listener {
    protected final CombatSkill combatSkill;
    protected final Duration duration = Duration.ofSeconds(20);

    protected DenialTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.DENIAL);
        this.combatSkill = combatSkill;
        this.description = "Knockback denies mob spells"
            + "for " + duration.toSeconds() + " seconds";
        this.infoPages = List.of(new Component[] {
                // Page 1
                Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("This effect denies the following for "),
                        Component.text(duration.toSeconds() + " seconds"),
                        Component.text(":"),
                        Component.text("\n- Shooting Arrows"),
                        Component.text("\n- Throwing Potions"),
                        Component.text("\n- Spider Poison"),
                    }),
                // Page 2
                Component.text("Use a Knockback weapon on an enemy to give it this status effect."),
            });
    }

    @Override
    protected void enable() { }

    /**
     * When a mob is damaged, apply the Denial effect.
     */
    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile != null) return;
        if (item == null || item.getEnchantmentLevel(Enchantment.KNOCKBACK) == 0) return;
        if (mob instanceof Boss) return;
        if (EntityMarker.hasId(mob, "boss")) return;
        MobStatusEffect.DENIAL.set(mob, duration);
        Effects.applyStatusEffect(mob);
    }

    /**
     * Deny shooting bow.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Mob shooter)) return;
        if (!MobStatusEffect.DENIAL.has(shooter)) return;
        event.setCancelled(true);
        Effects.denyLaunch(shooter);
    }

    /**
     * Deny throwing potions.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onWitchThrowPotion(WitchThrowPotionEvent event) {
        Mob witch = event.getEntity();
        if (!MobStatusEffect.DENIAL.has(witch)) return;
        event.setCancelled(true);
        Effects.denyLaunch(witch);
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
        if (!MobStatusEffect.DENIAL.has(mob)) return;
        Session session = plugin.sessions.of(player);
        session.combat.setPoisonFreebie(true);
    }

    /**
     * Disable the event if PoisonFreebie is set, see above.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Session session = plugin.sessions.of(player);
        if (!session.combat.isPoisonFreebie()) return;
        session.combat.setPoisonFreebie(false);
        if (event.getCause() != EntityPotionEffectEvent.Cause.ATTACK) {
            return;
        }
        switch (event.getAction()) {
        case ADDED: case CHANGED: break;
        default: return;
        }
        event.setCancelled(true);
    }
}
