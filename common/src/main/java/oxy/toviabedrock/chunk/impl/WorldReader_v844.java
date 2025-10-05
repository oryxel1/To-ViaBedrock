package oxy.toviabedrock.chunk.impl;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.SubChunkData;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.util.VarInts;
import oxy.toviabedrock.api.chunks.ChunkSection;
import oxy.toviabedrock.api.chunks.bitarray.BitArray;
import oxy.toviabedrock.api.chunks.bitarray.BitArrayVersion;
import oxy.toviabedrock.api.chunks.bitarray.SingletonBitArray;
import oxy.toviabedrock.api.chunks.palettes.NewDataPalette;
import oxy.toviabedrock.api.chunks.palettes.OlderDataPalette;
import oxy.toviabedrock.api.dimension.BedrockDimension;
import oxy.toviabedrock.base.definitions.MappedBlockDefinition;
import oxy.toviabedrock.chunk.WorldTracker;
import oxy.toviabedrock.chunk.base.WorldReaderBase;
import oxy.toviabedrock.session.UserSession;

public class WorldReader_v844 extends WorldReaderBase {
    public WorldReader_v844(UserSession session) {
        super(session);
    }

    @Override
    public void onClientbound(BedrockPacket packet) {
        if (packet instanceof StartGamePacket startGame) {
            this.session.get(WorldTracker.class).setDimension(BedrockDimension.dimensionFromId(startGame.getDimensionId()));
        }

        if (packet instanceof ChangeDimensionPacket dimensionPacket) {
            final WorldTracker tracker = this.session.get(WorldTracker.class);
            tracker.setDimension(BedrockDimension.dimensionFromId(dimensionPacket.getDimension()));
            tracker.clear();
        }

        if (packet instanceof LevelChunkPacket levelPacket) {
            this.readLevelChunk(levelPacket);
        }

        if (packet instanceof SubChunkPacket subChunkPacket) {
            this.readSubChunk(subChunkPacket);
        }

        if (packet instanceof UpdateBlockPacket blockPacket) {
            if (blockPacket.getDefinition() instanceof MappedBlockDefinition mapped) {
                this.session.get(WorldTracker.class).set(
                        blockPacket.getBlockPosition().getX(), blockPacket.getBlockPosition().getY(), blockPacket.getBlockPosition().getZ(),
                        blockPacket.getDataLayer(), mapped.oldId()
                );
            }
        }
    }

    protected void readLevelChunk(LevelChunkPacket packet) {
        final int subChunksCount = packet.getSubChunksLength();
        if (subChunksCount < -2 || packet.getDimension() < 0 || packet.getDimension() > 2) {
            return;
        }

        final WorldTracker tracker = this.session.get(WorldTracker.class);
//        if (subChunksCount == 0) {
//            tracker.remove(packet.getChunkX(), packet.getChunkZ());
//            return;
//        }

        final ChunkSection[] sections = new ChunkSection[tracker.getDimension().getHeight() >> 4];

        final ByteBuf buffer = packet.getData().retainedDuplicate();
        try {
            for (int i = 0; i < subChunksCount; i++) {
                final byte version = buffer.readByte();
                switch (version) {
                    case 0, 2, 3, 4, 5, 6, 7 -> sections[i] = new ChunkSection(new OlderDataPalette[] {readV0Section(buffer)});
                    case 1 -> sections[i] = new ChunkSection(new NewDataPalette[] {readBlockPalette(buffer)});
                    case 8, 9 -> sections[i] = new ChunkSection(readV9_8Section(version, buffer));
                }
            }
            // I only need those, no need to read the rest lol.
        } catch (Exception ignored) {
        } finally {
            buffer.release();
        }

        tracker.put(packet.getChunkX(), packet.getChunkZ(), sections);
    }

    protected void readSubChunk(SubChunkPacket packet) {
        final WorldTracker tracker = this.session.get(WorldTracker.class);

        for (SubChunkData chunkData : packet.getSubChunks()) {
            final Vector3i position = packet.getCenterPosition().add(chunkData.getPosition());
            final ChunkSection[] chunk = tracker.get(position.getX(), position.getZ());
            if (chunk == null) {
//                System.out.println("Can't find chunk lol: " + position);
                continue;
            }

            final ChunkSection section = chunk[position.getY() + Math.abs(tracker.getDimension().getMinY() >> 4)];
//            if (section == null) {
//                System.out.println("Can't find chunk section lol.");
//                return;
//            }

            final ByteBuf buffer = chunkData.getData().retainedDuplicate();
            try {
                ChunkSection readSection = null;
                final byte version = buffer.readByte();
                switch (version) {
                    case 0, 2, 3, 4, 5, 6, 7 -> readSection = new ChunkSection(new OlderDataPalette[] {readV0Section(buffer)});
                    case 1 -> readSection = new ChunkSection(new NewDataPalette[] {readBlockPalette(buffer)});
                    case 8, 9 -> readSection = new ChunkSection(readV9_8Section(version, buffer));
                }

                if (readSection != null) {
                    if (section == null) {
                        chunk[position.getY() + Math.abs(tracker.getDimension().getMinY() >> 4)] = readSection;
                    } else {
                        if (section.getDataPalettes().isEmpty()) {
                            section.getDataPalettes().addAll(readSection.getDataPalettes());
                        }
                    }
                }
            } finally {
                buffer.release();
            }
        }
    }

    private OlderDataPalette readV0Section(ByteBuf buffer) {
        final OlderDataPalette palette = new OlderDataPalette();
        buffer.readBytes(palette.getBlocks());
        buffer.readBytes(palette.getData().getHandle());
        return palette;
    }

    private NewDataPalette[] readV9_8Section(int version, ByteBuf buffer) {
        final short layers = buffer.readUnsignedByte(); // Layers (ideally 2).
        if (version == 9) {
            buffer.readUnsignedByte(); // Sub chunk index.
        }

        final NewDataPalette[] palettes = new NewDataPalette[layers];
        for (int layer = 0; layer < layers; layer++) {
            palettes[layer] = readBlockPalette(buffer);
        }
        return palettes;
    }

    private NewDataPalette readBlockPalette(ByteBuf buffer) {
        final short header = buffer.readUnsignedByte();
        final int bitArrayVersion = header >> 1;
        if (bitArrayVersion == 127) {
            return null;
        }

        final BitArray bitArray;
        if (bitArrayVersion == 0) {
            bitArray = BitArrayVersion.get(bitArrayVersion, true).createArray(4096, null);
        } else {
            bitArray = BitArrayVersion.get(bitArrayVersion, true).createArray(4096);
        }

        if (!(bitArray instanceof SingletonBitArray)) {
            for (int i = 0; i < bitArray.getWords().length; i++) {
                bitArray.getWords()[i] = buffer.readIntLE();
            }
        }

        final int size = bitArray instanceof SingletonBitArray ? 1 : VarInts.readInt(buffer);

        final IntList palette = new IntArrayList(size);
        for (int i = 0; i < size; i++) {
            palette.add(VarInts.readInt(buffer));
        }

        return new NewDataPalette(bitArray, palette);
    }
}
