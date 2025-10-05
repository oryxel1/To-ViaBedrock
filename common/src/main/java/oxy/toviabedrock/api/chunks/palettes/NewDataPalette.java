package oxy.toviabedrock.api.chunks.palettes;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import oxy.toviabedrock.api.chunks.BaseDataPalette;
import oxy.toviabedrock.api.chunks.bitarray.BitArray;
import oxy.toviabedrock.api.chunks.bitarray.BitArrayVersion;

public class NewDataPalette implements BaseDataPalette<NewDataPalette> {
    private final IntList palette;
    private BitArray bitArray;

    public NewDataPalette(int airBlockId) {
        this(airBlockId, BitArrayVersion.V2);
    }

    public NewDataPalette(int airBlockId, BitArrayVersion version) {
        this.bitArray = version.createArray(4096);
        this.palette = new IntArrayList(16);
        this.palette.add(airBlockId);
    }

    public NewDataPalette(BitArray bitArray, IntList palette) {
        this.palette = palette;
        this.bitArray = bitArray;
    }

    @Override
    public int get(int index) {
        if (this.palette.isEmpty()) {
            return -1;
        }

        return this.palette.getInt(this.bitArray.get(index));
    }

    @Override
    public void set(int index, int id) {
        try {
            this.bitArray.set(index, this.idFor(id));
        } catch (Exception ignored) {}
    }

    @Override
    public NewDataPalette copy() {
        return new NewDataPalette(this.bitArray.copy(), new IntArrayList(this.palette));
    }

    private int idFor(int runtimeId) {
        int index = this.palette.indexOf(runtimeId);
        if (index != -1) {
            return index;
        }

        index = this.palette.size();
        this.palette.add(runtimeId);
        BitArrayVersion version = this.bitArray.getVersion();
        if (index > version.getMaxEntryValue()) {
            BitArrayVersion next = version.next();
            if (next != null) {
                this.onResize(next);
            }
        }
        return index;
    }

    private void onResize(BitArrayVersion version) {
        BitArray newBitArray = version.createArray(4096);

        for (int i = 0; i < 4096; i++) {
            newBitArray.set(i, this.bitArray.get(i));
        }
        this.bitArray = newBitArray;
    }
}
