package com.cavetale.skills.skill.combat;

import com.cavetale.skills.util.Players;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
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
        if (!(mob.getLastDamageCause() instanceof EntityDamageByEntityEvent edbee)) return;
        switch (edbee.getCause()) {
        case ENTITY_ATTACK:
        case ENTITY_SWEEP_ATTACK:
            if (!killer.equals(edbee.getDamager())) return;
            combatSkill.onMeleeKill(killer, mob, event);
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
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Mob mob) {
                    combatSkill.onMobDamagePlayer(player, mob, event);
                }
                break;
            default: return;
            }
        } else if (event.getEntity() instanceof Mob) {
            // Player attacks mob
            final Mob mob = (Mob) event.getEntity();
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Player player) {
                    combatSkill.onPlayerDamageMob(player, mob, event);
                }
                break;
            default: return;
            }
        }
    }
}
