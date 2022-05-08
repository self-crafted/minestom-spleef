package com.github.selfcrafted.minestomspleef;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class Lobby {
    static final Pos SPAWN = new Pos(7.5, 101, 7.5);
    static InstanceContainer LOBBY_CONTAINER;

    private Lobby() {}

    static void init() {
        // Add lobby instance
        LOBBY_CONTAINER = MinecraftServer.getInstanceManager().createInstanceContainer();
        LOBBY_CONTAINER.setTime(-6000);
        LOBBY_CONTAINER.setTimeRate(0);
        LOBBY_CONTAINER.setTimeUpdate(null);
        LOBBY_CONTAINER.setChunkLoader(new LobbyGenerator());

        var globalNode = MinecraftServer.getGlobalEventHandler();

        globalNode.addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(LOBBY_CONTAINER);
            event.getPlayer().setRespawnPoint(SPAWN);
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        });

        globalNode.addListener(PlayerSpawnEvent.class, event -> {
            if (event.getSpawnInstance() != LOBBY_CONTAINER) return;
            // TODO: 08.05.22 add menus for game selection and spectating
        });

        var eventNode = EventNode.value("lobby", EventFilter.INSTANCE,
                instance -> instance == LOBBY_CONTAINER);

        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            // TODO: 08.05.22 add menus for game selection and spectating
        });

        globalNode.addChild(eventNode);
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
}
