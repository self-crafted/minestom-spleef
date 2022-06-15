package com.github.selfcrafted.minestomspleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Lobby {
    static final Pos SPAWN = new Pos(7.5, 101, 7.5);
    static final ItemStack CREATE_ITEM = ItemStack.builder(Material.CRAFTING_TABLE)
            .displayName(Component.text("Create game", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false))
            .amount(1)
            .build();
    static final ItemStack PLAY_ITEM = ItemStack.builder(Material.COMPASS)
            .displayName(Component.text("Play", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false))
            .amount(1)
            .build();
    static final ItemStack SPECTATE_ITEM = ItemStack.builder(Material.SPYGLASS)
            .displayName(Component.text("Spectate", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false))
            .amount(1)
            .build();

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
            var player = event.getPlayer();
            var inventory = player.getInventory();
            player.setRespawnPoint(SPAWN);
            player.setGameMode(GameMode.ADVENTURE);
            inventory.clear();
            inventory.setItemStack(3, CREATE_ITEM);
            inventory.setItemStack(4, PLAY_ITEM);
            inventory.setItemStack(5, SPECTATE_ITEM);
            player.setHeldItemSlot((byte) 4);
        });

        globalNode.addListener(ItemDropEvent.class, event -> event.setCancelled(true));
        globalNode.addListener(PlayerSwapItemEvent.class, event -> event.setCancelled(true));
        globalNode.addListener(InventoryPreClickEvent.class, event ->
                event.setCancelled(List.of(CREATE_ITEM, PLAY_ITEM, SPECTATE_ITEM).contains(event.getClickedItem())));

        var eventNode = EventNode.value("lobby", EventFilter.INSTANCE,
                instance -> instance == LOBBY_CONTAINER);

        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            var player = event.getPlayer();
            if (CREATE_ITEM == event.getItemStack()) {
                player.openInventory(new CreateMenu(player).getInventory());
            } else if (PLAY_ITEM == event.getItemStack()) {
                GameManager.openPlayMenu(player);
            } else if (SPECTATE_ITEM == event.getItemStack()) {
                GameManager.openSpectateMenu(player);
            }
        });

        globalNode.addChild(eventNode);
    }

    static void join(Player player) { player.setInstance(LOBBY_CONTAINER, SPAWN); }

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
