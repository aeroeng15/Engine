/*
 *   
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.world.terrain.provider;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.settings.TerrainSettings;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.RegionConfig;
import com.terraforged.world.terrain.LandForms;
import com.terraforged.world.terrain.MixedTerarin;
import com.terraforged.world.terrain.Terrain;
import com.terraforged.world.terrain.populator.TerrainPopulator;
import com.terraforged.world.terrain.special.VolcanoPopulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class StandardTerrainProvider implements TerrainProvider {

    private final List<TerrainPopulator> mixable = new ArrayList<>();
    private final List<TerrainPopulator> unmixable = new ArrayList<>();
    private final Map<Terrain, List<Populator>> populators = new HashMap<>();

    private final LandForms landForms;
    private final RegionConfig config;
    private final TerrainSettings settings;
    private final GeneratorContext context;
    private final Populator defaultPopulator;

    public StandardTerrainProvider(GeneratorContext context, RegionConfig config, Populator defaultPopulator) {
        this.config = config;
        this.context = context;
        this.settings = context.settings.terrain;
        this.landForms = new LandForms(context.settings.terrain, context.levels);
        this.defaultPopulator = defaultPopulator;
        init();
    }

    public void init() {
        registerMixable(context.terrain.dales, landForms.getLandBase(), landForms.dales(context.seed), settings.dales);
        registerMixable(context.terrain.hills, landForms.getLandBase(), landForms.hills1(context.seed), settings.hills);
        registerMixable(context.terrain.hills, landForms.getLandBase(), landForms.hills2(context.seed), settings.hills);
        registerMixable(context.terrain.steppe, landForms.getLandBase(), landForms.steppe(context.seed), settings.steppe);
        registerMixable(context.terrain.plains, landForms.getLandBase(), landForms.plains(context.seed), settings.plains);
        registerMixable(context.terrain.plateau, landForms.getLandBase(), landForms.plateau(context.seed), settings.plateau);
        registerMixable(context.terrain.badlands, landForms.getLandBase(), landForms.badlands(context.seed), settings.badlands);
        registerMixable(context.terrain.torridonian, landForms.getLandBase(), landForms.torridonian(context.seed), settings.torridonian);

        registerUnMixable(context.terrain.badlands, landForms.getLandBase(), landForms.badlands(context.seed), settings.badlands);
        registerUnMixable(context.terrain.mountains, landForms.getLandBase(), landForms.mountains(context.seed), settings.mountains);
        registerUnMixable(context.terrain.mountains, landForms.getLandBase(), landForms.mountains2(context.seed), settings.mountains);
        registerUnMixable(context.terrain.mountains, landForms.getLandBase(), landForms.mountains3(context.seed), settings.mountains);
        registerUnMixable(new VolcanoPopulator(context.seed, config, context.levels, context.terrain));
    }

    @Override
    public void registerMixable(TerrainPopulator populator) {
        populators.computeIfAbsent(populator.getType(), t -> new ArrayList<>()).add(populator);
        mixable.add(populator);
    }

    @Override
    public void registerUnMixable(TerrainPopulator populator) {
        populators.computeIfAbsent(populator.getType(), t -> new ArrayList<>()).add(populator);
        unmixable.add(populator);
    }

    @Override
    public int getVariantCount(Terrain terrain) {
        List<Populator> list = populators.get(terrain);
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public Populator getPopulator(Terrain terrain, int variant) {
        if (variant < 0) {
            return defaultPopulator;
        }

        List<Populator> list = populators.get(terrain);
        if (list == null) {
            return defaultPopulator;
        }

        if (variant >= list.size()) {
            variant = list.size() - 1;
        }

        return list.get(variant);
    }

    @Override
    public LandForms getLandforms() {
        return landForms;
    }

    @Override
    public List<Populator> getPopulators() {
        List<TerrainPopulator> mixed = combine(getMixable(mixable), this::combine);
        List<Populator> result = new ArrayList<>(mixed.size() + unmixable.size());
        result.addAll(mixed);
        result.addAll(unmixable);
        return result;
    }

    public List<TerrainPopulator> getTerrainPopulators() {
        List<TerrainPopulator> populators = new ArrayList<>();
        populators.addAll(mixable);
        populators.addAll(unmixable);
        return populators;
    }

    protected GeneratorContext getContext() {
        return context;
    }

    private TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2) {
        return combine(tp1, tp2, context.seed, config.scale / 2);
    }

    private TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2, Seed seed, int scale) {
        Terrain type = new MixedTerarin(tp1.getType(), tp2.getType());

        Module combined = Source.perlin(seed.next(), scale, 1)
                .warp(seed.next(), scale / 2, 2, scale / 2D)
                .blend(tp1.getVariance(), tp2.getVariance(), 0.5, 0.25)
                .clamp(0, 1);

        return new TerrainPopulator(type, landForms.getLandBase(), combined);
    }

    private static <T> List<T> combine(List<T> input, BiFunction<T, T, T> operator) {
        int length = input.size();
        for (int i = 1; i < input.size(); i++) {
            length += (input.size() - i);
        }

        List<T> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            result.add(null);
        }

        for (int i = 0, k = input.size(); i < input.size(); i++) {
            T t1 = input.get(i);
            result.set(i, t1);
            for (int j = i + 1; j < input.size(); j++, k++) {
                T t2 = input.get(j);
                T t3 = operator.apply(t1, t2);
                result.set(k, t3);
            }
        }

        return result;
    }

    private static List<TerrainPopulator> getMixable(List<TerrainPopulator> input) {
        List<TerrainPopulator> output = new ArrayList<>(input.size());
        for (TerrainPopulator populator : input) {
            if (populator.getType().getWeight() > 0) {
                output.add(populator);
            }
        }
        return output;
    }
}
