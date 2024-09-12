package com.cavetale.skills.talent.combat;

import com.cavetale.skills.skill.combat.MobStatusEffect;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentType;
import java.time.Duration;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

public final class StunPikeTalent extends Talent {
    public StunPikeTalent() {
        super(TalentType.STUN_PIKE, "Stun Pike",
              "Monsters struck by thrown Channeling :trident:tridents are stunned for a while.");
        addLevel(1, "+" + levelToSeconds(1) + " seconds");
        addLevel(1, "+" + levelToSeconds(2) + " seconds");
        addLevel(1, "+" + levelToSeconds(3) + " seconds");
        addLevel(1, "+" + levelToSeconds(4) + " seconds");
        addLevel(1, "+" + levelToSeconds(5) + " seconds");
    }

    private static int levelToSeconds(int level) {
        return 2 + level;
    }

    @Override
    public ItemStack createIcon() {
        return createIcon(Material.TRIDENT);
    }

    public void onPlayerHitMobWithTrident(Player player, Trident trident, Mob mob) {
        if (!isPlayerEnabled(player)) return;
        if (trident.getItemStack().getEnchantmentLevel(Enchantment.CHANNELING) < 1) return;
        if (BossMob.isBossMob(mob)) return;
        if (!mob.isAware()) return;
        final int level = getTalentLevel(player);
        if (level < 1) return;
        final int seconds = levelToSeconds(level);
        MobStatusEffect.FREEZE.tick(mob, Duration.ofSeconds(seconds));
        mob.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, mob.getEyeLocation(), seconds * 20, 0.5, 0.5, 0.5, 0.125);
        if (isDebugTalent(player)) {
            player.sendMessage(talentType + " lvl:" + level + " seconds:" + seconds);
        }
    }
}
