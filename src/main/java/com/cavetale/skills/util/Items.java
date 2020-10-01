package com.cavetale.skills.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class Items {
    private ItemStack itemStack;
    private ItemMeta itemMeta;

    private Items(final Material material) {
        itemStack = new ItemStack(material);
    }

    private Items(final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static Items of(Material material) {
        return new Items(material);
    }

    public static Items of(ItemStack itemStack) {
        return new Items(itemStack);
    }

    public ItemStack bake() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
            itemMeta = null;
        }
        return itemStack;
    }

    public ItemStack create() {
        bake();
        return itemStack.clone();
    }

    private ItemMeta meta() {
        if (itemMeta == null) {
            itemMeta = itemStack.getItemMeta();
        }
        return itemMeta;
    }

    public String name() {
        if (!meta().hasDisplayName()) return "";
        String name = meta().getDisplayName();
        return name != null ? name : "";
    }

    public Items name(String name) {
        meta().setDisplayName(name);
        return this;
    }

    public List<String> lore() {
        if (!meta().hasLore()) return new ArrayList<>();
        List<String> lore = meta().getLore();
        return lore != null
            ? lore
            : new ArrayList<>();
    }

    public List<String> tooltip() {
        List<String> result = new ArrayList<>();
        if (meta().hasDisplayName()) result.add(name());
        result.addAll(lore());
        return result;
    }

    public Items tooltip(List<String> in) {
        name(!in.isEmpty() ? in.get(0) : "");
        lore(in.size() > 1 ? in.subList(1, in.size()) : Collections.emptyList());
        return this;
    }

    public Items tooltip(String... in) {
        name(in.length > 0 ? in[0] : "");
        if (in.length > 1) {
            lore(Arrays.copyOfRange(in, 1, in.length));
        } else {
            lore(Collections.emptyList());
        }
        return this;
    }

    public Items lore(String... lore) {
        meta().setLore(Arrays.asList(lore));
        return this;
    }

    public Items lore(List<String> lore) {
        meta().setLore(lore);
        return this;
    }

    public Items appendLore(String line) {
        List<String> lore = meta().getLore();
        if (lore == null) lore = new ArrayList<>(1);
        lore.add(line);
        meta().setLore(lore);
        return this;
    }

    public Items appendLore(List<String> lines) {
        List<String> lore = meta().getLore();
        if (lore == null) lore = new ArrayList<>(lines.size());
        lore.addAll(lines);
        meta().setLore(lore);
        return this;
    }

    public Items hide() {
        meta().addItemFlags(ItemFlag.values());
        return this;
    }

    public Items glow() {
        meta().addEnchant(Enchantment.DURABILITY, 1, true);
        return this;
    }

    public Items amount(int amt) {
        amt = Math.max(amt, 1);
        amt = Math.min(amt, itemStack.getType().getMaxStackSize());
        itemStack.setAmount(amt);
        return this;
    }

    public Items applyTooltip(Function<String, String> fun) {
        name(fun.apply(name()));
        lore(lore().stream().map(fun).collect(Collectors.toList()));
        return this;
    }

    public Items playerProfile(PlayerProfile profile) {
        if (!(meta() instanceof SkullMeta)) return this;
        SkullMeta skull = (SkullMeta) meta();
        skull.setPlayerProfile(profile);
        return this;
    }
}
