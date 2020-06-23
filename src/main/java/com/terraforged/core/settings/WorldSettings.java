package com.terraforged.core.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import com.terraforged.n2d.func.DistanceFunc;
import com.terraforged.world.continent.ContinentMode;
import com.terraforged.world.continent.SpawnType;
import com.terraforged.world.heightmap.Heightmap;

@Serializable
public class WorldSettings {

    public transient long seed = 0L;

    public Continent continent = new Continent();

    public TransitionPoints transitionPoints = new TransitionPoints();

    public Properties properties = new Properties();

    @Serializable
    public static class Continent {

        @Comment("Controls the continent generator type")
        public ContinentMode continentMode = ContinentMode.MULTI;

        @Comment("Controls how continent shapes are calculated")
        public DistanceFunc continentShape = DistanceFunc.EUCLIDEAN;

        @Range(min = 0F, max = 1F)
        @Comment("Controls the amount of ocean between continents")
        public float oceanScale = 0.8F;

        @Range(min = 100, max = 10000)
        @Comment("Controls the size of continents")
        public int continentScale = 3000;
    }

    @Serializable
    public static class TransitionPoints {

        @Range(min = 0F, max = 1F)
        @Comment("The deep ocean transition point")
        public float deepOcean = Heightmap.DEEP_OCEAN_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment("The shallow ocean transition point")
        public float shallowOcean = Heightmap.OCEAN_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment("The beach transition point")
        public float beach = Heightmap.BEACH_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment("The coast transition point")
        public float coast = Heightmap.COAST_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment("The inland transition point")
        public float inland = Heightmap.INLAND_VALUE;
    }

    @Serializable
    public static class Properties {

        @Comment("Set whether spawn should be close to x=0,z=0 or the centre of the nearest continent")
        public SpawnType spawnType = SpawnType.CONTINENT_CENTER;

        @Range(min = 0, max = 256)
        @Comment("Controls the world height")
        public int worldHeight = 256;

        @Range(min = 0, max = 255)
        @Comment("Controls the sea level")
        public int seaLevel = 63;
    }
}
