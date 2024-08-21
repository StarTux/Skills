package com.cavetale.skills;

import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import com.cavetale.mytems.util.Text;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentLevel;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Books;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.mytems.util.Items.tooltip;
import static com.cavetale.skills.SkillsPlugin.moneyBonusPercentage;
import static com.cavetale.skills.SkillsPlugin.skillsCommand;
import static java.awt.Color.HSBtoRGB;
import static java.awt.Color.RGBtoHSB;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextColor.color;
import static net.kyori.adventure.text.format.TextDecoration.*;

@RequiredArgsConstructor
public final class TalentMenu {
    protected static final int LINELENGTH = 24;
    private final Player player;
    private final Session session;
    private Gui gui;

    private static ItemStack icon(Material material, Component... lines) {
        ItemStack icon = new ItemStack(material);
        icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return tooltip(icon, List.of(lines));
    }

    private static ItemStack icon(Mytems mytems, Component... lines) {
        ItemStack icon = mytems.createIcon();
        icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        return tooltip(icon, List.of(lines));
    }

    public Gui open() {
        if (!session.isEnabled()) return null;
        final SkillType skillType = session.getTalentGui();
        final int size = 6 * 9;
        gui = new Gui()
            .size(size)
            .title(skillType.getIconTitle())
            .layer(GuiOverlay.BLANK, skillType.textColor);
        // Make top menu
        for (SkillType otherSkillType : SkillType.values()) {
            final int slot = 3 + otherSkillType.ordinal();
            if (otherSkillType == skillType) {
                float[] hsb = RGBtoHSB(skillType.textColor.red(),
                                       skillType.textColor.green(),
                                       skillType.textColor.blue(), null);
                gui.layer(GuiOverlay.TAB_BG, color(HSBtoRGB(hsb[0], hsb[1] * 0.65f, hsb[2] * 0.65f)));
                gui.layer(GuiOverlay.tab(slot), skillType.textColor);
            }
            final int otherTalentPoints = session.getTalentPoints(otherSkillType);
            final boolean focus = otherTalentPoints > 0;
            ItemStack icon = tooltip(otherSkillType.createIcon(focus), List.of(otherSkillType.getIconTitle()));
            icon.setAmount(Math.max(otherTalentPoints, 1));
            gui.setItem(slot, icon, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
                    session.setTalentGui(otherSkillType);
                    open();
                });
        }
        final int talentPoints = session.getTalentPoints(skillType);
        gui.setItem(0, Mytems.TURN_LEFT.createIcon(List.of(text("Back to Overview", GRAY))), click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                skillsCommand().skill(player, skillType);
            });
        if (talentPoints > 0) {
            ItemStack talentItem = icon(Material.ENDER_EYE,
                                        text(talentPoints + " "
                                             + skillType.displayName + " Talent Point"
                                             + (talentPoints > 1 ? "s" : "")));
            talentItem.setAmount(Math.min(64, talentPoints));
            gui.setItem(8, talentItem);
        }
        // Root
        gui.setItem(5 + 3 * 9, tooltip(skillType.createIcon(), List.of(text("Back to skill page", GRAY))), click -> {
                if (!click.isLeftClick()) return;
                skillsCommand().skill(player, skillType);
            });
        gui.highlight(5 + 3 * 9, GOLD);
        // Talents
        for (TalentType talentType : TalentType.SKILL_MAP.get(skillType)) {
            if (!talentType.isEnabled() && !session.isDebugMode()) continue;
            makeTalentIcon(talentType);
        }
        gui.highlight(9, skillType.textColor);
        gui.setItem(9, getMoneyIcon(skillType), click -> {
                if (!click.isRightClick()) return;
                boolean r = session.unlockMoneyBonus(skillType, () -> {
                        open();
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 1.0f);
                    });
                if (!r) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            });
        gui.highlight(18, skillType.textColor);
        gui.setItem(18, getExpIcon(skillType), click -> {
                if (!click.isRightClick()) return;
                boolean r = session.unlockExpBonus(skillType, () -> {
                        open();
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 1.0f);
                    });
                if (!r) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            });
        if (session.getTalentPointsSpent(skillType) > 0) {
            gui.setItem(45, getRespecIcon(skillType), click -> {
                    if (!click.isRightClick()) return;
                    if (!session.respec(player, skillType)) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER, 1.0f, 1.0f);
                    }
                });
        }
        gui.setItem(Gui.OUTSIDE, null, click -> {
                if (!click.isLeftClick()) return;
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, SoundCategory.MASTER, 1.0f, 1.0f);
                skillsCommand().skill(player, skillType);
            });
        gui.open(player);
        session.getSkill(skillType).setReminder(false);
        return gui;
    }

    private static final Component DIVIDER = text(" ".repeat(Text.ITEM_LORE_WIDTH + 2), color(0x28055E), STRIKETHROUGH);

    private void makeTalentIcon(TalentType talentType) {
        final Talent talent = talentType.getTalent();
        final boolean enabled = session.isTalentEnabled(talentType);
        final boolean canAccess = session.canAccessTalent(talentType);
        final int level = session.getTalentLevel(talentType);
        final TalentLevel maxLevel = talent.getMaxLevel();
        final TalentLevel currentLevel = talent.getLevel(level);
        final TalentLevel nextLevel = level < maxLevel.getLevel()
            ? talent.getLevel(level + 1)
            : null;
        final int cost = nextLevel != null
            ? nextLevel.getTalentPointCost()
            : 0;
        final int has = session.getTalentPoints(talentType.skillType);
        ItemStack icon;
        if (!canAccess) {
            icon = Mytems.SILVER_KEYHOLE.createIcon();
        } else if (level > 0 && !enabled) {
            icon = Mytems.CROSSED_CHECKBOX.createIcon();
        } else {
            icon = talent.createIcon();
        }
        // Make tooltip
        List<Component> tooltip = new ArrayList<>();
        for (String raw : talent.getRawDescription()) {
            for (String line : Text.wrapLine(raw, LINELENGTH)) {
                tooltip.add(text(line, GRAY));
            }
        }
        tooltip.add(talentType.asComponent());
        if (currentLevel != null) {
            tooltip.add(DIVIDER);
            if (maxLevel.getLevel() > 1) {
                tooltip.add(textOfChildren(Mytems.CHECKED_CHECKBOX,
                                           text(tiny(" lv ") + currentLevel.getLevel() + "/" + maxLevel.getLevel(), GREEN)));
            } else {
                tooltip.add(textOfChildren(Mytems.CHECKED_CHECKBOX,
                                           text(tiny(" unlocked"), GREEN)));
            }
            for (String raw : currentLevel.getRawDescription()) {
                for (String line : Text.wrapLine(raw, LINELENGTH)) {
                    tooltip.add(text(line, GRAY));
                }
            }
        }
        if (nextLevel != null) {
            tooltip.add(DIVIDER);
            if (maxLevel.getLevel() > 1) {
                tooltip.add(textOfChildren(Mytems.ARROW_RIGHT, text(tiny(" lv ") + nextLevel.getLevel() + "/" + maxLevel.getLevel(), RED)));
            } else {
                tooltip.add(textOfChildren(Mytems.ARROW_RIGHT, text(tiny(" unlock"), RED)));
            }
            for (String raw : nextLevel.getRawDescription()) {
                for (String line : Text.wrapLine(raw, LINELENGTH)) {
                    tooltip.add(text(line, GRAY));
                }
            }
            tooltip.add(textOfChildren(text(tiny("cost "), GRAY), text(cost, WHITE), text(tiny("tp"), GRAY)));
            if (!canAccess) {
                if (talentType.depends != null) {
                    tooltip.add(textOfChildren(text(tiny("requires "), DARK_RED), text(talentType.depends.getTalent().getDisplayName(), DARK_RED)));
                }
            } else if (has < cost) {
                tooltip.add(textOfChildren(text(tiny("requires "), DARK_RED), text(nextLevel.getTalentPointCost() + tiny("tp"), DARK_RED)));
            } else {
                if (nextLevel.getLevel() == 1) {
                    tooltip.add(textOfChildren(Mytems.MOUSE_RIGHT, text(tiny(" unlock for ") + cost + tiny("tp"), GREEN)));
                } else {
                    tooltip.add(textOfChildren(Mytems.MOUSE_RIGHT, text(tiny(" upgrade for ") + cost + tiny("tp"), GREEN)));
                }
                gui.highlight(talentType.slot.x, talentType.slot.z, BLUE);
            }
        }
        // Toggles
        if (currentLevel != null) {
            if (enabled) {
                tooltip.add(textOfChildren(text("DROP", RED), text(tiny(" to disable"), RED)));
            } else {
                tooltip.add(text(tiny("disabled"), DARK_RED));
                tooltip.add(textOfChildren(text("DROP", GREEN), text(tiny(" to enable"), GREEN)));
            }
        }
        tooltip.add(textOfChildren(Mytems.MOUSE_LEFT, text(tiny(" more info"), AQUA)));
        icon = tooltip(icon, tooltip);
        icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        gui.setItem(talentType.slot.x, talentType.slot.z, icon, click -> {
                if (click.isLeftClick()) {
                    onLeftClickTalent(talentType);
                } else if (click.isRightClick()) {
                    onRightClickTalent(talentType, nextLevel);
                } else if (click.getClick() == ClickType.DROP) {
                    onDropTalent(talentType);
                }
            });
    }

    private ItemStack getMoneyIcon(SkillType skillType) {
        int bonus = session.getMoneyBonus(skillType);
        int perc = moneyBonusPercentage(bonus);
        int next = moneyBonusPercentage(bonus + 1);
        ItemStack icon = Mytems.GOLDEN_COIN.createIcon();
        icon.setAmount(Math.max(1, Math.min(64, bonus)));
        icon.editMeta(meta -> {
                tooltip(meta, List.of(textOfChildren(Mytems.GOLDEN_COIN, text(" Money Bonus", GOLD)),
                                      textOfChildren(text(tiny("current "), GRAY), text(perc, WHITE), text("%", GRAY)),
                                      textOfChildren(text(tiny("next "), GRAY), text(next, WHITE), text("%", GRAY)),
                                      empty(),
                                      textOfChildren(text(tiny("cost "), GRAY), text(1, WHITE), text(tiny("tp"), GRAY)),
                                      textOfChildren(Mytems.MOUSE_RIGHT, text(" Unlock", GRAY, ITALIC))));
            });
        return icon;
    }

    private ItemStack getExpIcon(SkillType skillType) {
        int bonus = session.getExpBonus(skillType);
        ItemStack icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
        icon.setAmount(Math.max(1, Math.min(64, bonus)));
        icon.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.values());
                tooltip(meta, List.of(textOfChildren(VanillaItems.EXPERIENCE_BOTTLE, text(" Exp Bonus", GOLD)),
                                      textOfChildren(text(tiny("current bonus "), GRAY), text(bonus, WHITE), text("xp", GRAY)),
                                      textOfChildren(text(tiny("next bonus "), GRAY), text((bonus + 1), WHITE), text("xp", GRAY)),
                                      empty(),
                                      textOfChildren(text(tiny("cost "), GRAY), text(1, WHITE), text(tiny("tp"), GRAY)),
                                      textOfChildren(Mytems.MOUSE_RIGHT, text(" Unlock", GRAY, ITALIC))));
            });
        return icon;
    }

    private ItemStack getRespecIcon(SkillType skillType) {
        ItemStack icon = Mytems.REDO.createIcon();
        int tp = session.getTalentPointsSpent(skillType);
        icon.editMeta(meta -> {
                tooltip(meta, List.of(textOfChildren(text(" Refund "), skillType.getIconTitle(), text(" Talent Points")).color(BLUE),
                                      textOfChildren(text(tiny("total "), GRAY), text(tp, WHITE), text(tiny("tp"), GRAY)),
                                      empty(),
                                      textOfChildren(text(tiny("cost "), GRAY), text(1, WHITE), Mytems.KITTY_COIN),
                                      textOfChildren(Mytems.MOUSE_RIGHT, text(" Purchase", GRAY, ITALIC))));
            });
        return icon;
    }

    private void onLeftClickTalent(TalentType talentType) {
        if (!talentType.isEnabled()) return;
        List<Component> description = talentType.getTalent().getDescription();
        List<Component> pages = new ArrayList<>();
        List<Component> page = new ArrayList<>();
        Talent talent = talentType.getTalent();
        page.add(talentType.asComponent());
        page.add(empty());
        page.add(description.get(0));
        page.add(empty());
        page.add(DefaultFont.BACK_BUTTON.getComponent()
                 .hoverEvent(showText(text("Back to Talent Menu", GRAY)))
                 .clickEvent(runCommand("/talent")));
        pages.add(join(separator(newline()), page));
        for (int i = 1; i < description.size(); i += 1) {
            pages.add(description.get(i));
        }
        Books.open(player, pages);
    }

    private void onRightClickTalent(TalentType talentType, TalentLevel nextLevel) {
        if (!session.isEnabled()) return;
        if (nextLevel == null
            || !session.canAccessTalent(talentType)
            || session.getTalentPoints(talentType.skillType) < nextLevel.getTalentPointCost()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            return;
        }
        if (nextLevel.getLevel() == 1) {
            session.unlockTalent(talentType, () -> {
                    open();
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.25f, 2.0f);
                });
        } else {
            session.upgradeTalent(talentType, () -> {
                    open();
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.25f, 2.0f);
                });
        }
    }

    private void onDropTalent(TalentType talentType) {
        if (!talentType.isEnabled()) return;
        if (!session.isEnabled()) return;
        if (!session.hasTalent(talentType)) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            return;
        }
        final boolean oldValue = session.isTalentEnabled(talentType);
        session.setTalentEnabled(talentType, !oldValue);
        open();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
    }
}
