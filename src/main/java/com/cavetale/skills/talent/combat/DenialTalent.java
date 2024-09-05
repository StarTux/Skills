package com.cavetale.skills.talent.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.combat.MobStatusEffect;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import com.cavetale.worldmarker.entity.EntityMarker;
import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent;
import java.time.Duration;
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

public final class DenialTalent extends Talent implements Listener {
    public DenialTalent() {
        super(TalentType.DENIAL, "Denial",
              "Hitting monsters with knockback denies the following:",
              ":barrier: Shooting :arrow:Arrows",
              ":barrier: Throwing :splash_potion:Potions",
              ":barrier: :spider_face:Spider Poison",
              ":barrier: :creeper_face:Creeper Explosion",
              ":barrier: :enderman_face:Enderman Escape",
              "Use a Knockback weapon on an enemy to give it this status effect. You have to hit them with full strength!");
        addLevel(1, "Knockback denies mob spells, projectiles, poison for " + levelToSeconds(1) + " seconds");
        addLevel(1, "Knockback denies mob spells, projectiles, poison for " + levelToSeconds(2) + " seconds");
        addLevel(1, "Knockback denies mob spells, projectiles, poison for " + levelToSeconds(3) + " seconds");
        addLevel(1, "Knockback denies mob spells, projectiles, poison for " + levelToSeconds(4) + " seconds");
        addLevel(1, "Knockback denies mob spells, projectiles, poison for " + levelToSeconds(5) + " seconds");
    }

    private static int levelToSeconds(int level) {
        return level * 5;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.BARRIER);
    }

    /**
     * When a mob is damaged, apply the Denial effect.
     */
    public void onPlayerDamageMob(Player player, Mob mob, ItemStack item, EntityDamageByEntityEvent event) {
        if (player.getAttackCooldown() < 1.0f) return;
        if (!isPlayerEnabled(player)) return;
        if (item == null || item.getEnchantmentLevel(Enchantment.KNOCKBACK) == 0) return;
        if (mob instanceof Boss) return;
        if (EntityMarker.hasId(mob, "boss")) return;
        final int level = Session.of(player).getTalentLevel(talentType);
        if (level < 1) return;
        final int seconds = levelToSeconds(level);
        MobStatusEffect.DENIAL.set(mob, Duration.ofSeconds(seconds));
        Location eye = mob.getEyeLocation();
        mob.getWorld().spawnParticle(Particle.ENCHANT, eye, 32, 0.0, 0.0, 0.0, 0.5);
        mob.getWorld().playSound(eye, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.25f, 1.8f);
    }

    /**
     * Deny shooting bow.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Mob shooter)) return;
        if (!MobStatusEffect.DENIAL.has(shooter)) return;
        event.setCancelled(true);
    }

    /**
     * Deny throwing potions.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onWitchThrowPotion(WitchThrowPotionEvent event) {
        Mob witch = event.getEntity();
        if (!MobStatusEffect.DENIAL.has(witch)) return;
        event.setCancelled(true);
    }

    /**
     * This event is called before the poison is applied.  We remember
     * this by setting the PoisonFreebie, so that the effect below,
     * which does not know the causing entity, can pick up on it.
     */
    public void onMobDamagePlayer(Player player, Mob mob, EntityDamageByEntityEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (!isArthropod(mob)) return;
        if (!MobStatusEffect.DENIAL.has(mob)) return;
        Session.of(player).combat.setPoisonFreebie(true);
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
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Session session = Session.of(player);
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
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!MobStatusEffect.DENIAL.has(mob)) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEndermanEscape(EndermanEscapeEvent event) {
        if (!MobStatusEffect.DENIAL.has(event.getEntity())) return;
        event.setCancelled(true);
    }
}
