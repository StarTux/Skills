package com.cavetale.skills.skill.archery;

import com.cavetale.skills.crafting.AnvilEnchantment;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

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
            ? List.of(new AnvilEnchantment(Material.CROSSBOW, Enchantment.ARROW_INFINITE))
            : List.of(new AnvilEnchantment(Material.CROSSBOW, Enchantment.ARROW_INFINITE, Set.of(Enchantment.MENDING)));
    }

    protected void onShootCrossbow(Player player, ItemStack crossbow, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        if (crossbow.getEnchantmentLevel(Enchantment.ARROW_INFINITE) == 0) return;
        Session session = sessionOf(player);
        List<ItemStack> arrows = ((CrossbowMeta) crossbow.getItemMeta()).getChargedProjectiles();
        if (arrows.isEmpty() || arrows.get(0).getType() != Material.ARROW) return;
        // Only once per tick so we do not return 3 multishot arrows
        if (session.archery.isXbowInfinityLock()) return;
        session.archery.setXbowInfinityLock(true);
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> session.archery.setXbowInfinityLock(false));
        if (session.isDebugMode()) {
            player.sendMessage(talentType + " arrows:" + arrows.size());
        }
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        for (ItemStack drop : player.getInventory().addItem(arrows.get(0).clone()).values()) {
            player.getWorld().dropItem(player.getLocation(), drop).setPickupDelay(0);
        }
    }
}
