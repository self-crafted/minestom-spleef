package com.github.selfcrafted.minestomspleef.command;

import com.github.selfcrafted.minestomspleef.GameManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class LeaveCommand extends Command {
    public LeaveCommand() {
        super("leave", "l");

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) GameManager.leave(player);
        });
    }
}
