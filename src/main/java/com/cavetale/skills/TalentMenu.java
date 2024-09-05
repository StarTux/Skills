package com.cavetale.skills;

import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.text.LineWrap;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Gui;
import com.cavetale.mytems.util.Text;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.talent.Talent;
import com.cavetale.skills.talent.TalentLevel;
import com.cavetale.skills.talent.TalentType;
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
import static com.cavetale.skills.SkillsPlugin.skillsCommand;
import static com.cavetale.skills.util.Text.formatDouble;
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
    private static final Vec2i ROOT_VECTOR = Vec2i.of(4, 3);
    private final Player player;
    private final Session session;
    private Gui gui;
    private final LineWrap lineWrap = new LineWrap()
        .componentMaker(input -> text(input, GRAY))
        .maxLineLength(18);

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
                gui.layer(GuiOverlay.TAB_BG, color(0x404040));
                gui.layer(GuiOverlay.tab(slot), skillType.textColor);
            }
            final int otherTalentPoints = session.getTalentPoints(otherSkillType);
            final boolean focus = otherTalentPoints > 0;
            ItemStack icon = tooltip(otherSkillType.createIcon(focus), List.of(otherSkillType.getIconTitle()));
            final int stackSize = Math.max(1, Math.min(99, otherTalentPoints));
            icon.editMeta(meta -> meta.setMaxStackSize(stackSize));
            icon.setAmount(stackSize);
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
            talentItem.editMeta(meta -> meta.setMaxStackSize(Math.min(99, talentPoints)));
            talentItem.setAmount(Math.min(99, talentPoints));
            gui.setItem(8, talentItem);
        }
        // Root
        gui.setItem(ROOT_VECTOR.x, ROOT_VECTOR.z, tooltip(skillType.createIcon(), List.of(text("Back to skill page", GRAY))), click -> {
                if (!click.isLeftClick()) return;
                skillsCommand().skill(player, skillType);
            });
        gui.highlight(ROOT_VECTOR.x, ROOT_VECTOR.z, GOLD);
        // Talents
        for (TalentType talentType : TalentType.SKILL_MAP.get(skillType)) {
            if (!talentType.isEnabled()) continue;
            makeTalentIcon(talentType);
            makeDependencyArrow(talentType);
        }
        gui.highlight(27, skillType.textColor);
        gui.setItem(27, getMoneyIcon(skillType), click -> {
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
            gui.highlight(45, color(0x202020));
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
        } else if (currentLevel != null) {
            icon = currentLevel.createIcon();
            icon.editMeta(meta -> meta.setMaxStackSize(currentLevel.getLevel()));
            icon.setAmount(currentLevel.getLevel());
        } else {
            icon = talent.createIcon();
        }
        // Make tooltip
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(talentType.asComponent());
        tooltip.addAll(lineWrap.wrap(talent.getRawDescription().get(0)));
        if (currentLevel != null) {
            tooltip.add(DIVIDER);
            if (maxLevel.getLevel() > 1) {
                tooltip.add(textOfChildren(Mytems.CHECKED_CHECKBOX,
                                           text(tiny(" lv ") + currentLevel.getLevel() + "/" + maxLevel.getLevel(), GREEN)));
            } else {
                tooltip.add(textOfChildren(Mytems.CHECKED_CHECKBOX,
                                           text(tiny(" unlocked for " + currentLevel.getTalentPointCost() + tiny("tp")), GREEN)));
            }
            for (String raw : currentLevel.getRawDescription()) {
                tooltip.addAll(lineWrap.wrap(raw));
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
                tooltip.addAll(lineWrap.wrap(raw));
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
        tooltip.add(DIVIDER);
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

    private void makeDependencyArrow(TalentType talentType) {
        final Vec2i dep = talentType.depends != null
            ? talentType.depends.slot
            : ROOT_VECTOR;
        final Vec2i vec = talentType.slot.subtract(dep);
        if (vec.equals(2, 0)) {
            gui.setItem(dep.x + 1, dep.z, noTooltip(Mytems.ARROW_RIGHT), null);
        } else if (vec.equals(-2, 0)) {
            gui.setItem(dep.x - 1, dep.z, noTooltip(Mytems.ARROW_LEFT), null);
        } else if (vec.equals(0, 2)) {
            gui.setItem(dep.x, dep.z + 1, noTooltip(Mytems.ARROW_DOWN), null);
        } else if (vec.equals(0, -2)) {
            gui.setItem(dep.x, dep.z - 1, noTooltip(Mytems.ARROW_UP), null);
        } else if (vec.equals(1, -1)) {
            gui.setItem(dep.x, dep.z - 1, noTooltip(Mytems.TURN_RIGHT), null);
        } else if (vec.equals(-1, -1)) {
            gui.setItem(dep.x, dep.z - 1, noTooltip(Mytems.TURN_LEFT), null);
        }
    }

    private static ItemStack noTooltip(Mytems mytems) {
        ItemStack result = mytems.createIcon();
        result.editMeta(meta -> meta.setHideTooltip(true));
        return result;
    }

    private ItemStack getMoneyIcon(SkillType skillType) {
        final int bonus = session.getMoneyBonus(skillType);
        final ItemStack icon = bonus < 100
            ? Mytems.GOLDEN_COIN.createIcon()
            : Mytems.DIAMOND_COIN.createIcon();
        final int stackSize = Math.max(1, bonus % 100);
        icon.editMeta(meta -> meta.setMaxStackSize(stackSize));
        icon.setAmount(stackSize);
        icon.editMeta(meta -> {
                final List<Component> tooltip = new ArrayList<>();
                tooltip.add(textOfChildren(Mytems.GOLDEN_COIN, text("Money Bonus", GOLD)));
                tooltip.add(text("Increase your money", GRAY));
                tooltip.add(textOfChildren(text("income from ", GRAY), skillType));
                if (bonus > 0) {
                    tooltip.add(DIVIDER);
                    final double percentage = session.getMoneyBonusPercentage(skillType);
                    tooltip.add(textOfChildren(Mytems.CHECKED_CHECKBOX, text(tiny(" lv ") + bonus, GREEN)));
                    tooltip.add(text(formatDouble(percentage) + "% money bonus", GRAY));
                }
                tooltip.add(DIVIDER);
                tooltip.add(textOfChildren(Mytems.ARROW_RIGHT, text(tiny(" lv ") + (bonus + 1), GREEN)));
                final double nextPercentage = session.getSkill(skillType).moneyBonusToPercentage(bonus + 1);
                tooltip.add(text(formatDouble(nextPercentage) + "% money bonus", GRAY));
                tooltip.add(textOfChildren(Mytems.MOUSE_RIGHT, text(tiny(" unlock for ") + 1 + tiny("tp"), GREEN)));
                tooltip(meta, tooltip);
            });
        return icon;
    }

    private ItemStack getExpIcon(SkillType skillType) {
        final int bonus = session.getExpBonus(skillType);
        final ItemStack icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
        final int stackSize = Math.max(1, bonus % 100);
        icon.editMeta(meta -> meta.setMaxStackSize(stackSize));
        icon.setAmount(stackSize);
        icon.editMeta(meta -> {
                final List<Component> tooltip = new ArrayList<>();
                tooltip.add(textOfChildren(VanillaItems.EXPERIENCE_BOTTLE, text("Exp Bonus", GREEN)));
                tooltip.add(text("Increase your exp", GRAY));
                tooltip.add(textOfChildren(text("drops from ", GRAY), skillType));
                if (bonus > 0) {
                    tooltip.add(DIVIDER);
                    tooltip.add(textOfChildren(Mytems.CHECKED_CHECKBOX, text(tiny(" lv ") + bonus, GREEN)));
                    tooltip.add(text(bonus + " bonus exp", GRAY));
                }
                tooltip.add(DIVIDER);
                tooltip.add(textOfChildren(Mytems.ARROW_RIGHT, text(tiny(" lv ") + (bonus + 1), GREEN)));
                tooltip.add(text((bonus + 1) + " bonus exp", GRAY));
                tooltip.add(textOfChildren(Mytems.MOUSE_RIGHT, text(tiny(" unlock for ") + 1 + tiny("tp"), GREEN)));
                tooltip(meta, tooltip);
            });
        return icon;
    }

    private ItemStack getRespecIcon(SkillType skillType) {
        ItemStack icon = Mytems.REDO.createIcon();
        int tp = session.getTalentPointsSpent(skillType);
        icon.editMeta(meta -> {
                tooltip(meta, List.of(textOfChildren(text("Refund ", BLUE), skillType.getIconTitle()),
                                      text(" Talent Points", BLUE),
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
        for (TalentLevel level : talent.getLevels()) {
            page.clear();
            page.add(text("Level " + level.getLevel(), BLUE));
            page.add(textOfChildren((session.getTalentLevel(talentType) >= level.getLevel() ? Mytems.CHECKED_CHECKBOX : Mytems.CHECKBOX),
                                    text(tiny("unlock cost "), GRAY), text(level.getTalentPointCost())));
            page.add(empty());
            page.addAll(level.getDescription());
            pages.add(join(separator(newline()), page));
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
