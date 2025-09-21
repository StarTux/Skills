package com.cavetale.skills.skill.combat;

import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
final class CombatListener implements Listener {
    protected final CombatSkill combatSkill;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDamageByEntityMonitor(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Mob mob) {
            // Mob attacks Player
            // if (event.getCause() == DamageCause.ENTITY_ATTACK) {
            // }
            return;
        } else if (event.getEntity() instanceof Mob mob && event.getDamager() instanceof Player player) {
            // Player attacks Mob
            if (event.getCause() == DamageCause.ENTITY_ATTACK) {
                final ItemStack item = player.getInventory().getItemInMainHand();
                combatSkill.berserkerTalent.onPlayerDamageMob(player, mob, event);
                combatSkill.denialTalent.onPlayerDamageMob(player, mob, item, event);
            }
        } else if (event.getEntity() instanceof Mob mob && event.getDamager() instanceof Trident trident && trident.getShooter() instanceof Player player) {
            combatSkill.stunPikeTalent.onPlayerHitMobWithTrident(player, trident, mob);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerDeath(PlayerDeathEvent event) {
        combatSkill.berserkerTalent.resetRage(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        combatSkill.berserkerTalent.resetRage(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    private void onDamageCalculation(DamageCalculationEvent event) {
        if (event.getTarget() == null) return;
        if (event.attackerIsPlayer()) {
            if (!event.getCalculation().isMeleeAttack()) return;
            final Player player = event.getAttackerPlayer();
            combatSkill.pyromaniacTalent.onDamageCalculation(player, event);
            combatSkill.berserkerTalent.onDamageCalculation(player, event);
            combatSkill.executionerTalent.onDamageCalculation(player, event);
            combatSkill.humanCannonballTalent.onDamageCalculation(player, event);
            combatSkill.chevalierTalent.onDamageCalculation(player, event);
            combatSkill.toxicistTalent.onDamageCalculation(player, event);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    private void onEntityResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        combatSkill.godModeTalent.onEntityResurrect(player, event);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    private void onPlayerLeftClick(PlayerInteractEvent event) {
        switch (event.getAction()) {
        case LEFT_CLICK_AIR:
        case LEFT_CLICK_BLOCK:
            break;
        default:
            return;
        }
        final Player player = event.getPlayer();
        combatSkill.slashAttackTalent.onPlayerLeftClick(player, event);
    }
}
