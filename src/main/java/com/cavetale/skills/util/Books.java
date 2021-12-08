package com.cavetale.skills.util;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class Books {
    private Books() { }

    public static ItemStack make(List<Component> pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.editMeta(m -> {
                if (m instanceof BookMeta meta) {
                    meta.setAuthor("Cavetale");
                    meta.title(Component.empty());
                    meta.pages(pages);
                }
            });
        return book;
    }

    public static void open(Player player, List<Component> pages) {
        player.openBook(make(pages));
    }
}
