package com.cavetale.skills.crafting;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.session.Session;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
        final ItemStack first = event.getInventory().getFirstItem();
        if (first == null || first.getType().isAir() || Mytems.forItem(first) != null) return;
        final ItemStack second = event.getInventory().getSecondItem();
        if (second == null || second.getType().isAir() || Mytems.forItem(second) != null) return;
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        if (first.getType() == Material.ENCHANTED_BOOK && second.getType() != Material.ENCHANTED_BOOK) return;
        if (!playMode(player)) return;
        Session session = sessionOf(player);
        if (session == null || !session.isEnabled()) return;
        Map<Enchantment, AnvilEnchantment> anvilEnchantmentMap = new HashMap<>();
        RECIPES: for (AnvilEnchantment it : session.getAnvilEnchantments()) {
            if (it.item != first.getType()) continue;
            for (Enchantment conflict : it.conflicts) {
                if (getEnchantmentLevel(first, conflict) != 0) {
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
        Map<Enchantment, Integer> enchantments = getEnchantments(second);
        int addedLevels = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            AnvilEnchantment recipe = anvilEnchantmentMap.get(enchantment);
            if (recipe == null) continue;
            int level = entry.getValue();
            int firstLevel = getEnchantmentLevel(first, enchantment);
            int vanillaLevel = getEnchantmentLevel(result, enchantment);
            if (firstLevel < vanillaLevel) {
                if (session.isDebugMode()) {
                    player.sendMessage(event.getEventName() + " Handled by Vanilla: " + recipe
                                       + " " + firstLevel + "<" + vanillaLevel);
                }
                // Handled by vanilla enchants
                continue;
            } else if (firstLevel < level) {
                if (session.isDebugMode()) {
                    player.sendMessage(event.getEventName() + " New or replaced enchantment: " + recipe);
                }
                // Add
                setEnchantmentLevel(result, enchantment, level);
                addedLevels += level;
            } else if (level == firstLevel && level < recipe.maxLevel) {
                if (session.isDebugMode()) {
                    player.sendMessage(event.getEventName() + " Increase level: " + recipe);
                }
                // Increase level
                setEnchantmentLevel(result, enchantment, level + 1);
                addedLevels += level + 1;
            } else {
                if (session.isDebugMode()) {
                    player.sendMessage(event.getEventName() + " None of the above: " + recipe);
                }
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

    private static int getEnchantmentLevel(ItemStack item, Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        return meta instanceof EnchantmentStorageMeta storage
            ? storage.getStoredEnchantLevel(enchantment)
            : meta.getEnchantLevel(enchantment);
    }

    private static Map<Enchantment, Integer> getEnchantments(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta instanceof EnchantmentStorageMeta storage
            ? storage.getStoredEnchants()
            : meta.getEnchants();
    }

    private static void setEnchantmentLevel(ItemStack item, Enchantment enchantment, int level) {
        item.editMeta(meta -> {
                // ignoreLevelRestriction: true
                if (meta instanceof EnchantmentStorageMeta storage) {
                    storage.addStoredEnchant(enchantment, level, true);
                } else {
                    meta.addEnchant(enchantment, level, true);
                }
            });
    }
}
