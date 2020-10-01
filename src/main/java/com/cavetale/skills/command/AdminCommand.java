package com.cavetale.skills.command;

import com.cavetale.core.command.CommandContext;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.skills.SQLSkill;
import com.cavetale.skills.Session;
import com.cavetale.skills.SkillType;
import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Talent;
import com.cavetale.skills.util.Gui;
import com.cavetale.skills.util.Items;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class AdminCommand extends CommandBase implements TabExecutor {
    private final SkillsPlugin plugin;
    private CommandNode root = new CommandNode("skadm");

    public void enable() {
        root.description("Skills admin command");
        root.addChild("reloadadvancements")
            .description("Reload all advanements")
            .caller(this::reloadAdvancementsCommand);
        root.addChild("gimme")
            .description("Give yourself a talent point")
            .caller(this::gimmeCommand);
        root.addChild("particles")
            .description("Toggle particles")
            .caller(this::particlesCommand);
        root.addChild("median")
            .description("Show some player stats")
            .caller(this::medianCommand);
        root.addChild("gui")
            .description("Generate a talent gui")
            .caller(this::guiCommand);
        root.addChild("give")
            .arguments("<player> <skill> <amount>")
            .description("Give a player skill points")
            .caller(this::giveCommand);
        CommandNode talentNode = root.addChild("talent")
            .description("Talent commands");
        talentNode.addChild("unlock")
            .caller(this::talentUnlockCommand)
            .arguments("<player> <talent>")
            .description("Unlock talent");
        talentNode.addChild("lock")
            .caller(this::talentLockCommand)
            .arguments("<player> <talent>")
            .description("Lock talent");
        plugin.getCommand("skadmin").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        return root.call(new CommandContext(sender, command, alias, args), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return root.complete(new CommandContext(sender, command, alias, args), args);
    }

    boolean reloadAdvancementsCommand(CommandContext context, CommandNode node, String[] args) {
        if (args.length != 0) return false;
        context.sender.sendMessage("Reloading advancements...");
        plugin.getAdvancements().unloadAll();
        plugin.getAdvancements().loadAll();
        context.sender.sendMessage("Advancements reloaded.");
        return true;
    }

    boolean gimmeCommand(CommandContext context, CommandNode node, String[] args) {
        if (args.length != 0) return false;
        Player player = context.requirePlayer();
        plugin.getTalents().addPoints(player, 1);
        return true;
    }

    boolean particlesCommand(CommandContext context, CommandNode node, String[] args) {
        if (args.length != 0) return false;
        Player player = context.requirePlayer();
        Session session = plugin.getSessions().of(player);
        boolean noParticles = !session.isNoParticles();
        session.setNoParticles(noParticles);
        player.sendMessage("Particles: " + (noParticles ? "off" : "on"));
        return true;
    }

    boolean medianCommand(CommandContext context, CommandNode node, String[] args) {
        if (args.length != 0) return false;
        for (SkillType skill : SkillType.values()) {
            List<SQLSkill> rows = plugin.getSql().getSkillRows().stream()
                .filter(s -> s.getLevel() > 0)
                .filter(s -> skill.key.equals(s.getSkill()))
                .sorted((b, a) -> Integer.compare(a.getTotalPoints(),
                                                  b.getTotalPoints()))
                .collect(Collectors.toList());
            if (rows.isEmpty()) continue;
            int sumSP = 0;
            int sumLevel = 0;
            for (SQLSkill row : rows) {
                sumSP += row.getTotalPoints();
                sumLevel += row.getLevel();
            }
            int avgSP = sumSP / rows.size();
            int avgLevel = sumLevel / rows.size();
            SQLSkill median = rows.get(rows.size() / 2);
            SQLSkill max = rows.get(0);
            String msg = skill.displayName
                + "\t"
                + " Sample=" + rows.size()
                + " Sum=" + sumSP + "," + sumLevel
                + " Avg=" + avgSP + "," + avgLevel
                + " Max=" + max.getTotalPoints() + "," + max.getLevel()
                + " Med=" + median.getTotalPoints() + "," + median.getLevel();
            context.sender.sendMessage(msg);
        }
        return true;
    }

    public static final class GuiState implements Cloneable {
        Talent[] slots = new Talent[6 * 9];
        Talent talent;
        int x;
        int y;
        Dir dir;

        @Override
        public GuiState clone() {
            GuiState c = new GuiState();
            for (int i = 0; i < slots.length; i += 1) {
                c.slots[i] = slots[i];
            }
            c.talent = talent;
            c.x = x;
            c.y = y;
            c.dir = dir;
            return c;
        }
    }

    enum Dir {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0),
        UP_LEFT(-1, -1),
        DOWN_LEFT(-1, 1),
        UP_RIGHT(-1, 1),
        DOWN_RIGHT(1, 1);

        public final int x;
        public final int y;

        Dir(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        int dist(Dir o) {
            return Math.abs(o.x - x) + Math.abs(o.y - y);
        }
    }

    GuiState recur(GuiState in, int level) {
        GuiState tmp = in.clone();
        List<Talent> dependants = in.talent.getDependants();
        if (dependants.isEmpty()) return in;
        for (Talent talent : dependants) {
            List<Dir> dirs = new ArrayList<>(Arrays.asList(Dir.values()));
            if (in.dir != null) {
                int index = dirs.indexOf(in.dir);
                dirs.set(index, dirs.get(0));
                dirs.set(0, in.dir);
                Collections.sort(dirs, (a, b) -> Integer.compare(a.dist(in.dir), b.dist(in.dir)));
            } else {
                Collections.shuffle(dirs);
            }
            GuiState res = null;
            for (Dir dir : dirs) {
                int x = in.x + dir.x;
                if (x < 0 || x > 8) continue;
                int y = in.y + dir.y;
                if (y < 0 || y > 5) continue;
                boolean empty = tmp.slots[x + y * 9] == null;
                if (!empty) continue;
                GuiState nx = tmp.clone();
                nx.talent = talent;
                nx.x = x;
                nx.y = y;
                nx.slots[x + y * 9] = talent;
                nx.dir = dir;
                nx = recur(nx.clone(), level + 1);
                if (nx == null) continue;
                res = nx;
                break;
            }
            if (res == null) return null;
            tmp = res;
        }
        tmp.dir = in.dir;
        return tmp;
    }

    boolean guiCommand(CommandContext context, CommandNode node, String[] args) {
        Player player = context.requirePlayer();
        GuiState state = new GuiState();
        state.x = 4;
        state.y = 2;
        state.talent = Talent.ROOT;
        state.slots[state.x + state.y * 9] = state.talent;
        state = recur(state, 0);
        if (state == null) {
            player.sendMessage("fail!");
            return true;
        }
        Gui gui = new Gui(plugin).rows(6).title("Talents");
        try (java.io.PrintStream out = new java.io.PrintStream(new java.io.File("tmp.txt"))) {
            for (int i = 0; i < state.slots.length; i += 1) {
                Talent talent = state.slots[i];
                if (talent == null) continue;
                ItemStack a = talent.getIcon();
                ItemStack b = Items.of(Material.CHEST)
                    .tooltip(Items.of(a).tooltip())
                    .create();
                gui.setItem(i, talent.getIcon());
                final int index = i;
                gui.addTask(() -> {
                        int time = gui.getTicks() / 10;
                        if (time % 2 == 0) {
                            gui.setItem(index, a);
                        } else {
                            gui.setItem(index, b);
                        }
                    });
                out.println(talent.name() + ".guiIndex = " + i + ";");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        gui.open(player);
        return true;
    }

    boolean giveCommand(CommandContext context, CommandNode node, String[] args) {
        if (args.length != 3) return false;
        Player target = playerOf(args[0]);
        SkillType skillType = SkillType.ofKey(args[1]);
        if (skillType == null) throw new CommandWarn("Skill not found: " + args[1]);
        int points = parseInt(args[2]);
        if (points < 1) throw new CommandWarn("Must be positive");
        plugin.getSkillPoints().give(target, skillType, points);
        context.sender.sendMessage("" + points + " " + skillType.displayName + " points given to " + target.getName());
        return true;
    }

    boolean talentUnlockCommand(CommandContext context, CommandNode node, String[] args) {
        return false;
    }

    boolean talentLockCommand(CommandContext context, CommandNode node, String[] args) {
        return false;
    }
}
