package com.github.selfcrafted.minestomspleef;

import com.sqcred.sboards.SBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.UUID;

public class GameInstance {
    private static final ItemStack SPLEEF_ITEM = ItemStack.builder(Material.IRON_SHOVEL)
            .displayName(Component.text("SPLEEF"))
            .meta(builder -> builder.canDestroy(Block.SAND).enchantment(Enchantment.EFFICIENCY, (short) 5).build())
            .build();

    private final SBoard BOARD = new SBoard(
            (player) -> Component.text("Time left: "+0, NamedTextColor.GOLD),
            (player) -> Arrays.asList(
                    Component.text("Ranking:"),
                    Component.text(player.getUsername(), NamedTextColor.GREEN),
                    Component.text(player.getUsername(), NamedTextColor.RED)
            )
    );
    private final InstanceContainer INSTANCE = MinecraftServer.getInstanceManager().createInstanceContainer();

    GameInstance(int players) {
        var generator = new Server.ArenaGenerator(players);
        INSTANCE.setGenerator(generator);
        INSTANCE.setChunkLoader(generator);
        INSTANCE.setTime(-6000);
        INSTANCE.setTimeRate(0);
        INSTANCE.setTimeUpdate(null);

        var eventNode = EventNode.event(INSTANCE.getUniqueId().toString(), EventFilter.PLAYER, playerEvent -> {
            if (playerEvent instanceof PlayerSpawnEvent) return ((PlayerSpawnEvent) playerEvent).getSpawnInstance() == INSTANCE;
            return playerEvent.getPlayer().getInstance() == INSTANCE;
        });

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();
            player.getInventory().clear();
            player.setHeldItemSlot((byte) 0);
            player.setItemInMainHand(SPLEEF_ITEM);
            BOARD.addPlayer(player);
            BOARD.updateAll();
        });

        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }

    public UUID getUuid() {
        return INSTANCE.getUniqueId();
    }

    void start() {
        // TODO: 06.06.22 start countdown
    }

    private void shutdown() {
        INSTANCE.getPlayers().forEach(player -> player.setInstance(Lobby.LOBBY_CONTAINER, Lobby.SPAWN));
        BOARD.removeAll();
        MinecraftServer.getInstanceManager().unregisterInstance(INSTANCE);
    }

    public enum Status {
        QUEUE_TIMEOUT,
        WAIT_FOR_PLAYERS,
        PRE_START_COUNTDOWN,
        IN_PROGRESS,
        SHOW_RESULTS,
        FINISHED
    }
}
