package oxy.toviabedrock.codec.v282;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.codec.EntityDataTypeMap;
import org.cloudburstmc.protocol.bedrock.codec.v291.Bedrock_v291;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataFormat;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.bedrock.transformer.FlagTransformer;
import org.cloudburstmc.protocol.common.util.TypeMap;
import oxy.toviabedrock.codec.v282.serializer.AddPlayerSerializer_v282;
import oxy.toviabedrock.codec.v282.serializer.StartGameSerializer_v282;

// Kinda weird how an old protocol extends a newer one eh? Well since Cloudburst already implemented 1.7.0 so do this is easier.
public class Bedrock_v282 extends Bedrock_v291 {
    protected static final TypeMap<EntityFlag> ENTITY_FLAGS =
            Bedrock_v291.ENTITY_FLAGS.toBuilder()
                    .remove(29) // EntityFlag.ORPHANED
                    .remove(58) // EntityFlag.IS_PREGNANT
                    .remove(59) // EntityFlag.LAYING_EGG
                    .remove(60) // EntityFlag.RIDER_CAN_PICK
                    .build();

    protected static final EntityDataTypeMap ENTITY_DATA =
            Bedrock_v291.ENTITY_DATA.toBuilder()
                    .replace(EntityDataTypes.FLAGS, 0, EntityDataFormat.LONG, new FlagTransformer(ENTITY_FLAGS, 0)) // Fixed entity flags.
                    .remove(86) // EntityDataTypes.BOAT_BUBBLE_TIME
                    .remove(87) // EntityDataTypes.AGENT_EID
                    .build();

    public static final BedrockCodec CODEC = Bedrock_v291.CODEC.toBuilder()
            .protocolVersion(282)
            .minecraftVersion("1.6.0")
            // Now let's "upgrade" the serializer.
            .updateSerializer(StartGamePacket.class, StartGameSerializer_v282.INSTANCE)
            .updateSerializer(AddPlayerPacket.class, AddPlayerSerializer_v282.INSTANCE)
            .updateSerializer(SetLocalPlayerAsInitializedPacket.class, new BedrockPacketSerializer<>() {
                @Override
                public void serialize(ByteBuf buffer, BedrockCodecHelper helper, SetLocalPlayerAsInitializedPacket packet) {
                }

                @Override
                public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, SetLocalPlayerAsInitializedPacket packet) {
                }
            })
            .build();
}
