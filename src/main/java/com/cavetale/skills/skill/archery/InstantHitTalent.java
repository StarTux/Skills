package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class InstantHitTalent extends Talent {
    public InstantHitTalent() {
        super(TalentType.INSTANT_HIT);
    }

    @Override
    public String getDisplayName() {
        return "Instant Hit";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Fully charged arrows reset any mob's invulnerability ticks",
                       "Mobs are invulnerable to all attack damage for a few ticks after they take damage."
                       + " This talent resets their invulnerability when you hit them"
                       + " with a fully charged :arrow:arrow."
                       + "\n\nThat way, mobs cannot deflect your arrows without a shield.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.MUSIC_DISC_OTHERSIDE);
    }

    protected void onArrowCollide(Player player, AbstractArrow arrow, LivingEntity target) {
        if (!isPlayerEnabled(player)) return;
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType
                               + " crit:" + arrow.isCritical()
                               + " ticks:" + target.getNoDamageTicks());
        }
        if (!arrow.isCritical()) return;
        target.setNoDamageTicks(0);
    }
}
