package com.github.selfcrafted.minestomspleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameManager {
    static final Inventory PLAY_MENU = new Inventory(InventoryType.CHEST_6_ROW, Component.text("Play"));
    static final Inventory SPECTATE_MENU = new Inventory(InventoryType.CHEST_6_ROW, Component.text("Spectate"));
    private static final Map<UUID, GameInstance> games = new HashMap<>();

    public static UUID createGame(Player creator, int players) {
        var game = new GameInstance(creator, players);
        var uuid = game.getUuid();
        games.put(uuid, game);
        // TODO: 14.06.22 check if game is full, if so only add it to spectator menu
        var icon = generateGameIcon(game);
        PLAY_MENU.addItemStack(icon);
        SPECTATE_MENU.addItemStack(icon);
        return uuid;
    }

    public static void join(Player player, UUID game) {
        var instance = MinecraftServer.getInstanceManager().getInstance(game);
        if (instance == null) return;
        player.setInstance(instance, Server.ArenaGenerator.START.add(0.5, 1, 0.5));
    }

    public static void spectate(Player player, UUID game) {

    }

    public static void leave(Player player) {
        Lobby.join(player);
    }

    private static ItemStack generateGameIcon(GameInstance game) {
        var players = game.getPlayers();
        var playerAmount = game.getPlayerAmount();

        var fillRatio = players.size()/playerAmount;
        var material = Material.WOODEN_SHOVEL;
        var color = NamedTextColor.RED;
        var lore = new ArrayList<Component>();
        lore.add(Component.text("Players: %d/%d".formatted(players.size(), playerAmount)));
        lore.addAll(players.stream().map(Player::getName).collect(Collectors.toSet()));
        if (fillRatio > 0.8) {
            material = Material.GOLDEN_SHOVEL;
            color = NamedTextColor.GREEN;
        }
        else if (fillRatio > 0.5) {
            material = Material.IRON_SHOVEL;
            color = NamedTextColor.YELLOW;
        }
        else if (fillRatio > 0.25) {
            material = Material.STONE_SHOVEL;
            color = NamedTextColor.AQUA;
        }
        return ItemStack.builder(material)
                .displayName(game.getCreator().getName().append(Component.text("'s Game"))
                        .color(color).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .build();
    }
}
