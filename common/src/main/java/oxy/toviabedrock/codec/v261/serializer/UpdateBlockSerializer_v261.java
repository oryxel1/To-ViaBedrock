package oxy.toviabedrock.codec.v261.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateBlockSerializer_v261 implements BedrockPacketSerializer<UpdateBlockPacket> {
    public static final UpdateBlockSerializer_v261 INSTANCE = new UpdateBlockSerializer_v261();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, UpdateBlockPacket packet) {
        helper.writeBlockPosition(buffer, packet.getBlockPosition());
        VarInts.writeUnsignedInt(buffer, packet.getDefinition().getRuntimeId());
        int flagValue = 0;
        for (UpdateBlockPacket.Flag flag : packet.getFlags()) {
            flagValue |= (1 << flag.ordinal());
        }
        VarInts.writeUnsignedInt(buffer, flagValue);
//        VarInts.writeUnsignedInt(buffer, packet.getDataLayer());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, UpdateBlockPacket packet) {
        packet.setBlockPosition(helper.readBlockPosition(buffer));
        packet.setDefinition(helper.getBlockDefinitions().getDefinition(VarInts.readUnsignedInt(buffer)));
        int flagValue = VarInts.readUnsignedInt(buffer);
        Set<UpdateBlockPacket.Flag> flags = packet.getFlags();
        for (UpdateBlockPacket.Flag flag : UpdateBlockPacket.Flag.values()) {
            if ((flagValue & (1 << flag.ordinal())) != 0) {
                flags.add(flag);
            }
        }
        packet.setDataLayer(0); // making my life harder I see.
    }
}
