package com.cavetale.skills.skill.combat;

import com.cavetale.skills.util.Players;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

@RequiredArgsConstructor
final class CombatListener implements Listener {
    protected final CombatSkill combatSkill;

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        Mob mob = (Mob) event.getEntity();
        if (!mob.isDead()) return;
        Player killer = mob.getKiller();
        if (killer == null) return;
        combatSkill.onPlayerKillMob(killer, mob, event);
        if (!(mob.getLastDamageCause() instanceof EntityDamageByEntityEvent edbee)) return;
        switch (edbee.getCause()) {
        case PROJECTILE:
            Projectile projectile = (Projectile) edbee.getDamager();
            if (!killer.equals(projectile.getShooter())) return;
            combatSkill.onArcherKill(killer, mob, projectile, edbee);
            return;
        case ENTITY_ATTACK:
        case ENTITY_SWEEP_ATTACK:
            if (!killer.equals(edbee.getDamager())) return;
            combatSkill.onMeleeKill(killer, mob);
            return;
        default: break;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            // Mob attacks player
            final Player player = (Player) event.getEntity();
            if (!Players.playMode(player)) return;
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
                combatSkill.onMobDamagePlayer(player, mob, proj, event);
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
            if (player != null && Players.playMode(player)) {
                combatSkill.onPlayerDamageMob(player, mob, proj, event);
            }
        }
    }
}
