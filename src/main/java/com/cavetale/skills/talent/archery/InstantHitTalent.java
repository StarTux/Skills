package com.cavetale.skills.talent.archery;

import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InstantHitTalent extends Talent {
    public InstantHitTalent() {
        super(TalentType.INSTANT_HIT, "Instant Hit",
              "Fully charged :bow:bow arrows reset any mob's :clock:invulnerability ticks",
              "Mobs are invulnerable to all attack damage for half a second after they take damage. This talent resets their invulnerability when you hit them with a fully charged :bow:bow arrow."
              + "\nThat way, mobs cannot deflect your arrows without a shield.");
        addLevel(3, "Always hit the enemy");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.MUSIC_DISC_CAT);
    }

    public void onArrowCollide(Player player, AbstractArrow arrow, LivingEntity target) {
        if (!isPlayerEnabled(player)) return;
        if (!arrow.isCritical()) return;
        if (arrow.getWeapon() == null || arrow.getWeapon().getType() != Material.BOW) return;
        target.setNoDamageTicks(0);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " ticks:" + target.getNoDamageTicks());
        }
    }
}
