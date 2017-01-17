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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.helion3.orealerts.config.OreConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;

@Plugin(id = "orealerts", name = "OreAlerts", description = "Ore alert plugin for Sponge servers.", version = "1.0")
final public class OreAlerts {

    public static OreConfig config;
    public static Map<Location<World>, Long> recentLocations = new ConcurrentHashMap<>();
    @Inject @ConfigDir(sharedRoot = true) private Path configDir;
    @Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject private Game game;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        try {
            Files.createDirectories(configDir);
            config = OreConfig.fromLoader(configLoader);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while enabling OreAlerts!", e);
        }
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        game.getEventManager().registerListeners(this, new ChangeBlockBreakListener());
        game.getScheduler().createTaskBuilder()
                .async()
                .delay(5L, TimeUnit.MINUTES)
                .interval(5L, TimeUnit.MINUTES)
                .execute(() -> {
                    long time = new Date().getTime();
                    for (Entry<Location<World>, Long> entry : recentLocations.entrySet()) {
                        long diff = (time - entry.getValue()) / 1000;
                        if (diff >= 300) {
                            recentLocations.remove( entry.getKey() );
                        }
                    }
                }).submit(this);
    }

}
