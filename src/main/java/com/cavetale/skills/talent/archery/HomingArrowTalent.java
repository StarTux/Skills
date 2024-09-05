package com.cavetale.skills.talent.archery;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import com.cavetale.worldmarker.util.Tags;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import static com.cavetale.skills.SkillsPlugin.namespacedKey;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;

public final class HomingArrowTalent extends Talent {
    private static final String NAMESPACE_KEY = "homing_arrow_target";

    public HomingArrowTalent() {
        super(TalentType.HOMING_ARROW, "Homing Arrow",
              ":bow:Bow arrows :magnet:follow their target by adjusting their trajectory",
              "Hitting a mob will make it your next target. Otherwise, :arrow:arrows will try to seek out a target.");
        addLevel(1, "Homing Arrow");
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Material.COMPASS);
    }

    @RequiredArgsConstructor
    private final class ArrowTask extends BukkitRunnable {
        private final Player player;
        private final Session session;
        private final AbstractArrow arrow;
        private int cooldown = 0;

        public boolean isStillValid() {
            return session.isEnabled()
                && arrow.isValid()
                && !arrow.isDead()
                && !arrow.isInBlock()
                && !player.isDead()
                && player.getWorld().equals(arrow.getWorld())
                && arrow.getShooter().equals(player);
        }

        @Override
        public void run() {
            if (!isStillValid()) {
                cancel();
                return;
            }
            if (cooldown > 1) {
                cooldown -= 1;
                return;
            }
            // Each arrow can have its own target stored.  We try to
            // stick with it.
            final LivingEntity arrowTarget = getArrowTarget(arrow);
            if (arrowTarget != null) {
                if (tryToFollow(player, arrow, arrowTarget, "local")) {
                    return;
                } else {
                    setArrowTarget(arrow, null);
                }
            }
            // If that fails, we try to find a new target for this arrow.
            final LivingEntity newTarget = findTarget(player, arrow);
            if (newTarget != null) {
                setArrowTarget(arrow, newTarget);
                tryToFollow(player, arrow, newTarget, "new");
                if (isDebugTalent(player)) {
                    player.sendMessage(talentType + " new target " + newTarget.getType() + " " + newTarget.getEntityId());
                }
            } else {
                cooldown = 2;
            }
        }
    }

    public void onShootBow(final Player player, final AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        final Session session = Session.of(player);
        final ArrowTask task = new ArrowTask(player, session, arrow);
        task.runTaskTimer(skillsPlugin(), 1L, 1L);
    }

    private LivingEntity getArrowTarget(AbstractArrow arrow) {
        final String string = Tags.getString(arrow.getPersistentDataContainer(), namespacedKey(NAMESPACE_KEY));
        if (string == null) return null;
        final UUID uuid = UUID.fromString(string);
        final Entity entity = Bukkit.getEntity(uuid);
        if (!(entity instanceof LivingEntity result) || result.isDead() || !result.getWorld().equals(arrow.getWorld())) {
            setArrowTarget(arrow, null);
            return null;
        }
        return result;
    }

    private void setArrowTarget(AbstractArrow arrow, LivingEntity target) {
        if (target == null) {
            arrow.getPersistentDataContainer().remove(namespacedKey(NAMESPACE_KEY));
        } else {
            Tags.set(arrow.getPersistentDataContainer(), namespacedKey(NAMESPACE_KEY), target.getUniqueId().toString());
        }
    }

    private static final double ANGLE_THRESHOLD = Math.PI / 4.0;
    private static final double MAX_ANGLE_ADJUSTMENT = Math.PI / 24.0;

    private boolean tryToFollow(Player player, AbstractArrow arrow, LivingEntity target, String debugString) {
        final Location targetLocation = target.getLocation();
        final Location arrowLocation = arrow.getLocation();
        final Vector currentVelocity = arrow.getVelocity();
        final double currentSpeed = currentVelocity.length(); // blocks per tick
        if (currentSpeed < 0.01) return false;
        // Adjust target location by current movement
        final double travelTime = targetLocation.distance(arrowLocation) / currentSpeed; // ticks
        targetLocation.add(target.getVelocity().multiply(travelTime));
        targetLocation.add(0.0, 0.045 * travelTime, 0.0);
        final Vector targetVelocity = targetLocation.add(0.0, target.getHeight(), 0.0).toVector()
            .subtract(arrowLocation.toVector());
        final double angleInRadians = currentVelocity.angle(targetVelocity);
        if (angleInRadians > ANGLE_THRESHOLD) return false;
        if (angleInRadians < 0.001) return true;
        final double angleAdjustment = Math.min(MAX_ANGLE_ADJUSTMENT, angleInRadians);
        final double adjustmentFactor = angleAdjustment / angleInRadians;
        final Vector newVelocity = currentVelocity.normalize().multiply(1.0 - angleAdjustment)
            .add(targetVelocity.normalize().multiply(angleAdjustment))
            .normalize()
            .multiply(currentSpeed);
        arrow.setVelocity(newVelocity);
        if (isDebugTalent(player)) {
            final Location dist = target.getLocation().subtract(targetLocation);
            player.sendMessage(talentType + " adjust " + target.getType() + " " + debugString
                               + " " + String.format("%.2f %.2f %.2f", dist.getX(), dist.getY(), dist.getZ())
                               + " travelTime=" + String.format("%.2f", travelTime));
        }
        return true;
    }

    private LivingEntity findTarget(Player player, AbstractArrow arrow) {
        final double distance = 24.0;
        final Vector facing = arrow.getVelocity().normalize();
        final Vector arrowVector = arrow.getLocation().toVector();
        final Vector center = arrowVector.clone().add(facing.clone().multiply(distance));
        final BoundingBox bb = BoundingBox.of(center, distance, distance, distance);
        double min = ANGLE_THRESHOLD;
        LivingEntity target = null;
        for (Entity nearby : arrow.getWorld().getNearbyEntities(bb)) {
            if (nearby.isDead()) continue;
            if (!(nearby instanceof LivingEntity living)) continue;
            switch (living.getType()) {
            case ENDERMAN:
            case BREEZE:
                continue;
            default: break;
            }
            if (combatReward(nearby) == null) continue;
            if (!player.hasLineOfSight(nearby)) continue;
            final Vector direction = living.getLocation().add(0.0, living.getHeight() * 0.5, 0.0).toVector()
                .subtract(arrowVector);
            final double angle = facing.angle(direction);
            if (angle < min) {
                min = angle;
                target = living;
            }
        }
        return target;
    }
}
