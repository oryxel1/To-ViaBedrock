package oxy.toviabedrock.codec.v282.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.AdventureSettingsSerializer_v291;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddPlayerSerializer_v282 implements BedrockPacketSerializer<AddPlayerPacket> {
    public static final AddPlayerSerializer_v282 INSTANCE = new AddPlayerSerializer_v282();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, AddPlayerPacket packet) {
        helper.writeUuid(buffer, packet.getUuid());
        helper.writeString(buffer, packet.getUsername());

        helper.writeString(buffer, ""); // Third party name
        VarInts.writeInt(buffer, 0); // Platform.

        VarInts.writeLong(buffer, packet.getUniqueEntityId());
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        helper.writeString(buffer, packet.getPlatformChatId());
        helper.writeVector3f(buffer, packet.getPosition());
        helper.writeVector3f(buffer, packet.getMotion());
        helper.writeVector3f(buffer, packet.getRotation());
        helper.writeItem(buffer, packet.getHand());
        helper.writeEntityData(buffer, packet.getMetadata());
        AdventureSettingsSerializer_v291.INSTANCE.serialize(buffer, helper, packet.getAdventureSettings());
        helper.writeArray(buffer, packet.getEntityLinks(), helper::writeEntityLink);

        writeDeviceId(buffer, helper, packet);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, AddPlayerPacket packet) {
        packet.setUuid(helper.readUuid(buffer));
        packet.setUsername(helper.readString(buffer));

        helper.readString(buffer); // Third party name.
        VarInts.readInt(buffer); // Platform.

        packet.setUniqueEntityId(VarInts.readLong(buffer));
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        packet.setPlatformChatId(helper.readString(buffer));
        packet.setPosition(helper.readVector3f(buffer));
        packet.setMotion(helper.readVector3f(buffer));
        packet.setRotation(helper.readVector3f(buffer));
        packet.setHand(helper.readItem(buffer));
        helper.readEntityData(buffer, packet.getMetadata());
        AdventureSettingsSerializer_v291.INSTANCE.deserialize(buffer, helper, packet.getAdventureSettings());
        helper.readArray(buffer, packet.getEntityLinks(), helper::readEntityLink);
        packet.setDeviceId(helper.readString(buffer));

        readDeviceId(buffer, helper, packet);
    }

    protected void writeDeviceId(ByteBuf buffer, BedrockCodecHelper helper, AddPlayerPacket packet) {
        helper.writeString(buffer, packet.getDeviceId());
    }

    protected void readDeviceId(ByteBuf buffer, BedrockCodecHelper helper, AddPlayerPacket packet) {
        packet.setDeviceId(helper.readString(buffer));
    }
}
