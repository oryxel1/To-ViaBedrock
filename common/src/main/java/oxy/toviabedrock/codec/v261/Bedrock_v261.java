package oxy.toviabedrock.codec.v261;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.codec.v261.serializer.*;
import oxy.toviabedrock.codec.v274.Bedrock_v274;

public class Bedrock_v261 extends Bedrock_v274 {
    public static final BedrockCodec CODEC = Bedrock_v274.CODEC.toBuilder()
            .protocolVersion(261)
            .minecraftVersion("1.4.0")
            .deregisterPacket(UpdateBlockSyncedPacket.class) // What this packet used for anyway?
            .deregisterPacket(MoveEntityDeltaPacket.class) // bruh.
            // Again downgrade these.
            .updateSerializer(ContainerOpenPacket.class, ContainerOpenSerializer_v261.INSTANCE)
            .updateSerializer(UpdateBlockPacket.class, UpdateBlockSerializer_v261.INSTANCE)
            .updateSerializer(MoveEntityAbsolutePacket.class, MoveEntityAbsoluteSerializer_v261.INSTANCE)
            .updateSerializer(AddEntityPacket.class, AddEntitySerializer_v261.INSTANCE)
            .updateSerializer(StartGamePacket.class, StartGameSerializer_v261.INSTANCE)
            .build();
}
