package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.worldmarker.entity.EntityMarker;
import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent;
import java.time.Duration;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class DenialTalent extends Talent implements Listener {
    private static final int SECONDS = 20;
    private static final Duration DURATION = Duration.ofSeconds(SECONDS);

    protected DenialTalent() {
        super(TalentType.DENIAL);
    }

    @Override
    public String getDisplayName() {
        return "Denial";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Knockback denies mob spells, projectiles,"
                       + " poison for " + SECONDS + " seconds",
                       "This effect denies the following for "
                       + SECONDS + " seconds"
                       + ":\n"
                       + "\n:barrier: Shooting :arrow:Arrows"
                       + "\n:barrier: Throwing :splash_potion:Potions"
                       + "\n:barrier: :spider_face:Spider Poison"
                       + "\n:barrier: :creeper_face:Creeper Explosion"
                       + "\n:barrier: :enderman_face:Enderman Escape",
                       "Use a Knockback weapon on an enemy to give it this status effect."
                       + " You have to hit them with full strength!");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.BARRIER);
    }

    /**
     * When a mob is damaged, apply the Denial effect.
     */
    protected void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (player.getAttackCooldown() < 1.0f) return;
        if (!isPlayerEnabled(player)) return;
        if (item == null || item.getEnchantmentLevel(Enchantment.KNOCKBACK) == 0) return;
        if (mob instanceof Boss) return;
        if (EntityMarker.hasId(mob, "boss")) return;
        MobStatusEffect.DENIAL.set(mob, DURATION);
        Location eye = mob.getEyeLocation();
        mob.getWorld().spawnParticle(Particle.ENCHANT, eye, 32, 0.0, 0.0, 0.0, 0.5);
        mob.getWorld().playSound(eye, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.25f, 1.8f);
    }

    /**
     * Deny shooting bow.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Mob shooter)) return;
        if (!MobStatusEffect.DENIAL.has(shooter)) return;
        event.setCancelled(true);
    }

    /**
     * Deny throwing potions.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onWitchThrowPotion(WitchThrowPotionEvent event) {
        Mob witch = event.getEntity();
        if (!MobStatusEffect.DENIAL.has(witch)) return;
        event.setCancelled(true);
    }

    /**
     * This event is called before the poison is applied.  We remember
     * this by setting the PoisonFreebie, so that the effect below,
     * which does not know the causing entity, can pick up on it.
     */
    protected void onMobDamagePlayer(Player player, Mob mob, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!isArthropod(mob)) return;
        if (!MobStatusEffect.DENIAL.has(mob)) return;
        sessionOf(player).combat.setPoisonFreebie(true);
    }

    private boolean isArthropod(Mob mob) {
        switch (mob.getType()) {
        case BEE:
        case CAVE_SPIDER:
        case SPIDER:
        case ENDERMITE:
        case SILVERFISH:
            return true;
        default:
            return false;
        }
    }

    /**
     * Disable the event if PoisonFreebie is set, see above.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Session session = sessionOf(player);
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!MobStatusEffect.DENIAL.has(mob)) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    protected void onEndermanEscape(EndermanEscapeEvent event) {
        if (!MobStatusEffect.DENIAL.has(event.getEntity())) return;
        event.setCancelled(true);
    }
}
