package com.github.selfcrafted.minestomspleef;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Server {
    public static final String VERSION = "&version";
    public static final String MINESTOM_VERSION = "&minestomVersion";

    private static final String VELOCITY_SECRET = System.getenv("VELOCITY_SECRET");

    public static void main(String[] args) {
        System.setProperty("minestom.tps", "50");
        System.setProperty("minestom.terminal.disabled", "");

        MinecraftServer.LOGGER.info("Java: " + Runtime.version());
        MinecraftServer.LOGGER.info("&Name: " + VERSION);
        MinecraftServer.LOGGER.info("Minestom: " + MINESTOM_VERSION);
        MinecraftServer.LOGGER.info("Protocol: %d (%s)".formatted(
                MinecraftServer.PROTOCOL_VERSION, MinecraftServer.VERSION_NAME));

        // Initialize server
        MinecraftServer server = MinecraftServer.init();

        // Add lobby instance
        final var lobbyInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        lobbyInstance.setTime(-12000);
        lobbyInstance.setChunkLoader(new AnvilLoader("lobby"));
        lobbyInstance.setGenerator(new LobbyGenerator());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(lobbyInstance);
            event.getPlayer().setRespawnPoint(new Pos(0, 100.5, 0));
        });

        // Start server
        if (VELOCITY_SECRET != null) VelocityProxy.enable(VELOCITY_SECRET);
        server.start("0.0.0.0", 25565);
    }

    private static class LobbyGenerator implements Generator {
        @Override
        public void generate(@NotNull GenerationUnit unit) {
            final var start = unit.absoluteStart();
            final var end = unit.absoluteEnd();
            final var modifier = unit.modifier();

            modifier.fill(new Pos(-10, 101, 10), new Pos(10, 101, 10), Block.ACACIA_FENCE);
            modifier.fill(new Pos(-10, 101, 10), new Pos(-10, 101, -10), Block.ACACIA_FENCE);
            modifier.fill(new Pos(10, 101, -10), new Pos(10, 101, 10), Block.ACACIA_FENCE);
            modifier.fill(new Pos(10, 101, -10), new Pos(-10, 101, -10), Block.ACACIA_FENCE);

            modifier.fill(new Pos(-10, 100, 10), new Pos(-10, 100, 10), Block.IRON_BLOCK);
            modifier.setBlock(0, 100, 0, Block.RED_STAINED_GLASS);
            modifier.setBlock(0, 99, 0, Block.BEACON);
            modifier.fill(new Pos(-1, 98, 1), new Pos(-1, 98, 1), Block.IRON_BLOCK);
        }
    }
}