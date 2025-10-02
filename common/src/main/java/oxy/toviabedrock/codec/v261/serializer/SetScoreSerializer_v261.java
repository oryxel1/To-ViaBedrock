package oxy.toviabedrock.codec.v261.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.ScoreInfo;
import org.cloudburstmc.protocol.bedrock.packet.SetScorePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.UUID;

import static org.cloudburstmc.protocol.bedrock.packet.SetScorePacket.Action;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SetScoreSerializer_v261 implements BedrockPacketSerializer<SetScorePacket> {
    public static final SetScoreSerializer_v261 INSTANCE = new SetScoreSerializer_v261();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, SetScorePacket packet) {
        Action action = packet.getAction();
        buffer.writeByte(action.ordinal());

        // Oops, CloudburstMC protocol doesn't have these, let's cheat a little.
        helper.writeArray(buffer, packet.getInfos(), (buf, scoreInfo) -> {
            helper.writeUuid(buf, UUID.fromString(scoreInfo.getObjectiveId()));
            helper.writeString(buf, scoreInfo.getName());
            VarInts.writeUnsignedInt(buf, scoreInfo.getScore());
        });
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, SetScorePacket packet) {
        Action action = Action.values()[buffer.readUnsignedByte()];
        packet.setAction(action);

        helper.readArray(buffer, packet.getInfos(), buf -> {
            final UUID uuid = helper.readUuid(buf);
            final String scoreName = helper.readString(buf);
            final int score = VarInts.readUnsignedInt(buf);
            return new ScoreInfo(-1, uuid.toString(), score, scoreName);
        });
    }

}
