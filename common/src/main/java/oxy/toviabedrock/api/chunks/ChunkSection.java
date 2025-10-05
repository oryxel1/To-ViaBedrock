package oxy.toviabedrock.api.chunks;

import lombok.Getter;
import org.cloudburstmc.protocol.common.util.Preconditions;

import java.util.List;

public class ChunkSection {
    @Getter
    private final List<BaseDataPalette<?>> dataPalettes;

    public ChunkSection(BaseDataPalette<?>[] dataPalettes) {
        this.dataPalettes = List.of(dataPalettes);
    }

    public int get(int x, int y, int z, int layer) {
        Preconditions.checkElementIndex(layer, this.dataPalettes.size());
        return this.dataPalettes.get(layer).get(index(x, y, z));
    }

    public void set(int x, int y, int z, int layer, int id) {
        Preconditions.checkElementIndex(layer, this.dataPalettes.size());
        this.dataPalettes.get(layer).set(index(x, y, z), id);
    }

    private static int index(int x, int y, int z) {
        return (x << 8) | (z << 4) | y;
    }
}
