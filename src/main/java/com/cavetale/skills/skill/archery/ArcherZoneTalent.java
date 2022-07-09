package com.cavetale.skills.skill.archery;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class ArcherZoneTalent extends Talent {
    protected ArcherZoneTalent() {
        super(TalentType.ARCHER_ZONE);
    }

    @Override
    public String getDisplayName() {
        return "In The Zone";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Charged arrow hits increase arrow damage",
                       "Any fully charged arrow hitting a hostile mob will add 1 to the base damage."
                       + " Hit a block or shoot without full charge to reset the damage bonus.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.TARGET);
    }

    protected void onArrowDamage(Player player, AbstractArrow arrow, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if (combatReward(mob) == null) return;
        if (!ArcherySkill.isPrimaryArrow(arrow)) return;
        if (arrow.isCritical()) {
            increaseZone(player);
        } else {
            resetZone(player);
        }
    }

    protected void onArrowHitBlock(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (!ArcherySkill.isPrimaryArrow(arrow)) return;
        resetZone(player);
    }

    protected void onShootArrow(Player player, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        Session session = sessionOf(player);
        int zone = session.archery.getArcherZone();
        if (zone == 0) return;
        arrow.setDamage(arrow.getDamage() + (double) zone);
        if (session.isDebugMode()) player.sendMessage(talentType + " +" + zone);
    }

    protected void increaseZone(Player player) {
        Session session = sessionOf(player);
        int zone = session.archery.getArcherZone() + 1;
        session.archery.setArcherZone(zone);
        player.sendActionBar(join(separator(space()),
                                  text(zone, talentType.skillType.textColor, BOLD),
                                  text("In The Zone", talentType.skillType.textColor)));
    }

    protected void resetZone(Player player) {
        Session session = sessionOf(player);
        if (session.archery.getArcherZone() == 0) return;
        session.archery.setArcherZone(0);
        if (isPlayerEnabled(player)) {
            player.sendActionBar(text("In The Zone", DARK_GRAY, STRIKETHROUGH));
        }
    }
}
