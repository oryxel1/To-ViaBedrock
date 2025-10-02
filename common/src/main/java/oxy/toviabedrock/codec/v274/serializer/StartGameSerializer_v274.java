package oxy.toviabedrock.codec.v274.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import org.cloudburstmc.protocol.common.util.TextConverter;
import org.cloudburstmc.protocol.common.util.VarInts;
import oxy.toviabedrock.codec.v282.serializer.StartGameSerializer_v282;

// The same except no block palette or whatever id.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StartGameSerializer_v274 extends StartGameSerializer_v282 {
    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, StartGamePacket packet) {
        VarInts.writeLong(buffer, packet.getUniqueEntityId());
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        VarInts.writeInt(buffer, packet.getPlayerGameType().ordinal());
        helper.writeVector3f(buffer, packet.getPlayerPosition());
        helper.writeVector2f(buffer, packet.getRotation());

        this.writeLevelSettings(buffer, helper, packet);

        helper.writeString(buffer, packet.getLevelId());
        TextConverter converter = helper.getTextConverter();
        helper.writeString(buffer, converter.serialize(packet.getLevelName(CharSequence.class)));
        helper.writeString(buffer, packet.getPremiumWorldTemplateId());
        buffer.writeBoolean(packet.isTrial());
        buffer.writeLongLE(packet.getCurrentTick());
        VarInts.writeInt(buffer, packet.getEnchantmentSeed());
//
//        NbtList<NbtMap> palette = packet.getBlockPalette();
//        VarInts.writeUnsignedInt(buffer, palette.size());
//        for (NbtMap entry : palette) {
//            NbtMap blockTag = entry.getCompound("block");
//            helper.writeString(buffer, blockTag.getString("name"));
//            buffer.writeShortLE(entry.getShort("meta"));
//        }
//
//        helper.writeString(buffer, packet.getMultiplayerCorrelationId());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, StartGamePacket packet) {
        packet.setUniqueEntityId(VarInts.readLong(buffer));
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        packet.setPlayerGameType(GameType.from(VarInts.readInt(buffer)));
        packet.setPlayerPosition(helper.readVector3f(buffer));
        packet.setRotation(helper.readVector2f(buffer));

        this.readLevelSettings(buffer, helper, packet);

        packet.setLevelId(helper.readString(buffer));
        TextConverter converter = helper.getTextConverter();
        packet.setLevelName(converter.deserialize(helper.readString(buffer)));
        packet.setPremiumWorldTemplateId(helper.readString(buffer));
        packet.setTrial(buffer.readBoolean());
        packet.setCurrentTick(buffer.readLongLE());
        packet.setEnchantmentSeed(VarInts.readInt(buffer));
//
//        int paletteLength = VarInts.readUnsignedInt(buffer);
//        List<NbtMap> palette = new ObjectArrayList<>(paletteLength);
//        for (int i = 0; i < paletteLength; i++) {
//            palette.add(NbtMap.builder()
//                    .putCompound("block", NbtMap.builder()
//                            .putString("name", helper.readString(buffer))
//                            .build())
//                    .putShort("meta", buffer.readShortLE())
//                    .build());
//        }
//        packet.setBlockPalette(new NbtList<>(NbtType.COMPOUND, palette));
//
//        packet.setMultiplayerCorrelationId(helper.readString(buffer));
    }
}
