package com.cavetale.skills;

import com.cavetale.core.command.AbstractCommand;

public final class TalentCommand extends AbstractCommand<SkillsPlugin> {
    protected TalentCommand(final SkillsPlugin plugin) {
        super(plugin, "talent");
    }

    @Override
    protected void onEnable() {
        rootNode.denyTabCompletion()
            .description("Skills Talent Menu")
            .playerCaller(plugin.skillsCommand::talent);
    }
}
