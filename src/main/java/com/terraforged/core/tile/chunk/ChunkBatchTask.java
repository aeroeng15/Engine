package com.terraforged.core.tile.chunk;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.batch.BatchTask;
import com.terraforged.core.tile.Tile;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.Rivermap;

public class ChunkBatchTask implements BatchTask {

    private final int x;
    private final int z;
    private final int size;
    private final Tile tile;
    private final Heightmap heightmap;

    private BatchTask.Notifier notifier = BatchTask.NONE;

    public ChunkBatchTask(int x, int z, int size, Tile tile, Heightmap heightmap) {
        this.heightmap = heightmap;
        this.tile = tile;
        this.x = x;
        this.z = z;
        this.size = size;
    }

    @Override
    public void setNotifier(BatchTask.Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void run() {
        try {
            drive();
        } finally {
            notifier.markDone();
        }
    }

    private void drive() {
        for (int dz = 0; dz < size; dz++) {
            int cz = z + dz;
            if (cz > tile.getChunkSize().total) {
                continue;
            }

            for (int dx = 0; dx < size; dx++) {
                int cx = x + dx;
                if (cx > tile.getChunkSize().total) {
                    continue;
                }

                try {
                    driveOne(tile.getChunkWriter(cx, cz), heightmap);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    protected void driveOne(ChunkWriter chunk, Heightmap heightmap) {
        Rivermap rivers = null;
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                Cell cell = chunk.genCell(dx, dz);
                float x = chunk.getBlockX() + dx;
                float z = chunk.getBlockZ() + dz;

                // apply continental noise & initial landmass
                heightmap.applyBase(cell, x, z);

                // apply river map for continent at cell's position
                rivers = Rivermap.get(cell, rivers, heightmap);
                heightmap.applyRivers(cell, x, z, rivers);

                // apply climate noise
                heightmap.applyClimate(cell, x, z);
            }
        }
    }

    public static class Zoom extends ChunkBatchTask {

        private final float translateX;
        private final float translateZ;
        private final float zoom;

        public Zoom(int x, int z, int size, Tile tile, Heightmap heightmap, float translateX, float translateZ, float zoom) {
            super(x, z, size, tile, heightmap);
            this.translateX = translateX;
            this.translateZ = translateZ;
            this.zoom = zoom;
        }

        @Override
        protected void driveOne(ChunkWriter chunk, Heightmap heightmap) {
            Rivermap rivers = null;
            for (int dz = 0; dz < 16; dz++) {
                for (int dx = 0; dx < 16; dx++) {
                    Cell cell = chunk.genCell(dx, dz);
                    float x = ((chunk.getBlockX() + dx) * zoom) + translateX;
                    float z = ((chunk.getBlockZ() + dz) * zoom) + translateZ;

                    // apply continental noise & initial landmass
                    heightmap.applyBase(cell, x, z);

                    // apply river map for continent at cell's position
                    rivers = Rivermap.get(cell, rivers, heightmap);
                    heightmap.applyRivers(cell, x, z, rivers);

                    // apply climate noise
                    heightmap.applyClimate(cell, x, z);
                }
            }
        }
    }
}
