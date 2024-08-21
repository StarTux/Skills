package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class SpectralInfinityTalent extends Talent {
    public SpectralInfinityTalent() {
        super(TalentType.SPECTRAL_INFINITY, "Spectral Infinity",
              "Infinity sometimes works on spectral arrows",
              "When you shoot a :spectral_arrow:spectral arrow with an Infinity :bow:bow, you keep the :spectral_arrow:arrow 50% of the time.",
              "This also works on :crossbow:crossbows, provided your have the Crossbow Infinity talent.");
        addLevel(2, "Works 2/3 of the time");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.SPECTRAL_ARROW);
    }

    protected void onShootBow(Player player, ItemStack bow, ItemStack consumable, AbstractArrow arrow, EntityShootBowEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (bow.getEnchantmentLevel(Enchantment.INFINITY) == 0) return;
        if (consumable.getType() != Material.SPECTRAL_ARROW) return;
        if (!roll()) return;
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        // TODO BROKEN in API
        event.setConsumeItem(false);
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> player.updateInventory());
    }

    protected static boolean roll() {
        return random().nextBoolean();
    }
}
