package com.cavetale.skills.command;

public interface CommandCall {
    boolean call(CommandContext context, CommandNode node, String[] args);
}
