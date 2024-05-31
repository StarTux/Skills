package com.cavetale.skills.skill.archery;

import com.cavetale.skills.crafting.AnvilEnchantment;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

public final class CrossbowInfinityTalent extends Talent {
    public CrossbowInfinityTalent() {
        super(TalentType.XBOW_INFINITY);
    }

    @Override
    public String getDisplayName() {
        return "Crossbow Infinity";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Infinity works on crossbows",
                       "With this talent, you can combine a :crossbow:crossbow with an"
                       + " :enchanted_book:enchanted book on an"
                       + " anvil to enchant the crossbow with Infinity."
                       + "\n\nWhen shooting a crossbow with Infinity,"
                       + " you get the :arrow:arrow back.");
    }

    @Override
    public ItemStack createIcon() {
        ItemStack icon = new ItemStack(Material.CROSSBOW);
        icon.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.values());
                ((CrossbowMeta) meta).setChargedProjectiles(List.of(new ItemStack(Material.ARROW)));
            });
        return icon;
    }

    @Override
    public List<AnvilEnchantment> getAnvilEnchantments(Session session) {
        return session.isTalentEnabled(TalentType.INFINITY_MENDING)
            ? List.of(new AnvilEnchantment(Material.CROSSBOW, Enchantment.INFINITY))
            : List.of(new AnvilEnchantment(Material.CROSSBOW, Enchantment.INFINITY, Set.of(Enchantment.MENDING)));
    }

    /**
     * For regular arrows, this merely sets arrows to be not
     * pick-upable until the intangible_projectile API gets added.
     * Therefore, we do not check if this talent is even enabled.
     *
     * Tipped and spectral arrow talents shall remain broken for now.
     */
    protected void onShootCrossbow(Player player, ItemStack crossbow, AbstractArrow arrow, ItemStack arrowItem) {
        if (crossbow.getEnchantmentLevel(Enchantment.INFINITY) == 0) return;
        if (arrow.getPickupStatus() != AbstractArrow.PickupStatus.ALLOWED) return;
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
    }
}
