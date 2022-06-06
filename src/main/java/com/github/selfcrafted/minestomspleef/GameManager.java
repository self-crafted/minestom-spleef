package com.github.selfcrafted.minestomspleef;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    private static final Map<UUID, GameInstance> games = new HashMap<>();

    public static UUID createGame(int players) {
        var game = new GameInstance(players);
        var uuid = game.getUuid();
        games.put(uuid, game);
        return uuid;
    }

    public static void join(Player player, UUID game) {
        var instance = MinecraftServer.getInstanceManager().getInstance(game);
        player.setInstance(instance, Server.ArenaGenerator.START.add(0.5, 1, 0.5));
    }

    public static void spectate(Player player, UUID game) {

    }

    public static void leave(Player player) {
        Lobby.join(player);
    }
}
