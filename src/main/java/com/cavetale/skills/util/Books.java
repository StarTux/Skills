package com.cavetale.skills.util;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import static net.kyori.adventure.text.Component.text;

public final class Books {
    private Books() { }

    public static ItemStack make(List<Component> pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.editMeta(m -> {
                if (m instanceof BookMeta meta) {
                    meta.author(text("Cavetale"));
                    meta.title(text("Skills"));
                    meta.pages(pages);
                }
            });
        return book;
    }

    public static void open(Player player, List<Component> pages) {
        player.closeInventory();
        player.openBook(make(pages));
    }
}
