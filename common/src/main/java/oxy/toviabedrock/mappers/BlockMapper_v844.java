package oxy.toviabedrock.mappers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.data.LevelEventType;
import org.cloudburstmc.protocol.bedrock.data.SubChunkData;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.util.VarInts;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.BaseBlockMapper;
import oxy.toviabedrock.base.mappers.storage.BaseItemRemappingStorage;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.utils.MathUtils;
import oxy.toviabedrock.utils.chunk.BitArrayVersion;

import java.util.ArrayList;
import java.util.List;

public class BlockMapper_v844 extends BaseBlockMapper {
    public BlockMapper_v844(ProtocolToProtocol translator) {
        super(translator);
    }

    private final List<LevelEventType> BLOCK_PARTICLE_EVENTS = List.of(
            LevelEvent.PARTICLE_BREAK_BLOCK_DOWN, LevelEvent.PARTICLE_BREAK_BLOCK_UP,
            LevelEvent.PARTICLE_BREAK_BLOCK_NORTH, LevelEvent.PARTICLE_BREAK_BLOCK_SOUTH,
            LevelEvent.PARTICLE_BREAK_BLOCK_WEST, LevelEvent.PARTICLE_BREAK_BLOCK_EAST,
            LevelEvent.PARTICLE_DESTROY_BLOCK, LevelEvent.PARTICLE_DESTROY_BLOCK_NO_SOUND,
            LevelEvent.PARTICLE_BLOCK_EXPLOSION, LevelEvent.PARTICLE_DENY_BLOCK,
            LevelEvent.PARTICLE_CRACK_BLOCK
    );

    @Override
    public void init(UserSession session) {
        session.put(new BaseItemRemappingStorage(session));
    }

    @Override
    protected void registerProtocol() {
        System.out.println("Register protocol: " + translator.getTranslatedCodec().getProtocolVersion());
        super.registerProtocol();

        this.registerClientbound(LevelEventPacket.class, wrapped -> {
            final LevelEventPacket packet = (LevelEventPacket) wrapped.getPacket();
            if (BLOCK_PARTICLE_EVENTS.contains(packet.getType())) {
                packet.setData(mapBlockIdOrHashedId(wrapped.session(), packet.getData()));
            }
        });

        this.registerServerbound(ClientCacheStatusPacket.class, wrapped -> {
            ClientCacheStatusPacket packet = (ClientCacheStatusPacket) wrapped.getPacket();
            packet.setSupported(false);
        });

        this.registerClientbound(LevelChunkPacket.class, wrapped -> {
            final LevelChunkPacket packet = (LevelChunkPacket) wrapped.getPacket();
            final int subChunksCount = packet.getSubChunksLength();
            if (subChunksCount < -2 || packet.getDimension() < 0 || packet.getDimension() > 2) {
                wrapped.cancel();
                return;
            }

            final ByteBuf newBuffer = ByteBufAllocator.DEFAULT.ioBuffer(packet.getData().capacity());
            final ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
            try {
                for (int i = 0; i < subChunksCount; i++) {
                    mapBlockPaletteInChunkSection(wrapped.session(), buffer, newBuffer);
                }

                // Write the rest, we only handle the new blocks.
                newBuffer.writeBytes(buffer);

                byte[] payload = new byte[newBuffer.readableBytes()];
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
//                ignored.printStackTrace();
            } finally {
                newBuffer.release();
            }
        });

        this.registerClientbound(SubChunkPacket.class, wrapped -> {
            final SubChunkPacket packet = (SubChunkPacket) wrapped.getPacket();

            final List<SubChunkData> subChunks = new ArrayList<>();
            for (SubChunkData chunkData : packet.getSubChunks()) {
                final ByteBuf buffer = Unpooled.wrappedBuffer(chunkData.getData());
                final ByteBuf newBuffer = ByteBufAllocator.DEFAULT.ioBuffer(buffer.capacity());
                try {
                    mapBlockPaletteInChunkSection(wrapped.session(), buffer, newBuffer);

                    // Write the rest, we only handle the new blocks.
                    newBuffer.writeBytes(buffer);

                    byte[] payload = new byte[newBuffer.readableBytes()];
                    newBuffer.readBytes(payload);

                    final SubChunkData data = new SubChunkData();
                    data.setPosition(chunkData.getPosition());
                    data.setData(Unpooled.wrappedBuffer(payload));
                    data.setResult(chunkData.getResult());
                    data.setHeightMapType(chunkData.getHeightMapType());
                    data.setHeightMapData(chunkData.getHeightMapData() == null ? null : Unpooled.wrappedBuffer(chunkData.getHeightMapData().copy()));
                    data.setRenderHeightMapType(chunkData.getRenderHeightMapType());
                    data.setRenderHeightMapData(chunkData.getRenderHeightMapData() == null ? null : Unpooled.wrappedBuffer(chunkData.getRenderHeightMapData().copy()));
                    data.setCacheEnabled(chunkData.isCacheEnabled());
                    data.setBlobId(chunkData.getBlobId());

                    subChunks.add(data);
                } catch (Exception ignored) {
                    wrapped.cancel();
//                    ignored.printStackTrace();
                    return;
                } finally {
                    newBuffer.release();
                }
            }

            final SubChunkPacket subChunkPacket = new SubChunkPacket();
            subChunkPacket.setCacheEnabled(packet.isCacheEnabled());
            subChunkPacket.setDimension(packet.getDimension());
            subChunkPacket.setCenterPosition(packet.getCenterPosition());
            subChunkPacket.setSubChunks(subChunks);

            wrapped.setPacket(subChunkPacket);
        });
    }

    private void mapBlockPaletteInChunkSection(UserSession session, ByteBuf buffer, ByteBuf newBuffer) {
        final byte version = buffer.readByte();
        newBuffer.writeByte(version);

        switch (version) {
            case 8, 9 -> mapBlockPaletteV9_8(session, version, buffer, newBuffer);
        }
    }

    private void mapBlockPaletteV9_8(UserSession session, int version, ByteBuf buffer, ByteBuf newBuffer) {
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

            final BitArrayVersion bitVersion = BitArrayVersion.get(bitArrayVersion, true);

            int wordCount = MathUtils.ceil(4096F / BitArrayVersion.values()[bitVersion.ordinal()].entriesPerWord);
            if (bitVersion != BitArrayVersion.V0) {
                for (int word = 0; word < wordCount; word++) {
                    newBuffer.writeIntLE(buffer.readIntLE());
                }
            }

            final int size;

            if (bitVersion == BitArrayVersion.V0) {
                size = 1;
            } else {
                size = VarInts.readInt(buffer);
                VarInts.writeInt(newBuffer, size);
            }

            // It's a bad idea to map block like this since it allow uhh well duplicate block id! But it works then it works.
            for (int paletteIndex = 0; paletteIndex < size; paletteIndex++) {
                VarInts.writeInt(newBuffer, this.mapBlockIdOrHashedId(session, VarInts.readInt(buffer)));
            }
        }
    }
}
