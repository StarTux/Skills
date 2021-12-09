package com.cavetale.skills.skill.combat;

import com.cavetale.skills.Util;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@RequiredArgsConstructor
final class CombatListener implements Listener {
    protected final CombatSkill combatSkill;

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
            if (!mob.isDead()) return;
            Player killer = entity.getKiller();
            if (killer != null) {
                combatSkill.playerKillMob(killer, mob, event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            // Mob attacks player
            final Player player = (Player) event.getEntity();
            if (!Util.playMode(player)) return;
            Mob mob = null;
            Projectile proj = null;
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Mob) {
                    mob = (Mob) event.getDamager();
                }
                break;
            case PROJECTILE:
                if (event.getDamager() instanceof Projectile) {
                    proj = (Projectile) event.getDamager();
                    if (proj.getShooter() instanceof Mob) {
                        mob = (Mob) proj.getShooter();
                    }
                }
                break;
            default:
                break;
            }
            if (mob != null) {
                combatSkill.mobDamagePlayer(player, mob, proj, event);
            }
        } else if (event.getEntity() instanceof Mob) {
            // Player attacks mob
            final Mob mob = (Mob) event.getEntity();
            Player player = null;
            Projectile proj = null;
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Player) {
                    player = (Player) event.getDamager();
                }
                break;
            case PROJECTILE:
                if (event.getDamager() instanceof Projectile) {
                    proj = (Projectile) event.getDamager();
                    if (proj.getShooter() instanceof Player) {
                        player = (Player) proj.getShooter();
                    }
                }
                break;
            default:
                break;
            }
            if (player != null && Util.playMode(player)) {
                combatSkill.playerDamageMob(player, mob, proj, event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    protected void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            combatSkill.plugin.sessions.apply(player, session -> {
                    double health = player.getHealth();
                    if (session.isTalentEnabled(TalentType.COMBAT_GOD_MODE)
                        && session.getImmortal() > 0
                        && health <= event.getFinalDamage()) {
                        event.setDamage(Math.max(0.0, health - 1.0));
                        Effects.godMode(player);
                        player.sendActionBar(Component.text("God Mode Save!", NamedTextColor.GOLD));
                    }
                });
        } else {
            Entity entity = event.getEntity();
        }
    }

    @EventHandler(ignoreCancelled = true)
    protected void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile proj = event.getEntity();
        if (proj.getShooter() instanceof Mob) {
            Mob mob = (Mob) proj.getShooter();
            if (MobStatusEffect.SILENCE.has(mob)) {
                Effects.denyLaunch(mob);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    protected void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (event.getCause() == EntityPotionEffectEvent.Cause.ATTACK) {
            switch (event.getAction()) {
            case ADDED: case CHANGED:
                combatSkill.plugin.sessions.apply(player, session -> {
                        if (!session.isTalentEnabled(TalentType.COMBAT_SPIDERS)) return;
                        if (!session.isPoisonFreebie()) return;
                        session.setPoisonFreebie(false);
                        event.setCancelled(true);
                    });
            default: break;
            }
        }
    }
}
