package com.cavetale.skills.skill.mining;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class RubyTalent extends Talent {
    public RubyTalent() {
        super(TalentType.RUBY, "Rubies",
              "Mining diamond veins will sometimes drop a ruby",
              "The chance depends on the size of the vein: Each :diamond_ore:block adds 5% to the total drop chance.");
        addLevel(2, "REMOVE");
        addLevel(2, "REMOVE EVEN MORE");
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Mytems.RUBY);
    }

    protected boolean onVeinMine(Player player, Block originalBlock, MiningReward reward, int rewardableVeinBlocks) {
        if (!isPlayerEnabled(player)) return false;
        if (!Tag.DIAMOND_ORES.isTagged(reward.material)) {
            return false;
        }
        double chance = (double) rewardableVeinBlocks / 20.0;
        double roll = random().nextDouble();
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " " + roll + "/" + chance);
        }
        if (roll >= chance) return false;
        final Location dropLocation = sessionOf(player).isTalentEnabled(TalentType.MINE_MAGNET)
            ? player.getLocation()
            : originalBlock.getLocation().add(0.5, 0.5, 0.5);
        originalBlock.getWorld().dropItem(dropLocation, Mytems.RUBY.createItemStack());
        return true;
    }
}
