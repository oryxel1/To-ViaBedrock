package oxy.toviabedrock.codec.v261.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.MoveEntityAbsoluteSerializer_v291;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import static org.cloudburstmc.protocol.common.util.Preconditions.checkNotNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoveEntityAbsoluteSerializer_v261 extends MoveEntityAbsoluteSerializer_v291 {
    public static final MoveEntityAbsoluteSerializer_v261 INSTANCE = new MoveEntityAbsoluteSerializer_v261();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, MoveEntityAbsolutePacket packet) {
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        helper.writeVector3f(buffer, packet.getPosition());
        this.writeByteRotation(buffer, helper, packet.getRotation());

        buffer.writeBoolean(packet.isOnGround());
        buffer.writeBoolean(packet.isTeleported());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, MoveEntityAbsolutePacket packet) {
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        packet.setPosition(helper.readVector3f(buffer));
        packet.setRotation(this.readByteRotation(buffer, helper));

        packet.setOnGround(buffer.readBoolean());
        packet.setTeleported(buffer.readBoolean());
    }
}
