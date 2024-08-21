package com.cavetale.skills.skill.archery;

import com.cavetale.skills.crafting.AnvilEnchantment;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class CrossbowFlameTalent extends Talent {
    public CrossbowFlameTalent() {
        super(TalentType.XBOW_FLAME, "Crossfire",
              "The Flame Enchantment works on crossbows",
              "Use an anvil to Put the Flame Enchantment on a crossbow via :enchanted_book:Enchanted Book. Regular :arrow:arrows will burn when launched.");
        addLevel(3, "REMOVE");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.FIRE_CHARGE);
    }

    protected void onShootCrossbow(Player player, ItemStack crossbow, AbstractArrow arrow, ItemStack arrowItem) {
        if (!isPlayerEnabled(player)) return;
        final int flame = crossbow.getEnchantmentLevel(Enchantment.FLAME);
        if (flame == 0) return;
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " " + flame);
        }
        if (arrowItem == null || arrowItem.getType() != Material.ARROW) return;
        arrow.setFireTicks(2000);
    }

    @Override
    public List<AnvilEnchantment> getAnvilEnchantments(Session session) {
        return List.of(new AnvilEnchantment(Material.CROSSBOW, Enchantment.FLAME));
    }
}
