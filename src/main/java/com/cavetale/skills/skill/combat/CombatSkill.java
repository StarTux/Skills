package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Talent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.util.Effects;
import com.cavetale.worldmarker.entity.EntityMarker;
import com.cavetale.worldmarker.util.Tags;
import java.time.Duration;
import java.util.EnumMap;
import lombok.NonNull;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CombatSkill extends Skill {
    protected final EnumMap<EntityType, Reward> rewards = new EnumMap<>(EntityType.class);
    protected final NamespacedKey killsKey;
    protected final NamespacedKey lastKillKey;

    protected static final long CHUNK_KILL_COOLDOWN = Duration.ofMinutes(5).toMillis();

    @Value
    static class Reward {
        final EntityType type;
        final int sp;
    }

    private void reward(@NonNull EntityType type, final int sp) {
        rewards.put(type, new Reward(type, sp));
    }

    public CombatSkill(@NonNull final SkillsPlugin plugin) {
        super(plugin, SkillType.COMBAT);
        this.killsKey = new NamespacedKey(plugin, "kills");
        this.lastKillKey = new NamespacedKey(plugin, "last_kill");
        MobStatusEffect.enable(plugin);
        reward(EntityType.ZOMBIE, 1);
        reward(EntityType.SKELETON, 1);
        reward(EntityType.CREEPER, 2);
        reward(EntityType.SLIME, 1);
        reward(EntityType.SILVERFISH, 1);
        reward(EntityType.POLAR_BEAR, 2);
        reward(EntityType.SHULKER, 2);
        reward(EntityType.SPIDER, 2);
        reward(EntityType.CAVE_SPIDER, 2);
        reward(EntityType.WITCH, 5);
        reward(EntityType.ZOMBIE_VILLAGER, 2);
        reward(EntityType.ENDERMITE, 2);
        reward(EntityType.BLAZE, 3);
        reward(EntityType.ELDER_GUARDIAN, 3);
        reward(EntityType.EVOKER, 3);
        reward(EntityType.GUARDIAN, 3);
        reward(EntityType.HUSK, 3);
        reward(EntityType.MAGMA_CUBE, 3);
        reward(EntityType.PHANTOM, 3);
        reward(EntityType.VEX, 2);
        reward(EntityType.VINDICATOR, 3);
        reward(EntityType.WITHER_SKELETON, 4);
        reward(EntityType.GHAST, 5);
        reward(EntityType.STRAY, 1);
        reward(EntityType.DROWNED, 1);
        reward(EntityType.ILLUSIONER, 1);
        reward(EntityType.GIANT, 1);
        reward(EntityType.PIGLIN, 1);
        reward(EntityType.PIGLIN_BRUTE, 5);
        reward(EntityType.ZOMBIFIED_PIGLIN, 1);
        reward(EntityType.ENDERMAN, 1);
        reward(EntityType.ENDER_DRAGON, 10);
        reward(EntityType.WITHER, 10);
        reward(EntityType.HOGLIN, 3);
        reward(EntityType.ZOGLIN, 3);
        reward(EntityType.PILLAGER, 3);
        reward(EntityType.RAVAGER, 5);
    }

    public void playerKillMob(Player player, Mob mob, EntityDeathEvent event) {
        Reward reward = rewards.get(mob.getType());
        if (reward == null) return;
        if (mob instanceof Ageable && !((Ageable) mob).isAdult()) return;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        boolean rewarded;
        if (mob.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) mob.getLastDamageCause();
            switch (edbee.getCause()) {
            case PROJECTILE:
                rewarded = sniperKill(session, mob, edbee);
                break;
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                rewarded = meleeKill(session, mob, edbee);
                break;
            default:
                rewarded = false;
                break;
            }
        } else {
            rewarded = false;
        }
        final PersistentDataContainer pdc = mob.getLocation().getChunk().getPersistentDataContainer();
        final long now = System.currentTimeMillis();
        final Integer oldKills = Tags.getInt(pdc, killsKey);
        final Long oldLastKill = Tags.getLong(pdc, lastKillKey);
        int kills = oldKills != null ? oldKills : 0;
        long lastKill = oldLastKill != null ? oldLastKill : 0L;
        kills = now - lastKill < CHUNK_KILL_COOLDOWN ? kills + 1 : 0;
        Tags.set(pdc, killsKey, kills);
        Tags.set(pdc, lastKillKey, now);
        if (kills > 50) return;
        if (rewarded) {
            session.addSkillPoints(SkillType.COMBAT, reward.sp);
            event.setDroppedExp(event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
            Effects.kill(mob);
        }
    }

    protected boolean sniperKill(Session session, Mob mob, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) return false;
        Projectile proj = (Projectile) event.getDamager();
        if (!(proj.getShooter() instanceof Player)) return false;
        Player player = (Player) proj.getShooter();
        if (session.isTalentEnabled(Talent.COMBAT_ARCHER_ZONE)) {
            session.setArcherZone(5 * 20);
            session.setArcherZoneKills(session.getArcherZoneKills() + 1);
            player.sendActionBar(Component.join(JoinConfiguration.noSeparators(), new Component[] {
                        Component.text("In The Zone! ", NamedTextColor.RED),
                        Component.text(session.getArcherZoneKills(), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
                    }));
            Effects.archerZone(player);
        }
        return true;
    }

    protected boolean meleeKill(Session session, Mob mob, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return false;
        Player player = (Player) event.getDamager();
        if (session.isTalentEnabled(Talent.COMBAT_GOD_MODE)) {
            if (session.getImmortal() <= 0) {
                player.sendActionBar(Component.text("God Mode!", NamedTextColor.GOLD));
            }
            session.setImmortal(3 * 20);
        }
        return true;
    }

    public void mobDamagePlayer(@NonNull Player player, @NonNull Mob mob,
                                   Projectile proj,
                                   @NonNull EntityDamageByEntityEvent event) {
        final boolean ranged = proj != null;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        // -50% damage on melee
        if (session.isTalentEnabled(Talent.COMBAT_FIRE)
            && !ranged
            && mob.getFireTicks() > 0) {
            event.setDamage(event.getFinalDamage() * 0.5);
        }
        // Spider
        if (session.isTalentEnabled(Talent.COMBAT_SPIDERS) && !ranged
            && MobStatusEffect.NO_POISON.has(mob)) {
            session.setPoisonFreebie(true);
        }
    }

    protected static boolean isSpider(Entity entity) {
        switch (entity.getType()) {
        case SPIDER:
        case CAVE_SPIDER:
        case SILVERFISH:
        case ENDERMITE:
            return true;
        default:
            return false;
        }
    }

    protected static void potion(LivingEntity e, PotionEffectType type,
                       final int level, final int seconds) {
        e.addPotionEffect(new PotionEffect(type, seconds * 20,
                                           level - 1, true, false));
    }

    protected boolean silenceEffect(@NonNull Mob mob) {
        if (mob instanceof Boss) return false;
        String id = EntityMarker.getId(mob);
        if (id != null && id.contains("boss")) return false;
        MobStatusEffect.SILENCE.set(mob, Duration.ofSeconds(20));
        Effects.applyStatusEffect(mob);
        return true;
    }

    public void playerDamageMob(@NonNull Player player, @NonNull Mob mob,
                                Projectile proj,
                                @NonNull EntityDamageByEntityEvent event) {
        final boolean ranged = proj != null;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        final ItemStack item = player.getInventory().getItemInMainHand();
        // +50% damage
        if (session.isTalentEnabled(Talent.COMBAT_FIRE)
            && mob.getFireTicks() > 0) {
            event.setDamage(event.getFinalDamage() * 1.5);
        }
        // Knockback => Silence
        if (session.isTalentEnabled(Talent.COMBAT_SILENCE)
            && !ranged
            && item != null
            && item.getEnchantmentLevel(Enchantment.KNOCKBACK) > 0) {
            silenceEffect(mob);
        }
        // Spider => Slow + NoPoison
        if (session.isTalentEnabled(Talent.COMBAT_SPIDERS)
            && !ranged
            && item != null
            && item.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS) > 0
            && isSpider(mob)) {
            MobStatusEffect.NO_POISON.set(mob, Duration.ofSeconds(30));
            potion(mob, PotionEffectType.SLOW, 3, 30);
            Effects.applyStatusEffect(mob);
        }
        // In The Zone
        if (session.isTalentEnabled(Talent.COMBAT_ARCHER_ZONE)
            && ranged
            && session.getArcherZone() > 0) {
            event.setDamage(event.getFinalDamage() * 2.0);
        }
    }
}
