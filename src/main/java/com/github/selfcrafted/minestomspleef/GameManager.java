package com.github.selfcrafted.minestomspleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GameManager {
    private static final Inventory[] PLAY_MENU = {
            new Inventory(InventoryType.CHEST_6_ROW, Component.text("Play#0")),
            new Inventory(InventoryType.CHEST_6_ROW, Component.text("Play#1"))
    };
    private static final Inventory[] SPECTATE_MENU = {
            new Inventory(InventoryType.CHEST_6_ROW, Component.text("Spectate#0")),
            new Inventory(InventoryType.CHEST_6_ROW, Component.text("Spectate#1"))
    };

    private static final AtomicInteger displayInventoryIndex = new AtomicInteger(0);
    private static final AtomicInteger renderInventoryIndex = new AtomicInteger(1);
    private static final Map<UUID, GameInstance> games = new HashMap<>();
    private static final Map<UUID, ItemStack> gameToIcon = new HashMap<>();
    private static final Map<ItemStack, UUID> iconToGame = new HashMap<>();

    static {
        var playNode = EventNode.event("playMenu", EventFilter.INVENTORY,
                inventoryEvent -> inventoryEvent.getInventory() == PLAY_MENU[displayInventoryIndex.get()]
        );
        var spectateNode = EventNode.event("spectateMenu", EventFilter.INVENTORY,
                inventoryEvent -> inventoryEvent.getInventory() == SPECTATE_MENU[displayInventoryIndex.get()]
        );

        playNode.addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);
            var player = event.getPlayer();
            var game = iconToGame.get(event.getClickedItem());
            join(player, game);
        });

        spectateNode.addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);
            var player = event.getPlayer();
            var game = iconToGame.get(event.getClickedItem());
            spectate(player, game);
        });

        var eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addChild(playNode);
        eventHandler.addChild(spectateNode);
    }

    public static UUID createGame(Player creator, int players) {
        var game = new GameInstance(creator, players);
        var uuid = game.getUuid();
        games.put(uuid, game);
        updateMenus(uuid);
        return uuid;
    }

    public static void join(Player player, UUID game) {
        var instance = MinecraftServer.getInstanceManager().getInstance(game);
        if (instance == null) return;
        player.setInstance(instance, Server.ArenaGenerator.START.add(0.5, 1, 0.5));
        updateMenus(game);
    }

    public static void spectate(Player player, UUID game) {

    }

    public static void leave(Player player) {
        var instance = player.getInstance();
        if (instance == null) return;
        var uuid = instance.getUniqueId();
        var game = games.get(uuid);
        game.leave(player);
        Lobby.join(player);
        updateMenus(uuid);
    }

    private static void updateMenus(UUID uuid) {
        var game = games.get(uuid);
        if (game == null) {
            // remove icon from maps and inventories
            var icon = gameToIcon.get(uuid);
            iconToGame.remove(icon);
            gameToIcon.remove(uuid);
            PLAY_MENU[renderInventoryIndex.get()].takeItemStack(icon, TransactionOption.ALL);
            SPECTATE_MENU[renderInventoryIndex.get()].takeItemStack(icon, TransactionOption.ALL);
        } else {
            // update icon
            var icon = generateGameIcon(game);
            var oldIcon = gameToIcon.get(uuid);
            gameToIcon.put(uuid, icon);
            iconToGame.remove(oldIcon);
            iconToGame.put(icon, uuid);
            // redraw inventories
            for (UUID gameId : games.keySet()) {
                var currentGame = games.get(gameId);
                var currentIcon = gameToIcon.get(gameId);

                // TODO: 15.06.22 sort the items
                if (!currentGame.isFull()) PLAY_MENU[renderInventoryIndex.get()].addItemStack(currentIcon);
                SPECTATE_MENU[renderInventoryIndex.get()].addItemStack(currentIcon);
            }
        }
        swapInventories();
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

    private static void swapInventories() {
        renderInventoryIndex.set((byte) ((byte) (renderInventoryIndex.get()+1) % 2));
        displayInventoryIndex.set((byte) ((byte) (displayInventoryIndex.get()+1) % 2));
        PLAY_MENU[renderInventoryIndex.get()].getViewers().forEach(p -> p.openInventory(PLAY_MENU[displayInventoryIndex.get()]));
        PLAY_MENU[renderInventoryIndex.get()].clear();
        SPECTATE_MENU[renderInventoryIndex.get()].getViewers().forEach(p -> p.openInventory(SPECTATE_MENU[displayInventoryIndex.get()]));
        SPECTATE_MENU[renderInventoryIndex.get()].clear();
    }

    public static void openPlayMenu(Player player) {
        player.openInventory(PLAY_MENU[displayInventoryIndex.get()]);
    }

    public static void openSpectateMenu(Player player) {
        player.openInventory(SPECTATE_MENU[displayInventoryIndex.get()]);
    }
}
