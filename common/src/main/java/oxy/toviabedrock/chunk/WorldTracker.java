package oxy.toviabedrock.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import oxy.toviabedrock.api.chunks.ChunkSection;
import oxy.toviabedrock.api.dimension.BedrockDimension;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.UserStorage;
import oxy.toviabedrock.utils.MathUtils;

public class WorldTracker extends UserStorage {
    public static int INVALID_BLOCK_ID = -1;

    private final Long2ObjectMap<ChunkSection[]> chunks = new Long2ObjectOpenHashMap<>();
    @Setter @Getter
    private BedrockDimension dimension;

    public WorldTracker(UserSession session) {
        super(session);
    }

    public void put(int x, int z, ChunkSection[] chunks) {
        long chunkPosition = MathUtils.chunkPositionToLong(x, z);
        this.chunks.put(chunkPosition, chunks);
    }

    public ChunkSection[] get(int chunkX, int chunkZ) {
        long chunkPosition = MathUtils.chunkPositionToLong(chunkX, chunkZ);
        return this.chunks.getOrDefault(chunkPosition, null);
    }

    public void remove(int x, int z) {
        this.chunks.remove(MathUtils.chunkPositionToLong(x, z));
    }

    public void set(int x, int y, int z, int layer, int block) {
        final ChunkSection[] column = this.get(x >> 4, z >> 4);
        if (column == null) {
            return;
        }

        if (y < this.dimension.getMinY() || ((y - this.dimension.getMinY()) >> 4) > column.length - 1) {
            // Y likely goes above or below the height limit of this world
            return;
        }

        ChunkSection palette = column[(y - this.dimension.getMinY()) >> 4];
        if (palette == null) {
            return;
        }

        palette.set(x & 0xF, y & 0xF, z & 0xF, layer, block);
    }

    public int get(int x, int y, int z, int layer) {
        ChunkSection[] column = this.get(x >> 4, z >> 4);
        if (column == null) {
            return INVALID_BLOCK_ID;
        }

        if (y < this.dimension.getMinY() || ((y - this.dimension.getMinY()) >> 4) > column.length - 1) {
            // Y likely goes above or below the height limit of this world
            return INVALID_BLOCK_ID;
        }

        ChunkSection chunk = column[(y - this.dimension.getMinY()) >> 4];
        if (chunk != null) {
            try {
                return chunk.get(x & 0xF, y & 0xF, z & 0xF, layer);
            } catch (Exception e) {
                return INVALID_BLOCK_ID;
            }
        }

        return INVALID_BLOCK_ID;
    }

    public void clear() {
        this.chunks.clear();
    }
}
