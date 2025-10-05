package oxy.toviabedrock.api.chunks.palettes;

import lombok.Getter;
import oxy.toviabedrock.api.chunks.BaseDataPalette;
import oxy.toviabedrock.api.chunks.NibbleArray;

// Based on VB code :) (https://github.com/RaphiMC/ViaBedrock/blob/main/src/main/java/net/raphimc/viabedrock/api/chunk/datapalette/BedrockBlockArray.java).
@Getter
public class OlderDataPalette implements BaseDataPalette<OlderDataPalette> {
    private final byte[] blocks;
    private final NibbleArray data;

    public OlderDataPalette() {
        this.blocks = new byte[4096];
        this.data = new NibbleArray(this.blocks.length);
    }

    public OlderDataPalette(final byte[] blocks, final NibbleArray data) {
        this.blocks = blocks;
        this.data = data;
    }

    @Override
    public int get(int index) {
        return (this.blocks[index] & 255) << 4 | this.data.get(index);
    }

    @Override
    public void set(int index, int id) {
        if (id >> 4 > 255) {
            throw new IllegalArgumentException("Too large block id: " + id);
        }

        this.blocks[index] = (byte) (id >> 4);
        this.data.set(index, id & 15);
    }

    @Override
    public OlderDataPalette copy() {
        return new OlderDataPalette(this.blocks, new NibbleArray(data.getHandle()));
    }
}
