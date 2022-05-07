package com.github.selfcrafted.minestomspleef;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class Server {
    public static final String VERSION = "&version";
    public static final String MINESTOM_VERSION = "&minestomVersion";

    private static final String VELOCITY_SECRET = System.getenv("VELOCITY_SECRET");

    static InstanceContainer LOBBY_INSTANCE;


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
        LOBBY_INSTANCE = MinecraftServer.getInstanceManager().createInstanceContainer();
        LOBBY_INSTANCE.setTime(-6000);
        LOBBY_INSTANCE.setTimeRate(0);
        LOBBY_INSTANCE.setTimeUpdate(null);
        LOBBY_INSTANCE.setChunkLoader(new LobbyGenerator());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(LOBBY_INSTANCE);
            event.getPlayer().setRespawnPoint(LobbyGenerator.START);
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, event -> {
            var spleefGame = new SpleefGame();
            spleefGame.addPlayer(event.getPlayer());
            spleefGame.start();
        });

        // Start server
        if (VELOCITY_SECRET != null) VelocityProxy.enable(VELOCITY_SECRET);
        server.start("0.0.0.0", 25565);
    }

    static class LobbyGenerator implements IChunkLoader {
        static final Pos START = new Pos(7.5, 101, 7.5);
        @Override
        public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
            Chunk chunk = new DynamicChunk(instance, chunkX, chunkZ);
            if (chunkX == 0 && chunkZ == 0) {
                for (int x = 0; x <= 16; x++)
                    for (int z = 0; z <= 16; z++) {
                        chunk.setBlock(x, 100, z, Block.QUARTZ_PILLAR);
                        chunk.setBlock(x, 98, z, Block.IRON_BLOCK);
                        if (x%15 == 0 || z%15 == 0) {
                            chunk.setBlock(x, 101, z, Block.TINTED_GLASS);
                            chunk.setBlock(x, 102, z, Block.BARRIER);
                        }
                    }
                chunk.setBlock(7, 100, 7, Block.RED_STAINED_GLASS);
                chunk.setBlock(7, 99, 7, Block.BEACON);
            }
            return CompletableFuture.completedFuture(chunk);
        }

        @Override
        public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
            return CompletableFuture.completedFuture(null);
        }
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