package com.cavetale.skills.talent.combat;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import java.util.List;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class CombatMagnetTalent extends Talent {
    public CombatMagnetTalent() {
        super(TalentType.COMBAT_MAGNET, "Combat Magnet",
              "Drops from mobs killed in combat are warped to you",
              "When you kill a mob in combat, its drops and exp will land at your feet for you to collect");
        addLevel(1, "Pick up mob drops");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.MAGNET);
    }

    public void onPlayerMobMeleeKill(Player player, EntityDeathEvent event) {
        if (!isPlayerEnabled(player)) return;
        final List<ItemStack> drops = List.copyOf(event.getDrops());
        event.getDrops().clear();
        for (ItemStack drop : drops) {
            final Item item = player.getWorld().dropItem(player.getLocation(), drop);
            item.setPickupDelay(0);
            item.setOwner(player.getUniqueId());
        }
        if (isDebugTalent(player)) {
            player.sendMessage(talentType.name());
        }
    }
}
