package com.cavetale.skills.crafting;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.Repairable;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.util.Players.playMode;

public final class CraftingListener implements Listener {
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, skillsPlugin());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPrepareAnvil(PrepareAnvilEvent event) {
        if (event.getInventory().getRenameText() != null && !event.getInventory().getRenameText().isEmpty()) {
            return;
        }
        final ItemStack first = event.getInventory().getFirstItem();
        if (first == null || first.getType().isAir() || Mytems.forItem(first) != null) return;
        final ItemStack second = event.getInventory().getSecondItem();
        if (second == null || second.getType().isAir() || Mytems.forItem(second) != null) return;
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        if (!playMode(player)) return;
        Session session = sessionOf(player);
        if (session == null || !session.isEnabled()) return;
        Map<Enchantment, AnvilEnchantment> anvilEnchantmentMap = new HashMap<>();
        RECIPES: for (AnvilEnchantment it : session.getAnvilEnchantments()) {
            if (it.item != first.getType()) continue;
            for (Enchantment conflict : it.conflicts) {
                if (first.getEnchantmentLevel(conflict) != 0) {
                    continue RECIPES;
                }
            }
            anvilEnchantmentMap.put(it.enchantment, it);
        }
        if (anvilEnchantmentMap.isEmpty()) {
            return;
        }
        ItemStack result = event.getResult();
        if (result == null) {
            result = first.clone();
        }
        Map<Enchantment, Integer> enchantments = ((EnchantmentStorageMeta) second.getItemMeta()).getStoredEnchants();
        int addedLevels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!anvilEnchantmentMap.containsKey(enchantment)) continue;
            int level = entry.getValue();
            int firstLevel = first.getEnchantmentLevel(enchantment);
            int vanillaLevel = result.getEnchantmentLevel(enchantment);
            if (firstLevel != vanillaLevel) {
                // Handled by vanilla enchants
                continue;
            } else if (firstLevel == 0) {
                // Add
                result.addUnsafeEnchantment(enchantment, level);
                addedLevels += level;
            } else if (level == firstLevel && level < enchantment.getMaxLevel()) {
                // Increase level
                result.addUnsafeEnchantment(enchantment, level + 1);
                addedLevels += level + 1;
            } else {
                continue;
            }
        }
        if (addedLevels == 0) {
            return;
        }
        int baseCostA = ((Repairable) first.getItemMeta()).getRepairCost();
        int baseCostB = ((Repairable) second.getItemMeta()).getRepairCost();
        int baseRepairCost = Math.max(baseCostA, baseCostB);
        result.editMeta(meta -> ((Repairable) meta).setRepairCost(baseRepairCost + 1));
        event.setResult(result);
        event.getInventory().setRepairCost(baseRepairCost + addedLevels);
    }
}
