package com.cavetale.skills;

import com.cavetale.core.font.DefaultFont;
import com.cavetale.core.font.GuiOverlay;
import com.cavetale.core.font.VanillaItems;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.util.Items;
import com.cavetale.mytems.util.Text;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.skill.Talent;
import com.cavetale.skills.skill.TalentType;
import com.cavetale.skills.util.Books;
import com.cavetale.skills.util.Effects;
import com.cavetale.skills.util.Gui;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.skills.SkillsPlugin.moneyBonusPercentage;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsCommand;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class Guis {
    protected static final int LINELENGTH = 24;

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

    public static Gui talents(Player player) {
        Session session = sessionOf(player);
        if (!session.isEnabled()) return null;
        final SkillType skillType = session.getTalentGui();
        final int size = 6 * 9;
        final Gui gui = new Gui().size(size);
        GuiOverlay.Builder builder = GuiOverlay.builder(size)
            .title(skillType.asComponent())
            .layer(GuiOverlay.BLANK, skillType.textColor)
            .layer(GuiOverlay.TOP_BAR, DARK_GRAY);
        // Make top menu
        for (SkillType otherSkillType : SkillType.values()) {
            final int slot = 3 + otherSkillType.ordinal();
            if (otherSkillType == skillType) {
                builder.highlightSlot(slot, skillType.textColor);
            }
            ItemStack icon = Items.text(otherSkillType.createIcon(), List.of(otherSkillType.asComponent()));
            gui.setItem(slot, icon, click -> {
                    if (!click.isLeftClick()) return;
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 2.0f);
                    session.setTalentGui(otherSkillType);
                    talents(player);
                });
        }
        final int talentPoints = session.getTalentPoints(skillType);
        gui.setItem(0, Items.text(Mytems.TURN_LEFT.createIcon(), List.of(text("Back to skill page", GRAY))), click -> {
                if (!click.isLeftClick()) return;
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
        gui.setItem(5 + 3 * 9, Items.text(skillType.createIcon(), List.of(skillType.asComponent())));
        builder.highlightSlot(5 + 3 * 9, GOLD);
        // Talents
        for (TalentType talentType : TalentType.SKILL_MAP.get(skillType)) {
            final int slot = talentType.slot.x() + talentType.slot.y() * 9;
            ItemStack icon = talentType.getTalent().createIcon();
            icon.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
            List<Component> text = new ArrayList<>();
            text.add(talentType.asComponent());
            if (session.isTalentEnabled(talentType)) {
                text.add(text("Enabled", GREEN));
            } else if (session.hasTalent(talentType)) {
                text.add(text("Disabled", RED));
            } else {
                text.add(text("Locked", DARK_RED));
            }
            text.add(join(noSeparators(), text(tiny("cost "), GRAY), text(talentType.talentPointCost, WHITE), text(tiny("tp"), GRAY)));
            if (talentType.depends != null) {
                text.add(join(noSeparators(), text(tiny("requires "), GRAY), talentType.depends));
            }
            text.add(join(noSeparators(), Mytems.MOUSE_LEFT, text(" More Info", AQUA, ITALIC)));
            if (session.isTalentEnabled(talentType)) {
                text.add(join(noSeparators(), Mytems.MOUSE_RIGHT, text(" Disable", RED, ITALIC)));
            } else if (session.hasTalent(talentType)) {
                text.add(join(noSeparators(), Mytems.MOUSE_RIGHT, text(" Enable", GREEN, ITALIC)));
            } else {
                if (session.canAccessTalent(talentType)) {
                    text.add(join(noSeparators(), Mytems.MOUSE_RIGHT, text(" Unlock", GREEN, ITALIC)));
                }
            }
            for (String line : Text.wrapLine(talentType.getTalent().getRawDescription().get(0), LINELENGTH)) {
                text.add(text(line, GRAY));
            }
            icon = Items.text(icon, text);
            gui.setItem(slot, icon, click -> {
                    if (click.isLeftClick()) {
                        onLeftClickTalent(player, talentType);
                    } else if (click.isRightClick()) {
                        onRightClickTalent(player, talentType);
                    }
                });
            if (session.isTalentEnabled(talentType)) {
                builder.highlightSlot(slot, WHITE);
            } else if (session.hasTalent(talentType)) {
                builder.highlightSlot(slot, DARK_GRAY);
            } else if (session.canAccessTalent(talentType)) {
                builder.highlightSlot(slot, GRAY);
            } else {
                builder.highlightSlot(slot, skillType.textColor);
            }
        }
        builder.highlightSlot(9, skillType.textColor);
        gui.setItem(9, getMoneyIcon(session, skillType), click -> {
                if (!click.isRightClick()) return;
                boolean r = session.unlockMoneyBonus(skillType, () -> {
                        talents(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 1.0f);
                    });
                if (!r) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            });
        builder.highlightSlot(18, skillType.textColor);
        gui.setItem(18, getExpIcon(session, skillType), click -> {
                if (!click.isRightClick()) return;
                boolean r = session.unlockExpBonus(skillType, () -> {
                        talents(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 1.0f);
                    });
                if (!r) player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            });
        if (session.getTalentPointsSpent(skillType) > 0) {
            gui.setItem(45, getRespecIcon(session, skillType), click -> {
                    if (!click.isRightClick()) return;
                    if (!session.respec(player, skillType)) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER, 1.0f, 1.0f);
                    }
                });
        }
        gui.title(builder.build());
        gui.open(player);
        return gui;
    }

    private static ItemStack getMoneyIcon(Session session, SkillType skillType) {
        int bonus = session.getMoneyBonus(skillType);
        int perc = moneyBonusPercentage(bonus);
        int next = moneyBonusPercentage(bonus + 1);
        ItemStack icon = Mytems.GOLDEN_COIN.createIcon();
        icon.setAmount(Math.max(1, Math.min(64, bonus)));
        icon.editMeta(meta -> {
                Items.text(meta, List.of(join(noSeparators(), Mytems.GOLDEN_COIN, text(" Money Bonus", GOLD)),
                                         join(noSeparators(), Mytems.MOUSE_RIGHT, text(" Unlock", GRAY, ITALIC)),
                                         join(noSeparators(), text(tiny("cost "), GRAY), text(1, WHITE), text(tiny("tp"), GRAY)),
                                         join(noSeparators(), text(tiny("current "), GRAY), text(perc, WHITE), text("%", GRAY)),
                                         join(noSeparators(), text(tiny("next "), GRAY), text(next, WHITE), text("%", GRAY))));
            });
        return icon;
    }

    private static ItemStack getExpIcon(Session session, SkillType skillType) {
        int bonus = session.getExpBonus(skillType);
        ItemStack icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
        icon.setAmount(Math.max(1, Math.min(64, bonus)));
        icon.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.values());
                Items.text(meta, List.of(join(noSeparators(), VanillaItems.EXPERIENCE_BOTTLE, text(" Exp Bonus", GOLD)),
                                         join(noSeparators(), Mytems.MOUSE_RIGHT, text(" Unlock", GRAY, ITALIC)),
                                         join(noSeparators(), text(tiny("cost "), GRAY), text(1, WHITE), text(tiny("tp"), GRAY)),
                                         join(noSeparators(), text(tiny("current bonus "), GRAY), text(bonus, WHITE), text("xp", GRAY)),
                                         join(noSeparators(), text(tiny("next bonus "), GRAY), text((bonus + 1), WHITE), text("xp", GRAY))));
            });
        return icon;
    }

    private static ItemStack getRespecIcon(Session session, SkillType skillType) {
        ItemStack icon = Mytems.REDO.createIcon();
        int tp = session.getTalentPointsSpent(skillType);
        icon.editMeta(meta -> {
                Items.text(meta, List.of(join(noSeparators(), text(" Refund "), skillType, text(" Talent Points")).color(BLUE),
                                         join(noSeparators(), Mytems.MOUSE_RIGHT, text(" Purchase", GRAY, ITALIC)),
                                         join(noSeparators(), text(tiny("cost "), GRAY), text(1, WHITE), Mytems.KITTY_COIN),
                                         join(noSeparators(), text(tiny("total "), GRAY), text(tp, WHITE), text(tiny("tp"), GRAY))));
            });
        return icon;
    }

    private static void onLeftClickTalent(Player player, TalentType talentType) {
        List<Component> description = talentType.getTalent().getDescription();
        List<Component> pages = new ArrayList<>();
        List<Component> page = new ArrayList<>();
        Talent talent = talentType.getTalent();
        page.add(talentType.asComponent());
        page.add(empty());
        page.add(join(noSeparators(), text(tiny("cost "), GRAY), text(talentType.talentPointCost), text(tiny("tp"), GRAY)));
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

    private static void onRightClickTalent(Player player, TalentType talentType) {
        Session session = sessionOf(player);
        if (!session.isEnabled()) return;
        if (!session.canAccessTalent(talentType)) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 0.5f);
            return;
        }
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

    private Guis() { }
}
