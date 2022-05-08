package com.github.selfcrafted.minestomspleef;

import com.sqcred.sboards.SBoard;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SpleefGame {
    private static final ItemStack SPLEEF_ITEM = ItemStack.builder(Material.IRON_SHOVEL)
            .displayName(Component.text("SPLEEF"))
            .meta(builder -> builder.canDestroy(Block.SAND).enchantment(Enchantment.EFFICIENCY, (short) 5).build())
            .build();
    private final Set<Player> players = new HashSet<>();

    private SBoard BOARD;
    private final InstanceContainer INSTANCE = MinecraftServer.getInstanceManager().createInstanceContainer();

    void addPlayer(Player player) { players.add(player); }
    void removePlayer(Player player) { players.remove(player); }

    void start() {
        var generator = new Server.ArenaGenerator(players.size()+1);
        INSTANCE.setGenerator(generator);
        INSTANCE.setChunkLoader(generator);
        INSTANCE.setTime(-6000);
        INSTANCE.setTimeRate(0);
        INSTANCE.setTimeUpdate(null);

        BOARD = new SBoard(
                (player) -> Component.text(INSTANCE.getUniqueId().toString()),
                (player) -> Arrays.asList(
                        Component.text("Time left: 05:00"),
                        Component.text("================"),
                        Component.text(player.getUsername())
                )
        );

        players.forEach(player -> {
            player.setInstance(INSTANCE, Server.ArenaGenerator.START.add(0.5, 1, 0.5));
            player.getInventory().clear();
            player.setHeldItemSlot((byte) 0);
            player.setItemInMainHand(SPLEEF_ITEM);
            BOARD.addPlayer(player);
        });

        BOARD.updateAll();

        // TODO: 02.05.22 countdown

    }

    private void shutdown() {
        INSTANCE.getPlayers().forEach(player -> {
            player.setInstance(Lobby.LOBBY_CONTAINER, Lobby.SPAWN);
            BOARD.removePlayer(player);
        });
        MinecraftServer.getInstanceManager().unregisterInstance(INSTANCE);
    }
}
