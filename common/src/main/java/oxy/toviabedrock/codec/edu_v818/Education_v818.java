package oxy.toviabedrock.codec.edu_v818;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v818.Bedrock_v818;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import oxy.toviabedrock.codec.edu_v818.serializer.StartGameSerializer_Edu_v818;

public class Education_v818 extends Bedrock_v818 {
    public static final BedrockCodec CODEC = Bedrock_v818.CODEC.toBuilder()
            .updateSerializer(StartGamePacket.class, StartGameSerializer_Edu_v818.INSTANCE)
            .build();
}
