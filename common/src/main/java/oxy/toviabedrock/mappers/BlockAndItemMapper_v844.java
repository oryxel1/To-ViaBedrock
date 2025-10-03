package oxy.toviabedrock.mappers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.data.BlockChangeEntry;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSubChunkBlocksPacket;
import org.cloudburstmc.protocol.common.util.VarInts;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.utils.MathUtils;
import oxy.toviabedrock.utils.definition.UnknownBlockDefinition;

import java.util.HashMap;
import java.util.Map;

public class BlockAndItemMapper_v844 extends ProtocolToProtocol {
    protected final Map<Integer, Integer> mappedBlockIds = new HashMap<>();

    public BlockAndItemMapper_v844(BedrockCodec originalCodec, BedrockCodec translatedCodec) {
        super(originalCodec, translatedCodec);

        this.mapBlock();
        this.mapItem();
    }

    protected void mapBlock() {
    }
    protected void mapItem() {
    }

    @Override
    protected void registerProtocol() {
        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();
            // Bypass BDS block registry checksum, also no idea how this is calculated anyway.
            packet.setBlockRegistryChecksum(0);
        });

        this.registerClientbound(UpdateBlockPacket.class, wrapped -> {
            final UpdateBlockPacket packet = (UpdateBlockPacket) wrapped.getPacket();
            packet.setDefinition(new UnknownBlockDefinition(this.mappedBlockIds.getOrDefault(packet.getDefinition().getRuntimeId(), packet.getDefinition().getRuntimeId())));
        });

        this.registerClientbound(UpdateSubChunkBlocksPacket.class, wrapped -> {
            final UpdateSubChunkBlocksPacket packet = (UpdateSubChunkBlocksPacket) wrapped.getPacket();

            for (int i = 0; i < packet.getStandardBlocks().size(); i++) {
                final BlockChangeEntry entry = packet.getStandardBlocks().get(i);
                int blockId = entry.getDefinition().getRuntimeId();
                Integer id = this.mappedBlockIds.get(blockId);
                if (id == null) {
                    continue;
                }

                packet.getStandardBlocks().set(i,
                        new BlockChangeEntry(
                                entry.getPosition(),
                                new UnknownBlockDefinition(id),
                                entry.getUpdateFlags(),
                                entry.getMessageEntityId(),
                                entry.getMessageType()
                        )
                );
            }

            for (int i = 0; i < packet.getExtraBlocks().size(); i++) {
                final BlockChangeEntry entry = packet.getExtraBlocks().get(i);
                int blockId = entry.getDefinition().getRuntimeId();
                Integer id = this.mappedBlockIds.get(blockId);
                if (id == null) {
                    continue;
                }

                packet.getExtraBlocks().set(i,
                        new BlockChangeEntry(
                                entry.getPosition(),
                                new UnknownBlockDefinition(id),
                                entry.getUpdateFlags(),
                                entry.getMessageEntityId(),
                                entry.getMessageType()
                        )
                );
            }
        });

        this.registerClientbound(LevelChunkPacket.class, wrapped -> {
            final LevelChunkPacket packet = (LevelChunkPacket) wrapped.getPacket();
            final int subChunksCount = packet.getSubChunksLength();
            if (subChunksCount < -2 || packet.getDimension() < 0 || packet.getDimension() > 2) {
                wrapped.cancel();
                return;
            }

            byte[] payload;
            final ByteBuf newBuffer = ByteBufAllocator.DEFAULT.ioBuffer(packet.getData().capacity());
            final ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
            try {
                for (int i = 0; i < subChunksCount; i++) {
                    final byte version = buffer.readByte();
                    newBuffer.writeByte(version);

                    switch (version) {
                        case 8, 9 -> translatePaletteV89(version, buffer, newBuffer);
                    }
                }

                // Write the rest, we only handle the new blocks.
                newBuffer.writeBytes(buffer);

                payload = new byte[newBuffer.readableBytes()];
                newBuffer.readBytes(payload);

                LevelChunkPacket levelChunkPacket = new LevelChunkPacket();
                levelChunkPacket.setChunkX(packet.getChunkX());
                levelChunkPacket.setChunkZ(packet.getChunkZ());
                levelChunkPacket.setDimension(packet.getDimension());
                levelChunkPacket.setRequestSubChunks(packet.isRequestSubChunks());
                levelChunkPacket.setCachingEnabled(packet.isCachingEnabled());
                levelChunkPacket.setSubChunkLimit(packet.getSubChunkLimit());
                levelChunkPacket.setSubChunksLength(packet.getSubChunksLength());
                levelChunkPacket.getBlobIds().addAll(packet.getBlobIds());
                levelChunkPacket.setData(Unpooled.wrappedBuffer(payload));

                wrapped.setPacket(levelChunkPacket);
            } catch (Exception ignored) {
                wrapped.cancel();
                ignored.printStackTrace();
            } finally {
                newBuffer.release();
            }
        });
    }

    private void translatePaletteV89(int version, ByteBuf buffer, ByteBuf newBuffer) {
        final short layers = buffer.readUnsignedByte(); // Layers (ideally 2).
        newBuffer.writeByte(layers);
        if (version == 9) {
            newBuffer.writeByte(buffer.readUnsignedByte()); // Sub chunk index.
        }

        for (int layer = 0; layer < layers; layer++) {
            final short header = buffer.readUnsignedByte();
            newBuffer.writeByte(header);
            final int bitArrayVersion = header >> 1;
            if (bitArrayVersion == 127) {
                continue;
            }

            final BitVersionType bitVersion = BitVersionType.get(bitArrayVersion, true);

            int wordCount = MathUtils.ceil(4096F / BitVersionType.values()[bitVersion.ordinal()].entriesPerWord);
            if (bitVersion != BitVersionType.V0) {
                for (int word = 0; word < wordCount; word++) {
                    newBuffer.writeIntLE(buffer.readIntLE());
                }
            }

            final int size;

            if (bitVersion == BitVersionType.V0) {
                size = 1;
            } else {
                size = VarInts.readInt(buffer);
                VarInts.writeInt(newBuffer, size);
            }

            // It's a bad idea to map block like this since it allow uhh well duplicate block id! But it works then it works.
            for (int paletteIndex = 0; paletteIndex < size; paletteIndex++) {
                int blockId = VarInts.readInt(buffer);
                Integer id = this.mappedBlockIds.get(blockId);
                if (id != null) {
                    blockId = id;
                }

                VarInts.writeInt(newBuffer, blockId);
            }
        }
    }

    private enum BitVersionType {
        V16(16, 2),
        V8(8, 4),
        V6(6, 5), // 2 bit padding
        V5(5, 6), // 2 bit padding
        V4(4, 8),
        V3(3, 10), // 2 bit padding
        V2(2, 16),
        V1(1, 32),
        V0(0, 0);

        final byte bits;
        final byte entriesPerWord;
        final int maxEntryValue;

        BitVersionType(int bits, int entriesPerWord) {
            this.bits = (byte) bits;
            this.entriesPerWord = (byte) entriesPerWord;
            this.maxEntryValue = (1 << this.bits) - 1;
        }

        public static BitVersionType get(int version, boolean read) {
            for (BitVersionType ver : values()) {
                if ((!read && ver.entriesPerWord <= version) || (read && ver.bits == version)) {
                    return ver;
                }
            }
            throw new IllegalArgumentException("Invalid palette version: " + version);
        }
    }
}
