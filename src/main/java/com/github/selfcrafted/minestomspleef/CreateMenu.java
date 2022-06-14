package com.github.selfcrafted.minestomspleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.HashMap;
import java.util.Map;

public class CreateMenu {
    private static final Map<Player, CreateMenu> menus = new HashMap<>();
    private static final Map<Player, Inventory> inventories = new HashMap<>();
    private static final ItemStack MORE_PLAYERS_BUTTON = ItemStack.builder(Material.GREEN_DYE)
            .displayName(Component.text("More players", NamedTextColor.GREEN)).build();
    private static final ItemStack LESS_PLAYERS_BUTTON = ItemStack.builder(Material.RED_DYE)
            .displayName(Component.text("Less players", NamedTextColor.RED)).build();

    private static final ItemStack CREATE_BUTTON = ItemStack.builder(Material.ANVIL)
            .displayName(Component.text("CREATE", NamedTextColor.GOLD)).build();

    private static final ItemStack PLAYER_COUNT_LABEL = ItemStack.builder(Material.NAME_TAG)
            .displayName(Component.text("")).build();

    static {
        var eventNode = EventNode.event("createMenu", EventFilter.INVENTORY, event ->
                inventories.containsValue(event.getInventory()));

        // React to player inputs
        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);
            var player = event.getPlayer();
            var menu = menus.get(player);
            if (menu == null) return;
            if (event.getClickedItem() == MORE_PLAYERS_BUTTON) menu.addPlayer();
            else if (event.getClickedItem() == LESS_PLAYERS_BUTTON) menu.removePlayer();
            else if (event.getClickedItem() == CREATE_BUTTON) {
                var game = GameManager.createGame(player, menu.playerCount);
                event.getPlayer().closeInventory();
                menus.remove(player);
                inventories.remove(player);
                GameManager.join(player, game);
            }
        });

        // Cleanup when player aborts
        eventNode.addListener(InventoryCloseEvent.class, event -> {
            var player = event.getPlayer();
            menus.remove(player);
            inventories.remove(player);
        });

        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }

    private int playerCount = 2;
    final Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Create game"));

    CreateMenu(Player player) {
        menus.put(player, this);
        inventories.put(player, inventory);

        inventory.setItemStack(10, LESS_PLAYERS_BUTTON);
        inventory.setItemStack(12, PLAYER_COUNT_LABEL.withAmount(playerCount));
        inventory.setItemStack(14, MORE_PLAYERS_BUTTON);
        inventory.setItemStack(16, CREATE_BUTTON);
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void addPlayer() {
        if (playerCount == 16) return;
        playerCount += 1;
        inventory.setItemStack(12, PLAYER_COUNT_LABEL.withAmount(playerCount));
    }

    private void removePlayer() {
        if (playerCount == 2) return;
        playerCount -= 1;
        inventory.setItemStack(12, PLAYER_COUNT_LABEL.withAmount(playerCount));
    }
}
