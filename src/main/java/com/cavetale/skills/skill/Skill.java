package com.cavetale.skills.skill;

import com.cavetale.mytems.MytemsPlugin;
import com.cavetale.mytems.item.coin.Denomination;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.worldmarker.util.Tags;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Skill {
    public final SkillsPlugin plugin;
    @Getter protected final SkillType skillType;

    protected Skill(final SkillsPlugin plugin, final SkillType skillType) {
        this.plugin = plugin;
        this.skillType = skillType;
        skillType.register(this);
    }

    protected abstract void enable();

    protected final void giveExpBonus(Player player, Session session, int base) {
        int bonus = base + session.getExpBonus(skillType);
        if (bonus > 0) player.giveExp(bonus);
    }

    protected final void giveExpBonus(Player player, Session session) {
        giveExpBonus(player, session, 0);
    }

    protected final boolean dropMoney(Location location, double money) {
        Denomination[] denos = Denomination.values();
        double total = 0.0;
        for (Denomination deno : denos) total += deno.value;
        total /= (double) denos.length;
        double chance = money / total;
        double roll = plugin.random.nextDouble();
        if (roll >= chance) return false;
        Denomination deno = denos[plugin.random.nextInt(denos.length)];
        ItemStack itemStack = deno.mytems.createItemStack();
        Item item = location.getWorld().dropItem(location, itemStack);
        if (item == null) return false;
        item.getItemStack().editMeta(meta -> {
                Tags.set(meta.getPersistentDataContainer(), MytemsPlugin.namespacedKey("message"), skillType.displayName + " Skill");
            });
        return true;
    }
}
