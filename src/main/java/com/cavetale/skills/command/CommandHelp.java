package com.cavetale.skills.command;

import java.util.List;

public interface CommandHelp {
    List<String> help(CommandContext context, CommandNode node);
}
