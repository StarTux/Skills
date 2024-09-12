package com.cavetale.skills.talent.combat;

import com.cavetale.core.event.entity.PlayerEntityAbilityQuery;
import com.cavetale.mytems.Mytems;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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
        if (!MeleeWeapon.hasMeleeWeapon(player)) return;
        if (player.getAttackCooldown() < 1f) return;
        if (!isPlayerEnabled(player)) return;
        final Mob target = getLookAtEntity(player);
        if (target == null) return;
        final double baseDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        target.damage(baseDamage, DamageSource.builder(DamageType.PLAYER_ATTACK)
                      .withCausingEntity(player)
                      .withDirectEntity(player)
                      .withDamageLocation(target.getEyeLocation())
                      .build());
        if (isDebugTalent(player)) {
            player.sendMessage(talentType.name() + " target:" + target.getType()
                               + " dmg:" + formatDouble(baseDamage));
        }
    }

    private Mob getLookAtEntity(Player player) {
        final double range = player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).getValue();
        final Location playerLocation = player.getEyeLocation();
        final Vector playerDirection = playerLocation.getDirection();
        double minAngle = 0.0;
        Mob result = null;
        for (Entity nearby : player.getNearbyEntities(range, range, range)) {
            if (!(nearby instanceof Mob mob)) continue;
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
