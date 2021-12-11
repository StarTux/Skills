package com.cavetale.skills;

import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.VanillaItems;
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
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

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

    public Gui talents(Player player) {
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return null;
        final SkillType skillType = session.getTalentGui();
        final int size = 6 * 9;
        final Gui gui = new Gui(plugin).size(size);
        GuiOverlay.Builder builder = GuiOverlay.builder(size)
            .title(Component.join(JoinConfiguration.noSeparators(),
                                  VanillaItems.componentOf(skillType.tag.icon()),
                                  Component.text(skillType.tag.title() + " Skill",
                                                 skillType.tag.color(),
                                                 TextDecoration.BOLD)))
            .layer(GuiOverlay.BLANK, skillType.tag.color())
            .layer(GuiOverlay.TOP_BAR, NamedTextColor.GRAY);
        // Make top menu
        for (SkillType otherSkillType : SkillType.values()) {
            final int slot = 3 + otherSkillType.ordinal();
            if (otherSkillType == skillType) {
                builder.highlightSlot(slot, skillType.tag.color());
            }
            ItemStack icon = icon(otherSkillType.tag.icon(),
                                  Component.text(otherSkillType.tag.title(), otherSkillType.tag.color()));
            gui.setItem(slot, icon, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
                    session.setTalentGui(otherSkillType);
                    talents(player);
                });
        }
        final int talentPoints = session.getTalentPoints();
        final int talentCost = session.getTalentCost();
        if (talentPoints > 0) {
            ItemStack talentItem = icon(Material.ENDER_EYE,
                                        Component.text("You have " + talentPoints + " Talent Point"
                                                       + (talentPoints > 1 ? "s" : "")));
            talentItem.setAmount(Math.min(64, talentPoints));
            gui.setItem(8, talentItem);
        }
        // Root
        gui.setItem(4 + 3 * 9, icon(skillType.tag.icon(), Component.text(skillType.tag.title(),
                                                                         skillType.tag.color())));
        builder.highlightSlot(4 + 3 * 9, NamedTextColor.WHITE);
        // Talents
        for (TalentType talentType : TalentType.SKILL_MAP.get(skillType)) {
            final int slot = talentType.tag.x() + talentType.tag.y() * 9;
            if (session.canAccessTalent(talentType)) {
                ItemStack icon = new ItemStack(talentType.tag.icon());
                icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
                List<Component> text = new ArrayList<>();
                text.add(Component.text(talentType.tag.title(), skillType.tag.color()));
                if (session.isTalentEnabled(talentType)) {
                    text.add(Component.text("Enabled", NamedTextColor.GREEN));
                } else if (session.hasTalent(talentType)) {
                    text.add(Component.text("Disabled", NamedTextColor.RED));
                } else {
                    text.add(Component.join(JoinConfiguration.noSeparators(),
                                            Component.text("Unlock Cost ", NamedTextColor.GRAY),
                                            Component.text(talentCost + "TP", NamedTextColor.WHITE)));
                }
                for (String line : Text.wrapLine(talentType.tag.description(), LINELENGTH)) {
                    text.add(Component.text(line, NamedTextColor.GRAY));
                }
                icon = Items.text(icon, text);
                gui.setItem(slot, icon, click -> {
                        if (!click.isLeftClick()) return;
                        onClickTalent(player, talentType);
                    });
            }
            if (session.isTalentEnabled(talentType)) {
                builder.highlightSlot(slot, NamedTextColor.WHITE);
            } else if (session.hasTalent(talentType)) {
                builder.highlightSlot(slot, NamedTextColor.GRAY);
            } else {
                builder.highlightSlot(slot, skillType.tag.color());
            }
        }
        gui.title(builder.build());
        gui.open(player);
        return gui;
    }

    private void onClickTalent(Player player, TalentType talentType) {
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        if (session.isTalentEnabled(talentType)) {
            session.setTalentDisabled(talentType, true);
            talents(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
        } else if (session.hasTalent(talentType)) {
            session.setTalentDisabled(talentType, false);
            talents(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
        } else if (session.getTalentPoints() >= session.getTalentCost()) {
            session.unlockTalent(talentType);
            Effects.talentUnlock(player);
            talents(player);
        } else {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
        }
    }
}
