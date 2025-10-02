package org.oryxel.protocol.codec.v261.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContainerOpenSerializer_v261 implements BedrockPacketSerializer<ContainerOpenPacket> {
    public static final ContainerOpenSerializer_v261 INSTANCE = new ContainerOpenSerializer_v261();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ContainerOpenPacket packet) {
        buffer.writeByte(packet.getId());
        buffer.writeByte(packet.getType().getId());
        helper.writeBlockPosition(buffer, packet.getBlockPosition());
        VarInts.writeUnsignedLong(buffer, packet.getUniqueEntityId());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ContainerOpenPacket packet) {
        packet.setId(buffer.readByte());
        packet.setType(ContainerType.from(buffer.readByte()));
        packet.setBlockPosition(helper.readBlockPosition(buffer));
        packet.setUniqueEntityId(VarInts.readUnsignedLong(buffer));
    }
}
