package com.cavetale.skills.skill.archery;

import com.cavetale.skills.crafting.AnvilEnchantment;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import static com.cavetale.skills.SkillsPlugin.archerySkill;
import static com.cavetale.skills.SkillsPlugin.random;
import static com.cavetale.skills.SkillsPlugin.sessionOf;

public final class CrossbowVolleyTalent extends Talent {
    public CrossbowVolleyTalent() {
        super(TalentType.XBOW_VOLLEY);
    }

    @Override
    public String getDisplayName() {
        return "Volley";
    }

    @Override
    public List<String> getRawDescription() {
        return List.of("Multishot releases a volley of arrows",
                       "Instead of 3 arrows flat, Multishot fires a barrage of"
                       + " :arrow:arrows where you're looking, based on the level."
                       + " An arrow must be loaded in the :crossbow:crossbow.",
                       "Use :enchanted_book:Enchanted Books to create higher levels of Multishot."
                       + " You can either combine books or add books to your"
                       + " :crossbow:crossbow on an anvil.",
                       "Arrows are multiplied based on your Multishot level:\n"
                       + "\n:crossbow: I :arrow_right: :arrow:x3"
                       + "\n:crossbow: II :arrow_right: :arrow:x10"
                       + "\n:crossbow: III :arrow_right: :arrow:x20");
    }

    @Override
    public ItemStack createIcon() {
        ItemStack icon = new ItemStack(Material.CROSSBOW);
        icon.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.values());
                ((CrossbowMeta) meta).setChargedProjectiles(List.of(new ItemStack(Material.FIREWORK_ROCKET)));
            });
        return icon;
    }

    protected void onShootCrossbow(Player player, ItemStack crossbow, AbstractArrow arrow) {
        if (!isPlayerEnabled(player)) return;
        final int multishot = crossbow.getEnchantmentLevel(Enchantment.MULTISHOT);
        if (multishot == 0) return;
        List<ItemStack> arrows = ((CrossbowMeta) crossbow.getItemMeta()).getChargedProjectiles();
        if (arrows.isEmpty() || !Tag.ITEMS_ARROWS.isTagged(arrows.get(0).getType())) return;
        final int arrowCount = switch (multishot) {
        case 0 -> 0;
        case 1 -> 3;
        case 2 -> 10;
        case 3 -> 20;
        default -> 0;
        };
        final double velocity = arrow.getVelocity().length();
        int count;
        for (count = 0; count < arrowCount - 1; count += 1) {
            Location location = player.getLocation();
            float yaw = location.getYaw() + (float) ((random().nextDouble() * (random().nextBoolean() ? 1.0 : -1.0)) * 45.0);
            float pitch = location.getPitch() + (float) ((random().nextDouble() * (random().nextBoolean() ? 1.0 : -1.0)) * 18.0);
            location.setYaw(yaw);
            location.setPitch(Math.max(-90.0f, Math.min(90.0f, pitch)));
            Arrow spam = player.launchProjectile(Arrow.class, location.getDirection().multiply(velocity));
            if (spam == null) break;
            spam.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            spam.setShotFromCrossbow(true);
            spam.setCritical(true);
            spam.setPierceLevel(arrow.getPierceLevel());
            spam.setFireTicks(arrow.getFireTicks());
            ArcherySkill.setSpawmArrow(spam);
            archerySkill().onShootCrossbow(player, spam);
        }
        if (sessionOf(player).isDebugMode()) {
            player.sendMessage(talentType + " multi:" + multishot + " arrows:" + count + "/" + arrowCount);
        }
    }

    @Override
    public List<AnvilEnchantment> getAnvilEnchantments(Session session) {
        return List.of(new AnvilEnchantment(Material.CROSSBOW, Enchantment.MULTISHOT, 3),
                       new AnvilEnchantment(Material.ENCHANTED_BOOK, Enchantment.MULTISHOT, 3));
    }
}
