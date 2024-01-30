package com.cavetale.skills.skill;

import com.cavetale.mytems.MytemsPlugin;
import com.cavetale.mytems.item.coin.Denomination;
import com.cavetale.worldmarker.util.Tags;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.util.Players.playMode;

public abstract class Skill {
    @Getter protected final SkillType skillType;

    protected Skill(final SkillType skillType) {
        this.skillType = skillType;
        skillType.register(this);
    }

    protected abstract void enable();

    protected final boolean dropMoney(Player player, Location location, double money) {
        if (money < 0.01) return false;
        final Denomination deno;
        if (money <= Denomination.GOLD.value) {
            deno = Denomination.GOLD; // 1000
        } else if (money <= Denomination.DIAMOND.value) {
            deno = Denomination.DIAMOND; // 10,000
        } else {
            deno = Denomination.RUBY; // 100,000
        }
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

    protected final boolean isPlayerEnabled(Player player) {
        return playMode(player);
    }
}
