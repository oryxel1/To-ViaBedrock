package oxy.toviabedrock.utils.chunk;

public enum BitArrayVersion {
    V16(16, 2),
    V8(8, 4),
    V6(6, 5), // 2 bit padding
    V5(5, 6), // 2 bit padding
    V4(4, 8),
    V3(3, 10), // 2 bit padding
    V2(2, 16),
    V1(1, 32),
    V0(0, 0);

    public final byte bits;
    public final byte entriesPerWord;
    public final int maxEntryValue;

    BitArrayVersion(int bits, int entriesPerWord) {
        this.bits = (byte) bits;
        this.entriesPerWord = (byte) entriesPerWord;
        this.maxEntryValue = (1 << this.bits) - 1;
    }

    public static BitArrayVersion get(int version, boolean read) {
        for (BitArrayVersion ver : values()) {
            if ((!read && ver.entriesPerWord <= version) || (read && ver.bits == version)) {
                return ver;
            }
        }
        throw new IllegalArgumentException("Invalid palette version: " + version);
    }
}