package com.github.selfcrafted.minestomspleef.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;

public class ShutdownCommand extends Command {
    public ShutdownCommand() {
        super("end", "stop", "shutdown");
        addConditionalSyntax(
                (sender, commandString) -> sender instanceof ConsoleSender,
                (sender, context) -> MinecraftServer.stopCleanly()
        );
    }
}
