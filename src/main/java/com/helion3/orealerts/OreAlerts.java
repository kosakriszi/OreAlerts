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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;

@Plugin(id = "orealerts", name = "orealerts", version = "1.0")
final public class OreAlerts {
    private static Game game;
    public static Map<Location<World>, Long> recentLocations = new ConcurrentHashMap<Location<World>, Long>();

    /**
     * Performs bootstrapping of Prism resources/objects.
     *
     * @param event Server started
     */
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        expireCheckedLocations();

        game.getEventManager().registerListeners(this, new ChangeBlockBreakListener());
    }

    /**
     * Injected Game instance.
     * @param injectGame Game
     */
    @Inject
    public void setGame(Game injectGame) {
        game = injectGame;
    }

    /**
     * Remove expired recent locations
     */
    public void expireCheckedLocations(){
        game.getScheduler().createTaskBuilder()
            .async()
            .delay(5L, TimeUnit.MINUTES)
            .interval(5L, TimeUnit.MINUTES)
            .execute(new Runnable(){
                @Override
                public void run() {
                    long time = new Date().getTime();
                    for (final Entry<Location<World>, Long> entry : recentLocations.entrySet()) {
                        final long diff = (time - entry.getValue()) / 1000;
                        if (diff >= 300) {
                            recentLocations.remove( entry.getKey() );
                        }
                    }
                }
            }).submit(this);
    }
}
