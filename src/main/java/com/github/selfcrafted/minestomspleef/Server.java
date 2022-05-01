package com.github.selfcrafted.minestomspleef;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
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
        lobbyInstance.setTime(-6000);
        lobbyInstance.setTimeRate(0);
        lobbyInstance.setTimeUpdate(null);
        lobbyInstance.setChunkLoader(new LobbyGenerator());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(lobbyInstance);
            event.getPlayer().setRespawnPoint(new Pos(7.5, 101, 7.5));
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        });

        // Start server
        if (VELOCITY_SECRET != null) VelocityProxy.enable(VELOCITY_SECRET);
        server.start("0.0.0.0", 25565);
    }

    private static class LobbyGenerator implements IChunkLoader {
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

    private static class ArenaGenerator implements IChunkLoader, Generator {
        private static final Pos START = new Pos(7, 100, 7);

        private final Pos min;
        private final Pos max;

        private ArenaGenerator(int size) {
            this.min = START.add(size/2.0, 0, size/2.0);
            this.max = START.sub(size/2.0, 0, size/2.0);
        }

        @Override
        public void generate(@NotNull GenerationUnit unit) {
            final var minimumX = Math.max(unit.absoluteStart().blockX(), this.min.blockX());
            final var minimumZ = Math.max(unit.absoluteStart().blockZ(), this.min.blockZ());
            final var maximumX = Math.min(unit.absoluteEnd().blockX(), this.max.blockX());
            final var maximumZ = Math.min(unit.absoluteEnd().blockZ(), this.max.blockZ());

            final var modifier = unit.modifier();

            if (minimumX == this.min.blockX()) modifier.fill(this.min.withZ(minimumZ),
                    this.min.withZ(maximumZ).add(0, 10, 0), Block.CYAN_STAINED_GLASS);
            if (minimumZ == this.min.blockZ()) modifier.fill(this.min.withX(minimumX),
                    this.min.withX(maximumX).add(0, 10, 0), Block.CYAN_STAINED_GLASS);
            if (minimumX == this.max.blockX()) modifier.fill(this.max.withZ(minimumZ),
                    this.max.withZ(maximumZ).add(0, 10, 0), Block.CYAN_STAINED_GLASS);
            if (minimumZ == this.max.blockZ()) modifier.fill(this.max.withX(minimumX),
                    this.max.withX(maximumX).add(0, 10, 0), Block.CYAN_STAINED_GLASS);

            modifier.fill(this.min, this.max, Block.SAND);
            modifier.fill(this.min.add(0, 10, 0), this.max.add(0, 10, 0), Block.SEA_LANTERN);
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