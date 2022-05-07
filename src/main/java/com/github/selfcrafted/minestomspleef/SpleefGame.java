package com.github.selfcrafted.minestomspleef;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.HashSet;
import java.util.Set;

public class SpleefGame {
    private static final ItemStack SPLEEF_ITEM = ItemStack.builder(Material.IRON_SHOVEL)
            .displayName(Component.text("SPLEEF"))
            .meta(builder -> builder.canDestroy(Block.SAND).enchantment(Enchantment.EFFICIENCY, (short) 5).build())
            .build();
    private final Set<Player> players = new HashSet<>();

    void addPlayer(Player player) { players.add(player); }
    void removePlayer(Player player) { players.remove(player); }

    void start() {
        var manager =  MinecraftServer.getInstanceManager();
        var instanceContainer = manager.createInstanceContainer();
        manager.registerInstance(instanceContainer);
        var generator = new Server.ArenaGenerator(players.size()+1);
        instanceContainer.setGenerator(generator);
        instanceContainer.setChunkLoader(generator);
        instanceContainer.setTime(-6000);
        instanceContainer.setTimeRate(0);
        instanceContainer.setTimeUpdate(null);

        players.forEach(player -> {
            player.setInstance(instanceContainer, Server.ArenaGenerator.START.add(0.5, 1, 0.5));
            player.getInventory().clear();
            player.setHeldItemSlot((byte) 0);
            player.setItemInMainHand(SPLEEF_ITEM);
        });

        // TODO: 02.05.22 countdown

        MinecraftServer.getInstanceManager().unregisterInstance(instanceContainer);
    }
}
