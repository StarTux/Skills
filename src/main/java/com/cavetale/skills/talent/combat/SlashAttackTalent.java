package com.cavetale.skills.talent.combat;

import com.cavetale.core.event.entity.PlayerEntityAbilityQuery;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import static com.cavetale.skills.util.Text.formatDouble;

public final class SlashAttackTalent extends Talent {
    public SlashAttackTalent() {
        super(TalentType.SLASH_ATTACK, "Slash Attack",
              "A missed melee attack will hit a mob in front of you.");
        addLevel(1, "Slash Attack");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.STEEL_BROADSWORD);
    }

    public void onPlayerLeftClick(Player player, PlayerInteractEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (player.getAttackCooldown() < 1f) return;
        final ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!MeleeWeapon.isMeleeWeapon(weapon)) return;
        final Mob target = getLookAtEntity(player);
        if (target == null) return;
        double baseDamage = player.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
        final boolean crit = isCrit(player);
        // Crit
        if (crit) {
            baseDamage *= 1.5;
        }
        // Enchantments
        final int sharpness = weapon.getEnchantmentLevel(Enchantment.SHARPNESS);
        if (sharpness > 0) {
            // 0.5 + level * 0.5
            // See https://minecraft.wiki/w/Sharpness#Usage
            baseDamage += 0.5 + sharpness * 0.5;
        }
        if (baseDamage < 0.001) return;
        target.damage(baseDamage, DamageSource.builder(DamageType.PLAYER_ATTACK)
                      .withCausingEntity(player)
                      .withDirectEntity(player)
                      .withDamageLocation(target.getEyeLocation())
                      .build());
        if (isDebugTalent(player)) {
            player.sendMessage(talentType.name() + " target:" + target.getType()
                               + " crit:" + crit
                               + " sharp:" + sharpness
                               + " dmg:" + formatDouble(baseDamage));
        }
    }

    @SuppressWarnings("deprecation") // isOnGround is deprecated
    private static boolean isCrit(Player player) {
        // A player must be falling.
        // A player must not be on the ground.
        // A player must not be on a ladder or any type of vine.
        // A player must not be in water.
        // A player must not be affected by Blindness.
        // A player must not be affected by Slow Falling.
        // A player must not be riding an entity.
        // A player must not be flying.
        // The attack cooldown must not be below 84.8%.
        // See https://minecraft.wiki/w/Damage#Critical_hit
        if (player.getVelocity().getY() > -0.1) return false;
        if (player.isOnGround()) return false;
        if (player.isClimbing()) return false;
        if (player.isInWater()) return false;
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) return false;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return false;
        if (player.isInsideVehicle()) return false;
        if (player.isGliding() || player.isFlying()) return false;
        return true;
    }

    private static boolean isHostileMob(Mob mob) {
        switch (mob.getType()) {
        case GHAST:
        case SLIME:
        case PHANTOM:
        case MAGMA_CUBE:
        case ENDER_DRAGON:
        case SHULKER:
        case SHULKER_BULLET:
        case HOGLIN:
            return true;
        case IRON_GOLEM:
            return false;
        default:
            if (mob instanceof Tameable) return false;
            if (mob instanceof AbstractVillager) return false;
            return mob instanceof Monster
                || mob instanceof Boss;
        }
    }

    private Mob getLookAtEntity(Player player) {
        final double range = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).getValue();
        final Location playerLocation = player.getEyeLocation();
        final Vector playerDirection = playerLocation.getDirection();
        double minAngle = 0.0;
        Mob result = null;
        for (Entity nearby : player.getNearbyEntities(range, range, range)) {
            if (!(nearby instanceof Mob mob)) continue;
            if (!isHostileMob(mob)) continue;
            if (!PlayerEntityAbilityQuery.Action.DAMAGE.query(player, mob)) continue;
            if (!player.hasLineOfSight(mob)) continue;
            final Vector mobDirection = mob.getEyeLocation().subtract(playerLocation).toVector();
            final double mobDistance = mobDirection.length();
            if (mobDistance > range) continue;
            if (mobDistance < 0.01) return mob;
            final double mobAngle = mobDirection.angle(playerDirection);
            if (result == null || mobAngle < minAngle) {
                result = mob;
                minAngle = mobAngle;
            }
        }
        if (minAngle > Math.PI * 0.35) return null;
        if (result != null && isDebugTalent(player)) {
            player.sendMessage(talentType.name() + " target:" + result.getType()
                               + " angle:" + formatDouble(minAngle));
        }
        return result;
    }
}
