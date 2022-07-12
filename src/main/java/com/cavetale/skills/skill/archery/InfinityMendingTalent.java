package com.cavetale.skills.skill.archery;

import com.cavetale.skills.crafting.AnvilEnchantment;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class InfinityMendingTalent extends Talent {
    InfinityMendingTalent() {
        super(TalentType.INFINITY_MENDING);
    }

    @Override
    public String getDisplayName() {
        return "Infinity Mending";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Put Mending on a bow with Infinity",
                       "With this talent, you can combine a :bow:bow"
                       + " which already has Infinity with an"
                       + " :enchanted_book:enchanted book on an"
                       + " anvil to add Mending."
                       + "\n\nThis also works with :crossbow:crossbows.");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.ENCHANTED_BOOK);
    }

    @Override
    public List<AnvilEnchantment> getAnvilEnchantments(Session session) {
        return List.of(new AnvilEnchantment(Material.BOW, Enchantment.MENDING),
                       new AnvilEnchantment(Material.BOW, Enchantment.ARROW_INFINITE),
                       new AnvilEnchantment(Material.CROSSBOW, Enchantment.MENDING));
    }
};
