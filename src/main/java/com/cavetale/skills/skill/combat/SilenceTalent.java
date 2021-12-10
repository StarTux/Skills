package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.cavetale.worldmarker.entity.EntityMarker;
import java.time.Duration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public final class SilenceTalent extends Talent implements Listener {
    protected final CombatSkill combatSkill;

    protected SilenceTalent(final SkillsPlugin plugin, final CombatSkill combatSkill) {
        super(plugin, TalentType.COMBAT_SILENCE);
        this.combatSkill = combatSkill;
    }

    @Override
    protected void enable() { }

    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, Projectile projectile, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (projectile != null) return;
        if (item == null || item.getEnchantmentLevel(Enchantment.KNOCKBACK) == 0) return;
        if (mob instanceof Boss) return;
        if (EntityMarker.hasId(mob, "boss")) return;
        MobStatusEffect.SILENCE.set(mob, Duration.ofSeconds(20));
        Effects.applyStatusEffect(mob);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile proj = event.getEntity();
        if (!(proj.getShooter() instanceof Mob mob)) return;
        if (!MobStatusEffect.SILENCE.has(mob)) return;
        event.setCancelled(true);
        Effects.denyLaunch(mob);
    }
}
