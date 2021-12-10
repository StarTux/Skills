package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;

public final class HighscoreCommand extends AbstractCommand<SkillsPlugin> {
    protected HighscoreCommand(final SkillsPlugin plugin) {
        super(plugin, "highscore");
    }

    @Override
    protected void onEnable() {
        rootNode.arguments("[skill] [page]")
            .description("Skill Highscore")
            .completers(plugin.skillsCommand.highscoreCompleters)
            .playerCaller(plugin.skillsCommand::hi);
    }
}
