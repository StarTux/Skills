package com.cavetale.skills.skill.archery;

import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class TippedInfinityTalent extends Talent {
    public TippedInfinityTalent() {
        super(TalentType.TIPPED_INFINITY, "Tipped Infinity",
              "Infinity sometimes works on tipped arrows",
              "When you shoot a :tipped_arrow:tipped arrow with an Infinity :bow:bow, you keep the :tipped_arrow:arrow 66% of the time.",
              "This also works on :crossbow:crossbows, provided your have the Crossbow Infinity talent.");
        addLevel(2, "Works 2/3 of the time");
    }

    @Override
    public ItemStack createIcon() {
        ItemStack icon = new ItemStack(Material.TIPPED_ARROW);
        icon.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.values());
                if (meta instanceof PotionMeta potionMeta) {
                    potionMeta.setColor(Color.RED);
                }
            });
        return icon;
    }

    protected void onShootBow(Player player, ItemStack bow, ItemStack consumable, AbstractArrow arrow, EntityShootBowEvent event) {
        if (!isPlayerEnabled(player)) return;
        if (bow.getEnchantmentLevel(Enchantment.INFINITY) == 0) return;
        if (consumable.getType() != Material.TIPPED_ARROW) return;
        if (!roll()) return;
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        // TODO BROKEN in API
        event.setConsumeItem(false);
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> player.updateInventory());
    }

    protected static boolean roll() {
        return random().nextDouble() < 0.66;
    }
}
