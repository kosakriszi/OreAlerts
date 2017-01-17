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
package com.helion3.orealerts.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;

@ConfigSerializable
public class OreEntry {

    @Setting private String id;
    @Setting private String color;
    @Setting private String name;
    @Setting private TraitList traits;

    public OreEntry() {
    }

    public TextColor getColor() {
        return Sponge.getRegistry().getType(TextColor.class, color.toUpperCase()).orElse(TextColors.GRAY);
    }

    public String getName() {
        return name;
    }

    public boolean equals(BlockState blockState) {
        String incomingId = blockState.getId();
        // MIGHT BE A BIG BUG BOY.
        if (incomingId.contains("[")) {
            incomingId = incomingId.split("\\[")[0];
        }
        // Return false immediately if the state ID doesn't match ours.
        if (!id.equals(incomingId)) {
            return false;
        }
        for (Map.Entry<BlockTrait<?>, ?> entry : blockState.getTraitMap().entrySet()) {
            String key = entry.getKey().getName();
            Object value = entry.getValue();
            // Continue to next cycle if a trait present on the state isn't present here.
            if (!traits.containsKey(key)) {
                continue;
            }
            Object present = traits.get(key);
            // Lowercase string values for ease of comparision.
            if (present instanceof String) {
                present = ((String) present).toLowerCase();
            }

            // Convert enum values to strings.
            if (value instanceof Enum) {
                value = ((Enum) value).name().toLowerCase();
            }

            if (!present.equals(value)) {
                return false;
            }
        }
        return true;
    }

}
