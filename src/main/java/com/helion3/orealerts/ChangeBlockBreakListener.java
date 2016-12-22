/**
 * This file is part of OreAlerts, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Helion3 http://helion3.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.helion3.orealerts;

import java.util.Date;
import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.LightEmissionProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

final public class ChangeBlockBreakListener {
    private final MessageChannel channel = MessageChannel.permission("orealerts.receive");

    @Listener
    public void onChangeBlock(final ChangeBlockEvent.Break event) {
        Optional<Player> optionalPlayer = event.getCause().first(Player.class);

        // Must be player-driven
        if (!optionalPlayer.isPresent()) {
            return;
        }

        Player player = optionalPlayer.get();

        // Skip creative players
        Optional<GameMode> gameMode = player.getGameModeData().get(Keys.GAME_MODE);
        if (gameMode.isPresent() && gameMode.get().equals(GameModes.CREATIVE)) {
            return;
        }

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            // Skip if we've alerted this
            if (OreAlerts.recentLocations.containsKey(transaction.getOriginal().getLocation().get())) {
                continue;
            }

            // Skip if there was a creator
            if (transaction.getOriginal().getCreator().isPresent()) {
                continue;
            }

            BlockType type = transaction.getOriginal().getState().getType();

            if (isWatchedOre(type)) {
                // Count blocks in this vein
                int matchCount = findNeighborBlocks(transaction.getOriginal()) + 1;

                // Get lighting condition
                Optional<GroundLuminanceProperty> lightOptional = transaction.getOriginal().getLocation().get().getProperty(GroundLuminanceProperty.class);

                if(lightOptional.isPresent()) {
                    int emittedLight = (int) Math.floor((lightOptional.get().getValue() / 15D) * 100);

                    // Message with lighting emission
                    channel.send(Text.of(getColor(type), "[Ore] ", player.getName(), " found " + matchCount + " ", getNiceName(type), "in ", emittedLight, "% light."));
                } else {
                    // Message
                    channel.send(Text.of(getColor(type), "[Ore] ", player.getName(), " found " + matchCount + " ", getNiceName(type)));
                }
            }
         }
    }

    private Text getNiceName(BlockType type) {
        return Text.of(type.getId().replaceAll("minecraft:", "").replace("_ore", " "));
    }

    private TextColor getColor(BlockType type) {
        TextColor color = TextColors.WHITE;

        if (type.equals(BlockTypes.IRON_ORE)) {
            color = TextColors.GRAY;
        }
        else if (type.equals(BlockTypes.LAPIS_ORE)) {
            color = TextColors.BLUE;
        }
        else if (type.equals(BlockTypes.GOLD_ORE)) {
            color = TextColors.YELLOW;
        }
        else if (type.equals(BlockTypes.EMERALD_ORE)) {
            color = TextColors.GREEN;
        }
        else if (type.equals(BlockTypes.DIAMOND_ORE)) {
            color = TextColors.AQUA;
        }

        return color;
    }

    private boolean isWatchedOre(BlockType type) {
        return (type.equals(BlockTypes.IRON_ORE) ||
                type.equals(BlockTypes.GOLD_ORE) ||
                type.equals(BlockTypes.DIAMOND_ORE) ||
                type.equals(BlockTypes.EMERALD_ORE) ||
                type.equals(BlockTypes.LAPIS_ORE));
    }

    private int findNeighborBlocks(BlockSnapshot snapshot) {
        int matchCount = 0;
        Location<World> location = snapshot.getLocation().get();

        if (OreAlerts.recentLocations.containsKey(location)) {
            return matchCount;
        }

        OreAlerts.recentLocations.put(snapshot.getLocation().get(), new Date().getTime());

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    Location<World> neighborLocation = location.add(x, y, z);
                    BlockSnapshot neighbor = neighborLocation.getBlock().snapshotFor(neighborLocation);

                    if (neighbor.getState().getType().equals(snapshot.getState().getType()) && !OreAlerts.recentLocations.containsKey(neighborLocation)) {
                        matchCount++;
                        if (matchCount < 30) {
                            matchCount += findNeighborBlocks(neighbor);
                        }
                    }
                }
            }
        }

        return matchCount;
    }
}
