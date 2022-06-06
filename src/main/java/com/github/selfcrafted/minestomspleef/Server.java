package com.github.selfcrafted.minestomspleef;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class Server {
    public static final String VERSION = "&version";
    public static final String MINESTOM_VERSION = "&minestomVersion";

    private static final String VELOCITY_SECRET = System.getenv("VELOCITY_SECRET");


    public static void main(String[] args) {
        System.setProperty("minestom.tps", "50");
        if (Arrays.asList(args).contains("--no-terminal")) System.setProperty("minestom.terminal.disabled", "");

        MinecraftServer.LOGGER.info("Java: " + Runtime.version());
        MinecraftServer.LOGGER.info("&Name: " + VERSION);
        MinecraftServer.LOGGER.info("Minestom: " + MINESTOM_VERSION);
        MinecraftServer.LOGGER.info("Protocol: %d (%s)".formatted(
                MinecraftServer.PROTOCOL_VERSION, MinecraftServer.VERSION_NAME));

        // Initialize server
        MinecraftServer server = MinecraftServer.init();
        Lobby.init();

        // Start server
        if (VELOCITY_SECRET != null) {
            VelocityProxy.enable(VELOCITY_SECRET);
            MinecraftServer.LOGGER.info("Velocity proxy support enabled.");
        }
        server.start("0.0.0.0", 25565);
    }

    static class ArenaGenerator implements IChunkLoader, Generator {
        static final Pos START = new Pos(7, 30, 7);

        private final int arenaSize;

        ArenaGenerator(int playerCount) {
            this.arenaSize = (int) (Math.log(playerCount)*10);
        }

        @Override
        public void generate(@NotNull GenerationUnit unit) {
            final var modifier = unit.modifier();

            var start = unit.absoluteStart();
            for (int x = 0; x < unit.size().x(); x++) {
                for (int z = 0; z < unit.size().z(); z++) {
                    var bottom = start.add(x, 0, z);
                    var spleefLayer = bottom.withY(START.blockY());
                    var distance = Math.floor(START.distance(spleefLayer));
                    if (distance <= arenaSize) {
                        var ceilingLayer = bottom.withY(START.blockY()+10);
                        modifier.setBlock(spleefLayer, Block.SAND);
                        modifier.setBlock(ceilingLayer, Block.SEA_LANTERN);
                        if (distance == arenaSize) modifier.fill(spleefLayer, ceilingLayer.add(1, 1, 1),
                                Block.LIGHT_BLUE_STAINED_GLASS);
                    }
                }
            }
        }

        @Override
        public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
            return CompletableFuture.completedFuture(null);
        }
    }
}