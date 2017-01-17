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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@ConfigSerializable
public class OreConfig {

    private static final TypeToken<OreConfig> TYPE = TypeToken.of(OreConfig.class);
    static {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TraitList.class), new TraitListSerializer());
    }

    private final ConfigurationLoader<?> loader;
    private final ConfigurationNode node;
    @Setting("ores") private List<OreEntry> oreEntries;

    private OreConfig(ConfigurationLoader<?> loader, ConfigurationNode node) {
        this.loader = loader;
        this.node = node;
    }

    public static OreConfig fromLoader(ConfigurationLoader<?> loader) throws IOException {
        ConfigurationNode node = loader.load();
        ConfigurationNode fallbackConfig;
        try {
            fallbackConfig = loadDefaultConfiguration();
        }
        catch (IOException e) {
            throw new Error("Default configuration could not be loaded!");
        }
        node.mergeValuesFrom(fallbackConfig);

        OreConfig config = new OreConfig(loader, node);
        config.load();
        return config;
    }

    private void load() throws IOException {
        try {
            ObjectMapper.forObject(this).populate(node);
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
        loader.save(node);
    }

    public void save() throws IOException {
        try {
            ObjectMapper.forObject(this).serialize(node);
        } catch (ObjectMappingException e) {
            throw new IOException(e);
        }
        loader.save(node);
    }

    public List<OreEntry> getOreEntries() {
        return oreEntries;
    }

    public static ConfigurationNode loadDefaultConfiguration() throws IOException {
        URL defaultConfig = OreConfig.class.getResource("default.conf");
        if (defaultConfig == null) {
            throw new Error("Default config is not present in jar.");
        }
        HoconConfigurationLoader fallbackLoader = HoconConfigurationLoader.builder().setURL(defaultConfig).build();
        return fallbackLoader.load();
    }

}
