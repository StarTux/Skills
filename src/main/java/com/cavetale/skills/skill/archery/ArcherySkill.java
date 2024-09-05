package com.cavetale.skills.skill.archery;

import com.cavetale.core.event.skills.SkillsMobKillRewardEvent;
import com.cavetale.mytems.event.combat.DamageCalculationEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.combat.CombatReward;
import com.cavetale.skills.talent.archery.*;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;
import static com.cavetale.skills.skill.combat.CombatSkill.addKillAndCheckCooldown;

/**
 * The Archery Skill.
 * Arrow#isCritical <=> EntityShootBowEvent.getForce() == 1.0
 * Arrow#getDamage() == 2.0 (always), Power: +1, +1.5, +2, +2.5, +3
 * Power: (lvl + 1) / 2 (wiki says /4, meaning hearts!)
 * Power Crit: 6, 9, 11, 12, 14, 15
 * AbstractArrow <- Arrow/SpectralArrow
 * TippedArrow is deprecated and now in Arrow!
 */
public final class ArcherySkill extends Skill implements Listener {
    public final InTheZoneTalent inTheZoneTalent = new InTheZoneTalent();
    public final ArrowSpeedTalent arrowSpeedTalent = new ArrowSpeedTalent();
    public final LegolasTalent legolasTalent = new LegolasTalent();
    public final SniperTalent sniperTalent = new SniperTalent();
    public final HomingArrowTalent homingArrowTalent = new HomingArrowTalent();
    public final ArrowMagnetTalent arrowMagnetTalent = new ArrowMagnetTalent();
    public final InstantHitTalent instantHitTalent = new InstantHitTalent();
    public final VolleyTalent volleyTalent = new VolleyTalent();
    public final WaterBombTalent waterBombTalent = new WaterBombTalent();
    public final GunslingerTalent gunslingerTalent = new GunslingerTalent();
    public final GlowMarkTalent glowMarkTalent = new GlowMarkTalent();

    public ArcherySkill() {
        super(SkillType.ARCHERY);
    }

    @Override
    public void enable() {
    }

    private void onArrowKill(Player player, AbstractArrow arrow, Mob mob, EntityDeathEvent event) {
        final CombatReward reward = addKillAndCheckCooldown(mob.getLocation())
            ? null
            : combatReward(mob);
        if (reward != null) {
            reward(player, arrow, mob, event, reward);
        }
        if (arrowMagnetTalent.isPlayerEnabled(player)) {
            final int exp = event.getDroppedExp();
            event.setDroppedExp(0);
            if (exp > 0) {
                player.giveExp(exp, true);
            }
            List<ItemStack> drops = List.copyOf(event.getDrops());
            event.getDrops().clear();
            for (ItemStack drop : drops) {
                Item item = player.getWorld().dropItem(player.getLocation(), drop);
                item.setPickupDelay(0);
                item.setOwner(player.getUniqueId());
            }
        }
    }

    private boolean reward(Player player, AbstractArrow arrow, Mob mob, EntityDeathEvent event, CombatReward reward) {
        final Session session = Session.of(player);
        if (!session.isEnabled()) return false;
        final var rewardEvent = new SkillsMobKillRewardEvent(player, mob,
                                                             reward.sp,
                                                             session.computeMoneyDrop(skillType, reward.money),
                                                             3 * event.getDroppedExp() + session.getExpBonus(skillType));
        rewardEvent.callEvent();
        if (rewardEvent.isCancelled()) return false;
        if (rewardEvent.getPostMultiplyFactor() != 1.0) {
            skillsPlugin().getLogger().info("[Archery] [" + mob.getType() + "] " + player.getName() + " " + rewardEvent.debugString());
        }
        final Location location = arrowMagnetTalent.isPlayerEnabled(player)
            ? player.getLocation()
            : mob.getLocation();
        session.addSkillPoints(skillType, rewardEvent.getFinalSkillPoints());
        dropMoney(player, location, rewardEvent.getFinalMoney());
        event.setDroppedExp(rewardEvent.getFinalExp());
        return true;
    }

    private void onArrowDamage(Player player, AbstractArrow arrow, Mob mob, EntityDamageByEntityEvent event) {
        if (!arrow.isShotFromCrossbow()) {
            inTheZoneTalent.onBowDamage(player, arrow, mob);
            legolasTalent.onBowDamage(player, arrow, mob);
        }
        if (isDebugSkill(player)) {
            player.sendMessage(skillType + " onArrowDamage "
                               + " arrowDmg=" + arrow.getDamage()
                               + " eventDmg=" + event.getDamage()
                               + " finalDmg=" + event.getFinalDamage()
                               + " crit=" + arrow.isCritical()
                               + " primary=" + ArrowType.PRIMARY.is(arrow)
                               + " bonus=" + ArrowType.BONUS.is(arrow)
                               + " weapon=" + (arrow.getWeapon() != null
                                               ? arrow.getWeapon().getType()
                                               : "null"));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getDamager() instanceof AbstractArrow arrow
            && !(arrow instanceof Trident)
            && arrow.getShooter() instanceof Player player
            && isPlayerEnabled(player)) {
            onArrowDamage(player, arrow, mob, event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        Mob mob = (Mob) event.getEntity();
        if (!mob.isDead()) return;
        if (!(mob.getLastDamageCause() instanceof EntityDamageByEntityEvent edbee)) return;
        switch (edbee.getCause()) {
        case PROJECTILE:
            if (edbee.getDamager() instanceof AbstractArrow arrow
                && !(arrow instanceof Trident)
                && arrow.getShooter() instanceof Player player
                && isPlayerEnabled(player)) {
                onArrowKill(player, arrow, mob, event);
            }
            return;
        default: break;
        }
    }

    /**
     * Mark shot arrows as primary.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (event.getProjectile() instanceof Trident) return;
        ItemStack bow = event.getBow();
        if (bow == null) return;
        ArrowType.PRIMARY.set(arrow);
        final ItemStack consumable = event.getConsumable();
        if (bow.getType() == Material.BOW) {
            onShootBow(player, arrow);
        } else if (bow.getType() == Material.CROSSBOW) {
            // Called 3 times in case of Multishot
            // The ones below we do not want to be called for custom
            // arrows.  Flame would be a consideration, but it will
            // simply adopt the flame status of the original arrow,
            // which is a simple solution.
            // The order matters:
            // - Volley will copy the flame state
            volleyTalent.onShootCrossbow(player, bow, arrow, consumable);
            gunslingerTalent.onShootCrossbow(player);
        }
        if (isDebugSkill(player)) {
            player.sendMessage(skillType + " " + event.getEventName()
                               + " " + bow.getType()
                               + " velo:" + arrow.getVelocity().length()
                               + " dmg:" + arrow.getDamage()
                               + " crit:" + arrow.isCritical()
                               + " force:" + event.getForce()
                               + " fire:" + arrow.getFireTicks()
                               + " consume:" + event.shouldConsumeItem());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow) || arrow instanceof Trident) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (event.getHitBlock() != null) {
            inTheZoneTalent.onArrowHitBlock(player, arrow);
            waterBombTalent.onArrowHitBlock(player, arrow, event);
            if (arrow.isDead()) return;
            if (ArrowType.BONUS.is(arrow) || ArrowType.SPAM.is(arrow) || ArrowType.NO_PICKUP.is(arrow)) {
                Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> arrow.remove(), 10L);
            } else if (arrow.getPickupStatus() != AbstractArrow.PickupStatus.ALLOWED) {
                Bukkit.getScheduler().runTaskLater(skillsPlugin(), () -> arrow.remove(), 10L);
            }
        }
        if (event.getHitEntity() != null) {
            final LivingEntity target;
            if (event.getHitEntity() instanceof LivingEntity living) {
                target = living;
            } else if (event.getHitEntity() instanceof ComplexEntityPart part) {
                target = part.getParent();
            } else {
                target = null;
            }
            if (target != null) {
                instantHitTalent.onArrowCollide(player, arrow, target);
            }
        }
    }

    /**
     * Called by onEntityShootBow and LegolasTalent.
     */
    public void onShootBow(Player player, AbstractArrow arrow) {
        inTheZoneTalent.onShootBow(player, arrow);
        arrowSpeedTalent.onShootBow(player, arrow);
        homingArrowTalent.onShootBow(player, arrow);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    private void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
        final Player player = event.getPlayer();
        final AbstractArrow arrow = event.getArrow();
        if (ArrowType.NO_PICKUP.is(arrow)) {
            event.setCancelled(true);
            arrow.remove();
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
    private void onPlayerAttemptPickupItem(PlayerAttemptPickupItemEvent event) {
        if (ArrowType.NO_PICKUP.is(event.getItem().getItemStack())) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    private void onDamageCalculation(DamageCalculationEvent event) {
        if (event.getTarget() == null) return;
        if (!event.getCalculation().isArrowAttack()) return;
        if (event.getCalculation().getTarget() == null) return;
        if (event.attackerIsPlayer()) {
            final Player player = event.getAttackerPlayer();
            final AbstractArrow arrow = event.getCalculation().getArrow();
            final LivingEntity target = event.getTarget();
            sniperTalent.onPlayerDamageEntityCalculation(player, arrow, target, event);
            glowMarkTalent.onPlayerDamageEntityCalculation(player, arrow, target, event);
        }
    }
}
