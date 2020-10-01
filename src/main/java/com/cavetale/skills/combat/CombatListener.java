package com.cavetale.skills.combat;

import com.cavetale.skills.Effects;
import com.cavetale.skills.Session;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Talent;
import com.cavetale.skills.util.Util;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
public final class CombatListener implements Listener {
    final SkillsPlugin plugin;
    final CombatSkill combat;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
            if (!mob.isDead()) return;
            Player killer = entity.getKiller();
            if (killer != null) {
                combat.playerKillMob(killer, mob);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
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
                combat.mobDamagePlayer(player, mob, proj, event);
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
                combat.playerDamageMob(player, mob, proj, event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Session session = plugin.getSessions().of(player);
            double health = player.getHealth();
            if (session.hasTalent(Talent.COMBAT_GOD_MODE)
                && session.getImmortal() > 0
                && health <= event.getFinalDamage()) {
                event.setDamage(Math.max(0.0, health - 1.0));
                Effects.godMode(player);
                player.sendActionBar(ChatColor.GOLD + "God Mode Save!");
            }
        } else {
            Entity entity = event.getEntity();
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile proj = event.getEntity();
        if (proj.getShooter() instanceof Mob) {
            Mob mob = (Mob) proj.getShooter();
            if (combat.statusEffectOf(mob).hasSilence()) {
                Effects.denyLaunch(mob);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Session session = plugin.getSessions().of(player);
            if (session.hasTalent(Talent.COMBAT_SPIDERS)
                && session.isPoisonFreebie()
                && event.getCause() == EntityPotionEffectEvent.Cause.ATTACK) {
                session.setPoisonFreebie(false);
                event.setCancelled(true);
            }
        }
    }
}
