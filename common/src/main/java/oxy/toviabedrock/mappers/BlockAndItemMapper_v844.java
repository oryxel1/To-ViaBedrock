package oxy.toviabedrock.mappers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.data.BlockChangeEntry;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.data.LevelEventType;
import org.cloudburstmc.protocol.bedrock.data.SubChunkData;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.CreativeItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.util.VarInts;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.storage.ItemRemappingStorage_v844;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.impl.GameSessionStorage;
import oxy.toviabedrock.utils.HashMapWithHashed;
import oxy.toviabedrock.utils.MathUtils;
import oxy.toviabedrock.utils.definition.TOVBItemData;
import oxy.toviabedrock.utils.definition.UnknownBlockDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockAndItemMapper_v844 extends ProtocolToProtocol {
    protected final Map<Integer, Integer> mappedBlockIds = new HashMap<>();
    protected final Map<Integer, Integer> mappedHashedBlockIds = new HashMap<>();

    protected final Map<String, ItemRemapper> itemIdentifierToRemapper = new HashMap<>();
    protected final HashMapWithHashed<String, String> itemIdentifierToMapped = new HashMapWithHashed<>();
    public interface ItemRemapper {
        ItemDefinition remap(ItemDefinition definition);
    }

    public BlockAndItemMapper_v844(BedrockCodec originalCodec, BedrockCodec translatedCodec) {
        super(originalCodec, translatedCodec);

        this.mapBlock();
        this.mapItem();
    }

    protected void mapBlock() {
    }
    protected void mapItem() {
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
        session.put(new ItemRemappingStorage_v844(session));
    }

    @Override
    protected void registerProtocol() {
        // Item mapping part.
        this.registerServerbound(InventoryTransactionPacket.class, wrapped -> {
            final InventoryTransactionPacket packet = (InventoryTransactionPacket) wrapped.getPacket();
            packet.setItemInHand(this.mapItemReversed(wrapped.session(), packet.getItemInHand()));
            if (packet.getBlockDefinition() != null) {
                packet.setBlockDefinition(new UnknownBlockDefinition(mapBlockId(wrapped.session(), packet.getBlockDefinition().getRuntimeId())));
            }
//            System.out.println(packet);
        });

        this.registerClientbound(CreativeContentPacket.class, wrapped -> {
            final CreativeContentPacket packet = (CreativeContentPacket) wrapped.getPacket();
            for (int i = 0; i < packet.getContents().size(); i++) {
                final CreativeItemData creative = packet.getContents().get(i);
                final ItemData itemData = creative.getItem();
                final ItemData mapped = this.mapItem(wrapped.session(), itemData);

                if (mapped != itemData) {
                    packet.getContents().set(i, new CreativeItemData(mapped, creative.getNetId(), creative.getGroupId()));
                }
            }
        });

        this.registerClientbound(InventoryContentPacket.class, wrapped -> {
            final InventoryContentPacket packet = (InventoryContentPacket) wrapped.getPacket();
//            System.out.println("----------------------------");
            for (int i = 0; i < packet.getContents().size(); i++) {
                final ItemData itemData = packet.getContents().get(i);
                final ItemData mapped = this.mapItem(wrapped.session(), itemData);
//                if (itemData.getDefinition().getIdentifier().equals("minecraft:copper_chain")) {
//                    System.out.println("Map: " + mapped + ", equals=" + (itemData == mapped));
//                } else {
//                    System.out.println(itemData.getDefinition());
//                }

                if (mapped != itemData) {
                    packet.getContents().set(i, mapped);
                }
            }
        });

        this.registerClientbound(InventorySlotPacket.class, wrapped -> {
            final InventorySlotPacket packet = (InventorySlotPacket) wrapped.getPacket();
            packet.setItem(mapItem(wrapped.session(), packet.getItem()));
        });

        this.registerClientbound(ItemComponentPacket.class, wrapped -> {
            final ItemComponentPacket packet = (ItemComponentPacket) wrapped.getPacket();
            for (int i = 0; i < packet.getItems().size(); i++) {
                final ItemDefinition definition = packet.getItems().get(i);
                final ItemRemapper remapper = this.itemIdentifierToRemapper.get(definition.getIdentifier());
                if (remapper != null) {
                    packet.getItems().set(i, remapper.remap(definition));
                }

                wrapped.session().get(ItemRemappingStorage_v844.class).put(definition.getIdentifier(), definition);
                System.out.println(definition);
            }
        });

        // Block mapping part.

        this.registerClientbound(LevelEventPacket.class, wrapped -> {
            final LevelEventPacket packet = (LevelEventPacket) wrapped.getPacket();
            if (BLOCK_PARTICLE_EVENTS.contains(packet.getType())) {
                packet.setData(mapBlockId(wrapped.session(), packet.getData()));
            }
        });

        this.registerServerbound(ClientCacheStatusPacket.class, wrapped -> {
            ClientCacheStatusPacket packet = (ClientCacheStatusPacket) wrapped.getPacket();
            packet.setSupported(false);
        });

        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();
            // Bypass BDS block registry checksum, also no idea how this is calculated anyway.
            packet.setBlockRegistryChecksum(0);
        });

        this.registerClientbound(UpdateBlockPacket.class, wrapped -> {
            final UpdateBlockPacket packet = (UpdateBlockPacket) wrapped.getPacket();
            packet.setDefinition(new UnknownBlockDefinition(mapBlockId(wrapped.session(), packet.getDefinition().getRuntimeId())));
        });

        this.registerClientbound(UpdateSubChunkBlocksPacket.class, wrapped -> {
            final UpdateSubChunkBlocksPacket packet = (UpdateSubChunkBlocksPacket) wrapped.getPacket();

            for (int i = 0; i < packet.getStandardBlocks().size(); i++) {
                final BlockChangeEntry entry = packet.getStandardBlocks().get(i);
                int blockId = entry.getDefinition().getRuntimeId();
                int id = mapBlockId(wrapped.session(), blockId);
                if (id == blockId) {
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
                int id = mapBlockId(wrapped.session(), blockId);
                if (id == blockId) {
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

            final ByteBuf newBuffer = ByteBufAllocator.DEFAULT.ioBuffer(packet.getData().capacity());
            final ByteBuf buffer = Unpooled.wrappedBuffer(packet.getData());
            try {
                for (int i = 0; i < subChunksCount; i++) {
                    readChunkSectionAndTranslate(wrapped.session(), buffer, newBuffer);
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
                    readChunkSectionAndTranslate(wrapped.session(), buffer, newBuffer);

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

    private void readChunkSectionAndTranslate(UserSession session, ByteBuf buffer, ByteBuf newBuffer) {
        final byte version = buffer.readByte();
        newBuffer.writeByte(version);

        switch (version) {
            case 8, 9 -> translatePaletteV89(session, version, buffer, newBuffer);
        }
    }

    private void translatePaletteV89(UserSession session, int version, ByteBuf buffer, ByteBuf newBuffer) {
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
                VarInts.writeInt(newBuffer, mapBlockId(session, VarInts.readInt(buffer)));
            }
        }
    }

    private ItemData mapItem(UserSession session, ItemData data) {
        if (data == null) {
            return null;
        }

        final String identifier = this.itemIdentifierToMapped.get(data.getDefinition().getIdentifier());
        if (identifier == null) {
            return data;
        }
        final ItemDefinition mapped = session.get(ItemRemappingStorage_v844.class).getDefinition(identifier);
        if (mapped == null) {
            return data;
        }

        BlockDefinition definition = null;
        if (data.getBlockDefinition() != null) {
            definition = new UnknownBlockDefinition(mapBlockId(session, data.getBlockDefinition().getRuntimeId()));
        }

        final NbtMap originalTag = data.getTag() == null ? NbtMap.builder().build() : data.getTag();
        final NbtMapBuilder nbtBuilder = originalTag.toBuilder();

        NbtMapBuilder display = originalTag.containsKey("display") ? originalTag.getCompound("display").toBuilder() : NbtMap.builder();

        int hashed = data.getDefinition().getIdentifier().hashCode();
        if (display.build().containsKey("Lore")) {
            List<String> lores = display.build().getList("lore", NbtType.STRING);
            lores.add("§r§7Item mapped from: " + data.getDefinition().getIdentifier() + " (" + hashed + ").");
            display.put("Lore", data.getDefinition().getIdentifier());
        } else {
            display.putList("Lore", NbtType.STRING, List.of("§r§7Item mapped from: " + data.getDefinition().getIdentifier() + " (" + hashed + ")."));
        }
        nbtBuilder.put("display", display.build());
        nbtBuilder.put("TOVBHash", hashed);

        return new TOVBItemData(mapped, data.getDamage(),
                data.getCount(), nbtBuilder.build(), data.getCanPlace(),
                data.getCanBreak(), data.getBlockingTicks(), definition, data.isUsingNetId(), data.getNetId());
    }

    // I know this is a bad idea since we should properly track the item instead but oh well.
    // It works so who cares! Only BDS is *this* strict with item tag anyway!
    private ItemData mapItemReversed(UserSession session, ItemData data) {
        if (data == null) {
            return null;
        }

        if (data.getTag() == null || data.getTag().isEmpty() || !data.getTag().containsKey("TOVBHash")) {
            return data;
        }

//        System.out.println(data.getTag().getInt("TOVBHash"));
        final String identifier = this.itemIdentifierToMapped.getHashed().get(data.getTag().getInt("TOVBHash"));
        if (identifier == null) {
//            System.out.println("Null!");
            return data;
        }
        final ItemDefinition mapped = session.get(ItemRemappingStorage_v844.class).getDefinition(identifier);
        if (mapped == null) {
            return data;
        }

        BlockDefinition definition = null;
        if (data.getBlockDefinition() != null) {
            definition = new UnknownBlockDefinition(mapBlockId(session, data.getBlockDefinition().getRuntimeId()));
        }

        NbtMapBuilder tagBuilder = data.getTag().toBuilder();
        if (data.getTag().containsKey("display")) {
            final NbtMap display = data.getTag().getCompound("display");
            final NbtMapBuilder displayNbt = display.toBuilder();

            if (display.containsKey("Lore")) {
                final List<String> lore = new ArrayList<>(display.getList("Lore", NbtType.STRING));
                if (lore.size() == 1) {
                    displayNbt.remove("Lore");
                } else {
                    final String lore1 = "§r§7Item mapped from: " + mapped.getIdentifier() + " (" + data.getTag().getInt("TOVBHash") + ").";
                    lore.remove(lore1);
                    displayNbt.putList("Lore", NbtType.STRING, lore);
                }
            }

            if (!displayNbt.isEmpty()) {
                tagBuilder.put("display", displayNbt.build());
            } else {
                tagBuilder.remove("display");
            }
        }
        tagBuilder.remove("TOVBHash");

        return new TOVBItemData(mapped, data.getDamage(),
                data.getCount(), tagBuilder.build(), data.getCanPlace(),
                data.getCanBreak(), data.getBlockingTicks(), definition, data.isUsingNetId(), data.getNetId());
    }

    private int mapBlockId(UserSession user, int id) {
        if (user.get(GameSessionStorage.class).isBlockNetworkIdsHashed()) {
            return this.mappedHashedBlockIds.getOrDefault(id, id);
        } else {
            return this.mappedBlockIds.getOrDefault(id, id);
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
