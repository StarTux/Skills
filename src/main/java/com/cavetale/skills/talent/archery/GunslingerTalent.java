package com.cavetale.skills.talent.archery;

import com.cavetale.skills.skill.archery.ArrowType;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;

public final class GunslingerTalent extends Talent implements Listener {
    public GunslingerTalent() {
        super(TalentType.GUNSLINGER, "Gunslinger",
              "Wield a second crossbow in your off-hand for dual wielding effects",
              "When you reload the :crossbow:crossbow in your main hand, the one in your off-hand will also reload.",
              "Shooting the :crossbow:crossbow in your main hand will quickly switch with the :crossbow:crossbow in your off-hand");
        addLevel(2, "Fast Reload");
        addLevel(2, "Fast Shooting");
    }

    @Override
    public ItemStack createIcon() {
        ItemStack icon = createIcon(Material.CROSSBOW);
        icon.editMeta(m -> m.setMaxStackSize(2));
        icon.setAmount(2);
        return icon;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityLoadCrossbow(EntityLoadCrossbowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!isPlayerEnabled(player)) return;
        final int level = getTalentLevel(player);
        if (level < 1) return;
        final ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand == null || offhand.getType() != Material.CROSSBOW) return;
        if (!(offhand.getItemMeta() instanceof CrossbowMeta meta)) return;
        if (!meta.getChargedProjectiles().isEmpty()) return;
        ItemStack consumable = null;
        for (int i = 0; i < player.getInventory().getSize(); i += 1) {
            ItemStack it = player.getInventory().getItem(i);
            if (it != null && Tag.ITEMS_ARROWS.isTagged(it.getType())) {
                consumable = it;
                break;
            }
        }
        if (consumable == null) return;
        final int multishot = meta.getEnchantLevel(Enchantment.MULTISHOT);
        final boolean infinity = offhand.getEnchantmentLevel(Enchantment.INFINITY) > 0;
        final ItemStack chargedItem = consumable.asOne();
        ArrowType.NO_PICKUP.set(consumable);
        meta.setChargedProjectiles(multishot > 0
                                   ? List.of(chargedItem, chargedItem, chargedItem)
                                   : List.of(chargedItem));
        offhand.setItemMeta(meta);
        if (!infinity || consumable.getType() != Material.ARROW) {
            consumable.subtract(1);
        }
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " " + event.getEventName() + " lvl:" + level);
        }
    }

    public void onShootCrossbow(Player player) {
        if (!isPlayerEnabled(player)) return;
        final int level = getTalentLevel(player);
        if (level < 2) return;
        // Main Hand
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.CROSSBOW) return;
        // Off Hand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand == null || offhand.getType() != Material.CROSSBOW) return;
        Bukkit.getScheduler().runTask(skillsPlugin(), () -> {
                if (!player.isOnline()) return;
                ItemStack tmp = player.getInventory().getItemInOffHand();
                player.getInventory().setItemInOffHand(player.getInventory().getItemInMainHand());
                player.getInventory().setItemInMainHand(tmp);
            });
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + "lvl:" + level + " shoot");
        }
    }
}
