package oxy.toviabedrock.codec.edu_v818.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v818.serializer.StartGameSerializer_v818;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;

public class StartGameSerializer_Edu_v818 extends StartGameSerializer_v818 {
    public static final StartGameSerializer_Edu_v818 INSTANCE = new StartGameSerializer_Edu_v818();

    @Override
    protected void writeLevelSettings(ByteBuf buffer, BedrockCodecHelper helper, StartGamePacket packet) {
        super.writeLevelSettings(buffer, helper, packet);
        helper.writeString(buffer, ""); // Education Creator id.
        helper.writeString(buffer, ""); // Education Creator World id.
        helper.writeString(buffer, ""); // Education Referral id.
    }

    @Override
    protected void readLevelSettings(ByteBuf buffer, BedrockCodecHelper helper, StartGamePacket packet) {
        super.readLevelSettings(buffer, helper, packet);
        helper.readString(buffer); // Education Creator id.
        helper.readString(buffer); // Education Creator World id.
        helper.readString(buffer); // Education Referral id.
    }
}
