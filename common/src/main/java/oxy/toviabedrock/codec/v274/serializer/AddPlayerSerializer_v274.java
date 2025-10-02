package oxy.toviabedrock.codec.v274.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;
import oxy.toviabedrock.codec.v282.serializer.AddPlayerSerializer_v282;

public class AddPlayerSerializer_v274 extends AddPlayerSerializer_v282 {
    @Override
    protected void readDeviceId(ByteBuf buffer, BedrockCodecHelper helper, AddPlayerPacket packet) {
        packet.setDeviceId("None");
    }

    @Override
    protected void writeDeviceId(ByteBuf buffer, BedrockCodecHelper helper, AddPlayerPacket packet) {
    }
}
