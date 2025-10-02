package oxy.toviabedrock.codec.v274.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.MoveEntityAbsoluteSerializer_v291;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

// Flags (short instead of byte).
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoveEntityAbsoluteSerializer_v274 extends MoveEntityAbsoluteSerializer_v291 {
    public static final MoveEntityAbsoluteSerializer_v274 INSTANCE = new MoveEntityAbsoluteSerializer_v274();

    private static final int FLAG_ON_GROUND = 0x1;
    private static final int FLAG_TELEPORTED = 0x2;
    private static final int FLAG_FORCE_MOVE = 0x4;

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, MoveEntityAbsolutePacket packet) {
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        int flags = 0;
        if (packet.isOnGround()) {
            flags |= FLAG_ON_GROUND;
        }
        if (packet.isTeleported()) {
            flags |= FLAG_TELEPORTED;
        }
        if (packet.isForceMove()) {
            flags |= FLAG_FORCE_MOVE;
        }
        buffer.writeShort(flags);
        helper.writeVector3f(buffer, packet.getPosition());
        this.writeByteRotation(buffer, helper, packet.getRotation());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, MoveEntityAbsolutePacket packet) {
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        int flags = buffer.readUnsignedShort();
        packet.setOnGround((flags & FLAG_ON_GROUND) != 0);
        packet.setTeleported((flags & FLAG_TELEPORTED) != 0);
        packet.setForceMove((flags & FLAG_FORCE_MOVE) != 0);
        packet.setPosition(helper.readVector3f(buffer));
        packet.setRotation(this.readByteRotation(buffer, helper));
    }
}
