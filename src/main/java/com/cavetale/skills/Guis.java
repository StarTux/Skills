package com.cavetale.skills;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import com.cavetale.mytems.util.Text;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Effects;
import com.cavetale.skills.util.Gui;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.tiny;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

@RequiredArgsConstructor
public final class Guis {
    protected final SkillsPlugin plugin;
    protected static final int LINELENGTH = 24;

    protected void enable() {
        Gui.enable(plugin);
    }

    private static ItemStack icon(Material material, Component... lines) {
        ItemStack icon = new ItemStack(material);
        icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return Items.text(icon, List.of(lines));
    }

    private static ItemStack icon(Mytems mytems, Component... lines) {
        ItemStack icon = mytems.createIcon();
        icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return Items.text(icon, List.of(lines));
    }

    public Gui talents(Player player) {
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return null;
        final SkillType skillType = session.getTalentGui();
        final int size = 6 * 9;
        final Gui gui = new Gui(plugin).size(size);
        GuiOverlay.Builder builder = GuiOverlay.builder(size)
            .title(skillType.asComponent())
            .layer(GuiOverlay.BLANK, skillType.tag.color())
            .layer(GuiOverlay.TOP_BAR, GRAY);
        // Make top menu
        for (SkillType otherSkillType : SkillType.values()) {
            final int slot = 3 + otherSkillType.ordinal();
            if (otherSkillType == skillType) {
                builder.highlightSlot(slot, skillType.tag.color());
            }
            ItemStack icon = Items.text(otherSkillType.createIcon(),
                                        List.of(text(otherSkillType.tag.title(), otherSkillType.tag.color())));
            gui.setItem(slot, icon, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
                    session.setTalentGui(otherSkillType);
                    talents(player);
                });
        }
        final int talentPoints = session.getTalentPoints(skillType);
        if (talentPoints > 0) {
            ItemStack talentItem = icon(Material.ENDER_EYE,
                                        text(talentPoints + " "
                                             + skillType.displayName + " Talent Point"
                                             + (talentPoints > 1 ? "s" : "")));
            talentItem.setAmount(Math.min(64, talentPoints));
            gui.setItem(8, talentItem);
        }
        // Root
        gui.setItem(4 + 3 * 9, Items.text(skillType.createIcon(),
                                          List.of(text(skillType.tag.title(), skillType.tag.color()))));
        builder.highlightSlot(4 + 3 * 9, WHITE);
        // Talents
        for (TalentType talentType : TalentType.SKILL_MAP.get(skillType)) {
            final int slot = talentType.tag.x() + talentType.tag.y() * 9;
            if (session.canAccessTalent(talentType)) {
                ItemStack icon = new ItemStack(talentType.tag.icon());
                icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
                List<Component> text = new ArrayList<>();
                text.add(text(talentType.tag.title(), skillType.tag.color()));
                if (session.isTalentEnabled(talentType)) {
                    text.add(text("Enabled", GREEN));
                } else if (session.hasTalent(talentType)) {
                    text.add(text("Disabled", RED));
                } else {
                    text.add(join(noSeparators(), text(tiny("cost "), GRAY), text(talentType.talentPointCost, WHITE), text(tiny("tp"), GRAY)));
                }
                for (String line : Text.wrapLine(talentType.getTalent().getDescription(), LINELENGTH)) {
                    text.add(text(line, GRAY));
                }
                icon = Items.text(icon, text);
                gui.setItem(slot, icon, click -> {
                        if (!click.isLeftClick()) return;
                        onClickTalent(player, talentType);
                    });
            }
            if (session.isTalentEnabled(talentType)) {
                builder.highlightSlot(slot, WHITE);
            } else if (session.hasTalent(talentType)) {
                builder.highlightSlot(slot, GRAY);
            } else {
                builder.highlightSlot(slot, skillType.tag.color());
            }
        }
        gui.setItem(9, getMoneyIcon(session, skillType), click -> {
                if (!click.isLeftClick()) return;
                boolean r = session.unlockMoneyBonus(skillType, () -> {
                        talents(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 1.0f);
                    });
                if (!r) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            });
        gui.setItem(10, getExpIcon(session, skillType), click -> {
                if (!click.isLeftClick()) return;
                boolean r = session.unlockExpBonus(skillType, () -> {
                        talents(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 1.0f);
                    });
                if (!r) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            });
        gui.title(builder.build());
        gui.open(player);
        return gui;
    }

    private ItemStack getMoneyIcon(Session session, SkillType skillType) {
        int bonus = session.getMoneyBonus(skillType);
        int perc = SkillsPlugin.moneyBonusPercentage(bonus);
        int next = SkillsPlugin.moneyBonusPercentage(bonus + 1);
        ItemStack icon = Mytems.GOLDEN_COIN.createIcon();
        icon.setAmount(Math.max(1, Math.min(64, bonus)));
        Items.text(icon, List.of(join(noSeparators(), Mytems.GOLDEN_COIN, text(" Money Bonus", GOLD)),
                                 join(noSeparators(), text(tiny("current "), GRAY), text(perc, WHITE), text("%", GRAY)),
                                 join(noSeparators(), text(tiny("next "), GRAY), text(next, WHITE), text("%", GRAY)),
                                 join(noSeparators(), text(tiny("cost "), GRAY), text(1, WHITE), text(tiny("tp"), GRAY))));
        return icon;
    }

    private ItemStack getExpIcon(Session session, SkillType skillType) {
        int bonus = session.getExpBonus(skillType);
        ItemStack icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
        icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        icon.setAmount(Math.max(1, Math.min(64, bonus)));
        Items.text(icon, List.of(join(noSeparators(), VanillaItems.EXPERIENCE_BOTTLE, text(" Exp Bonus", GOLD)),
                                 join(noSeparators(), text(tiny("current bonus "), GRAY), text(bonus, WHITE), text("xp", GRAY)),
                                 join(noSeparators(), text(tiny("next bonus "), GRAY), text((bonus + 1), WHITE), text("xp", GRAY)),
                                 join(noSeparators(), text(tiny("cost "), GRAY), text(1, WHITE), text(tiny("tp"), GRAY))));
        return icon;
    }

    private void onClickTalent(Player player, TalentType talentType) {
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        if (session.isTalentEnabled(talentType)) {
            session.setTalentEnabled(talentType, false);
            talents(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
        } else if (session.hasTalent(talentType)) {
            session.setTalentEnabled(talentType, true);
            talents(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
        } else if (session.getTalentPoints(talentType.skillType) >= talentType.talentPointCost) {
            session.unlockTalent(talentType, () -> {
                    Effects.talentUnlock(player);
                    talents(player);
                });
        } else {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
        }
    }
}