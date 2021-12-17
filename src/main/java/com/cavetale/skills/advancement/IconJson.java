package com.cavetale.skills.advancement;

import com.cavetale.core.util.Json;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Data
final class IconJson {
    protected String item;
    protected String nbt;

    public void set(Material material) {
        this.item = material.getKey().toString();
        this.nbt = null;
    }

    public void set(ItemStack itemStack) {
        this.item = itemStack.getType().getKey().toString();
        NbtTag nbtTag = new NbtTag();
        itemStack.editMeta(meta -> {
                if (meta.hasCustomModelData()) {
                    nbtTag.CustomModelData = meta.getCustomModelData();
                }
            });
        this.nbt = Json.serialize(nbtTag);
    }

    @SuppressWarnings("MemberName")
    protected static final class NbtTag {
        protected Integer CustomModelData;
    }
}
