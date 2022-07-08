package com.cavetale.skills.skill.mining;

import com.cavetale.mytems.Mytems;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class RubyTalent extends Talent {
    public RubyTalent() {
        super(TalentType.RUBY);
    }

    @Override
    public String getDisplayName() {
        return "Rubies";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Mining diamond veins will sometimes drop diamonds",
                       "The chance depends on the size of the vein:"
                       + " Each :diamond_ore:block adds 5% to the total drop chance.");
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
        originalBlock.getWorld().dropItem(originalBlock.getLocation().add(0.5, 0.5, 0.5),
                                          Mytems.RUBY.createItemStack());
        return true;
    }
}
