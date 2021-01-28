package com.cavetale.skills;

import com.cavetale.worldmarker.EntityMarker;
import java.util.EnumMap;
import lombok.NonNull;
import lombok.Value;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

final class Combat {
    final SkillsPlugin plugin;
    final EnumMap<EntityType, Reward> rewards = new EnumMap<>(EntityType.class);
    static final String CHONK = "skills:chonk";
    static final String STATUS_EFFECT = "skills:status_effect";

    @Value
    static class Reward {
        final EntityType type;
        final int sp;
    }

    static class Chonk {
        Chonk() {
            time = Util.now();
        }

        int kills;
        long time;
    }

    private void reward(@NonNull EntityType type, final int sp) {
        rewards.put(type, new Reward(type, sp));
    }

    Combat(@NonNull final SkillsPlugin plugin) {
        this.plugin = plugin;
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

    void playerKillMob(Player player, Mob mob, EntityDeathEvent event) {
        Reward reward = rewards.get(mob.getType());
        if (reward == null) return;
        if (mob instanceof Ageable && !((Ageable) mob).isAdult()) return;
        Session session = plugin.sessionOf(player);
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
        Chunk chunk = mob.getLocation().getChunk();
        Chonk chonk = plugin.meta.getOrSet(chunk.getBlock(0, 0, 0),
                                           CHONK, Chonk.class, Chonk::new);
        long now = Util.now();
        chonk.kills = Math.max(0, chonk.kills + 10 - (int) (now - chonk.time));
        chonk.time = Util.now();
        if (chonk.kills > 50) return;
        if (rewarded) {
            plugin.addSkillPoints(player, SkillType.COMBAT, reward.sp);
            event.setDroppedExp(event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
            Effects.kill(mob);
        }
    }

    private boolean sniperKill(Session session, Mob mob, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) return false;
        Projectile proj = (Projectile) event.getDamager();
        if (!(proj.getShooter() instanceof Player)) return false;
        Player player = (Player) proj.getShooter();
        if (session.isTalentEnabled(Talent.COMBAT_ARCHER_ZONE)) {
            session.archerZone = 5 * 20;
            player.sendMessage(ChatColor.GOLD + "In The Zone! "
                               + ChatColor.LIGHT_PURPLE + ChatColor.BOLD
                               + ++session.archerZoneKills);
            Effects.archerZone(player);
        }
        return true;
    }

    private boolean meleeKill(Session session, Mob mob, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return false;
        Player player = (Player) event.getDamager();
        if (session.isTalentEnabled(Talent.COMBAT_GOD_MODE)) {
            if (session.immortal <= 0) {
                player.sendMessage(ChatColor.GOLD + "God Mode!");
            }
            session.immortal = 3 * 20;
        }
        return true;
    }

    StatusEffect statusEffectOf(@NonNull LivingEntity entity) {
        StatusEffect result = plugin.meta.get(entity, STATUS_EFFECT, StatusEffect.class)
            .orElse(null);
        if (result == null) {
            result = new StatusEffect();
            plugin.meta.set(entity, STATUS_EFFECT, result);
        }
        return result;
    }

    void mobDamagePlayer(@NonNull Player player, @NonNull Mob mob,
                         Projectile proj,
                         @NonNull EntityDamageByEntityEvent event) {
        final boolean ranged = proj != null;
        Session session = plugin.sessionOf(player);
        // -50% damage on melee
        if (session.isTalentEnabled(Talent.COMBAT_FIRE)
            && !ranged
            && mob.getFireTicks() > 0) {
            event.setDamage(event.getFinalDamage() * 0.5);
        }
        // Spider
        if (session.isTalentEnabled(Talent.COMBAT_SPIDERS)
            && !ranged
            && statusEffectOf(mob).hasNoPoison()) {
            session.poisonFreebie = true;
        }
    }

    static boolean isSpider(Entity entity) {
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

    static void potion(LivingEntity e, PotionEffectType type,
                       final int level, final int seconds) {
        e.addPotionEffect(new PotionEffect(type, seconds * 20,
                                           level - 1, true, false));
    }

    boolean silenceEffect(@NonNull Mob mob) {
        if (mob instanceof Boss) return false;
        String id = EntityMarker.getId(mob);
        if (id != null && id.contains("boss")) return false;
        statusEffectOf(mob).silence = Util.now() + 20;
        Effects.applyStatusEffect(mob);
        return true;
    }

    void playerDamageMob(@NonNull Player player, @NonNull Mob mob,
                         Projectile proj,
                         @NonNull EntityDamageByEntityEvent event) {
        final boolean ranged = proj != null;
        Session session = plugin.sessionOf(player);
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
            statusEffectOf(mob).noPoison = Util.now() + 30;
            potion(mob, PotionEffectType.SLOW, 3, 30);
            Effects.applyStatusEffect(mob);
        }
        // In The Zone
        if (session.isTalentEnabled(Talent.COMBAT_ARCHER_ZONE)
            && ranged
            && session.archerZone > 0) {
            event.setDamage(event.getFinalDamage() * 2.0);
        }
    }
}
