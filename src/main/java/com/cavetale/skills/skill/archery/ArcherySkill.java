package com.cavetale.skills.skill.archery;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.combat.CombatReward;
import com.cavetale.worldmarker.util.Tags;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Item;
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
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.moneyBonusPercentage;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;
import static com.cavetale.skills.skill.combat.CombatSkill.addKillAndCheckCooldown;

/**
 * The Archery Skill.
 * Arrow#isCritical <=> EntityShootBowEvent.getForce() == 1.0
 * Arrow#getDamage() == 2.0 (always), Power: +1, +1.5, +2, +2.5, +3
 * Power: (lvl + 1) / 2 (wiki says /4, meaning hearts!)
 * Power Crit: 6, 9, 11, 12, 14, 15
 */
public final class ArcherySkill extends Skill implements Listener {
    protected static final NamespacedKey PRIMARY_ARROW = NamespacedKey.fromString("skills:primary_arrow");
    protected static final NamespacedKey BONUS_ARROW = NamespacedKey.fromString("skills:bonus_arrow");
    protected static final NamespacedKey SPAM_ARROW = NamespacedKey.fromString("skills:spam_arrow");
    public final ArcherZoneTalent archerZoneTalent = new ArcherZoneTalent();
    public final ArcherZoneDeathTalent archerZoneDeathTalent = new ArcherZoneDeathTalent();
    public final ArrowSwiftnessTalent arrowSwiftnessTalent = new ArrowSwiftnessTalent();
    public final ArrowDamageTalent arrowDamageTalent = new ArrowDamageTalent();
    public final BonusArrowTalent bonusArrowTalent = new BonusArrowTalent();
    public final ArrowMagnetTalent arrowMagnetTalent = new ArrowMagnetTalent();

    public ArcherySkill() {
        super(SkillType.ARCHERY);
    }

    @Override
    public void enable() {
    }

    private void onArrowKill(Player player, AbstractArrow arrow, Mob mob, EntityDeathEvent event) {
        archerZoneDeathTalent.onArrowKill(player, arrow, mob);
        Session session = sessionOf(player);
        CombatReward reward = addKillAndCheckCooldown(mob.getLocation())
            ? null
            : combatReward(mob);
        final boolean hasMagnet = arrowMagnetTalent.isPlayerEnabled(player);
        if (reward != null) {
            session.addSkillPoints(skillType, reward.sp);
            if (reward.money > 0) {
                int bonus = session.getMoneyBonus(skillType);
                double factor = 1.0 + 0.01 * moneyBonusPercentage(bonus);
                Location location = hasMagnet ? player.getLocation() : mob.getLocation();
                dropMoney(player, location, reward.money * factor);
            }
            event.setDroppedExp(3 * event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
        }
        if (hasMagnet) {
            int exp = event.getDroppedExp();
            event.setDroppedExp(0);
            player.giveExp(exp, true);
            List<ItemStack> drops = List.copyOf(event.getDrops());
            event.getDrops().clear();
            for (ItemStack drop : drops) {
                Item item = player.getWorld().dropItem(player.getLocation(), drop);
                item.setPickupDelay(0);
                item.setOwner(player.getUniqueId());
            }
        }
    }

    private void onArrowDamage(Player player, AbstractArrow arrow, Mob mob, EntityDamageByEntityEvent event) {
        archerZoneTalent.onArrowDamage(player, arrow, mob);
        bonusArrowTalent.onArrowDamage(player, arrow, mob);
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(skillType + " onArrowDamage "
                               + " arrowDmg=" + arrow.getDamage()
                               + " eventDmg=" + event.getDamage()
                               + " finalDmg=" + event.getFinalDamage()
                               + " crit=" + arrow.isCritical()
                               + " primary=" + isPrimaryArrow(arrow)
                               + " bonus=" + isBonusArrow(arrow));
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

    protected void onShootArrow(Player player, AbstractArrow arrow) {
        archerZoneTalent.onShootArrow(player, arrow);
        arrowSwiftnessTalent.onShootArrow(player, arrow);
        arrowDamageTalent.onShootArrow(player, arrow);
    }

    /**
     * Mark shot arrows as primary.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isPlayerEnabled(player)) return;
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        ItemStack bow = event.getBow();
        if (bow == null) return;
        setPrimaryArrow(arrow);
        onShootArrow(player, arrow);
        // if (bow.getType() == Material.BOW) {
        // } else if (bow.getType() == Material.CROSSBOW) {
        // }
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(skillType + " " + event.getEventName()
                               + " " + bow.getType()
                               + " dmg=" + arrow.getDamage()
                               + " crit=" + arrow.isCritical()
                               + " force=" + event.getForce());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow) || arrow instanceof Trident) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (event.getHitBlock() != null) {
            archerZoneTalent.onArrowHitBlock(player, arrow);
        }
        if (isBonusArrow(arrow) || isSpamArrow(arrow)) {
            Bukkit.getScheduler().runTask(skillsPlugin(), () -> arrow.remove());
        } else if (arrow.getPickupStatus() != AbstractArrow.PickupStatus.ALLOWED) {
            Bukkit.getScheduler().runTask(skillsPlugin(), () -> arrow.remove());
        }
    }

    /**
     * Arrow show manually via bow or crossbow.
     */
    public static boolean isPrimaryArrow(AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().has(PRIMARY_ARROW);
    }

    public static void setPrimaryArrow(AbstractArrow arrow) {
        Tags.set(arrow.getPersistentDataContainer(), PRIMARY_ARROW, (byte) 1);
    }

    /**
     * Arrow shot by talent.  Not spam.
     */
    public static boolean isBonusArrow(AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().has(BONUS_ARROW);
    }

    public static void setBonusArrow(AbstractArrow arrow) {
        Tags.set(arrow.getPersistentDataContainer(), BONUS_ARROW, (byte) 1);
    }

    public static boolean isSpamArrow(AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().has(SPAM_ARROW);
    }

    public static void setSpawmArrow(AbstractArrow arrow) {
        Tags.set(arrow.getPersistentDataContainer(), SPAM_ARROW, (byte) 1);
    }

}
