package com.cavetale.skills.skill.archery;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.crafting.AnvilEnchantment;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class CrossbowPierceTalent extends Talent {
    public CrossbowPierceTalent() {
        super(TalentType.XBOW_PIERCE, "Multishot Piercing",
              "Piercing and Multishot work together",
              "You can put both the Multishot and the Piercing Enchantments on the same :crossbow:crossbow via anvil. Both will work as usual.");
        addLevel(4, "REMOVE");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.WOODEN_CROSSBOW);
    }

    @Override
    public List<AnvilEnchantment> getAnvilEnchantments(Session session) {
        return List.of(new AnvilEnchantment(Material.CROSSBOW, Enchantment.PIERCING),
                       (session.isTalentEnabled(TalentType.XBOW_VOLLEY)
                        ? new AnvilEnchantment(Material.CROSSBOW, Enchantment.MULTISHOT, 3)
                        : new AnvilEnchantment(Material.CROSSBOW, Enchantment.MULTISHOT)));
    }
}
