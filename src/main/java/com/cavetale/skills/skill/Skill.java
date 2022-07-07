package com.cavetale.skills.skill;

import com.cavetale.mytems.MytemsPlugin;
import com.cavetale.mytems.item.coin.Denomination;
import com.cavetale.skills.session.Session;
import com.cavetale.worldmarker.util.Tags;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.random;

public abstract class Skill {
    @Getter protected final SkillType skillType;

    protected Skill(final SkillType skillType) {
        this.skillType = skillType;
        skillType.register(this);
    }

    protected abstract void enable();

    protected final void giveExpBonus(Player player, Session session, int base) {
        int bonus = base + session.getExpBonus(skillType);
        if (bonus > 0) player.giveExp(bonus, true);
    }

    protected final void giveExpBonus(Player player, Session session) {
        giveExpBonus(player, session, 0);
    }

    protected final boolean dropMoney(Player player, Location location, double money) {
        final Denomination deno = Denomination.GOLD;
        final double chance = money / deno.value;
        final double roll = random().nextDouble();
        if (roll >= chance) return false;
        final ItemStack itemStack = deno.mytems.createItemStack();
        final Item item = location.getWorld().dropItem(location, itemStack, drop -> {
                drop.setCanMobPickup(false);
                drop.setOwner(player.getUniqueId());
                drop.setPickupDelay(0);
                drop.setInvulnerable(true);
            });
        if (item == null) return false;
        item.getItemStack().editMeta(meta -> {
                Tags.set(meta.getPersistentDataContainer(), MytemsPlugin.namespacedKey("message"), skillType.displayName + " Skill");
            });
        player.playSound(item.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 0.75f);
        return true;
    }
}
