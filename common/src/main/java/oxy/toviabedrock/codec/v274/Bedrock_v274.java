package oxy.toviabedrock.codec.v274;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.codec.v274.serializer.AddPlayerSerializer_v274;
import oxy.toviabedrock.codec.v274.serializer.MoveEntityAbsoluteSerializer_v274;
import oxy.toviabedrock.codec.v274.serializer.StartGameSerializer_v274;
import oxy.toviabedrock.codec.v282.Bedrock_v282;

public class Bedrock_v274 extends Bedrock_v282 {
    public static final BedrockCodec CODEC = Bedrock_v282.CODEC.toBuilder()
            .protocolVersion(274)
            .minecraftVersion("1.5.0")
            .helper(() -> new BedrockCodecHelper_v274(ENTITY_DATA, GAME_RULE_TYPES))
            // Doesn't exist yet.
            .deregisterPacket(PhotoTransferPacket.class)
            .deregisterPacket(ShowProfilePacket.class)
            .deregisterPacket(SetDefaultGameTypePacket.class)
            .deregisterPacket(RemoveObjectivePacket.class)
            .deregisterPacket(SetDisplayObjectivePacket.class)
            .deregisterPacket(SetScorePacket.class)
            .deregisterPacket(LabTablePacket.class)
            .deregisterPacket(SetScoreboardIdentityPacket.class)
            .deregisterPacket(SetLocalPlayerAsInitializedPacket.class)
            .deregisterPacket(UpdateSoftEnumPacket.class)
            .deregisterPacket(NetworkStackLatencyPacket.class) // Yikes.
            // These need to be downgraded.
            .updateSerializer(StartGamePacket.class, StartGameSerializer_v274.INSTANCE)
            .updateSerializer(AddPlayerPacket.class, AddPlayerSerializer_v274.INSTANCE)
            .updateSerializer(MoveEntityAbsolutePacket.class, MoveEntityAbsoluteSerializer_v274.INSTANCE)
            .updateSerializer(CameraPacket.class, new BedrockPacketSerializer<>() {
                @Override
                public void serialize(ByteBuf buffer, BedrockCodecHelper helper, CameraPacket packet) {
                }

                @Override
                public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, CameraPacket packet) {
                }
            })
            .build();
}
