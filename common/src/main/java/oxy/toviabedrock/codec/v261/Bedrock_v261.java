package oxy.toviabedrock.codec.v261;

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
import oxy.toviabedrock.codec.v261.serializer.*;

// Kinda weird how an old protocol extends a newer one eh? Well since Cloudburst already implemented 1.7.0 so do this is easier.
public class Bedrock_v261 extends Bedrock_v291 {
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
            .protocolVersion(261)
            .minecraftVersion("1.4.0")
            .helper(() -> new BedrockCodecHelper_v261(ENTITY_DATA, GAME_RULE_TYPES))
            // These packets don't exist on 1.4.0
            .deregisterPacket(PhotoTransferPacket.class)
            .deregisterPacket(MoveEntityDeltaPacket.class)
            .deregisterPacket(SetScoreboardIdentityPacket.class)
            .deregisterPacket(SetLocalPlayerAsInitializedPacket.class)
            .deregisterPacket(UpdateSoftEnumPacket.class)
            .deregisterPacket(NetworkStackLatencyPacket.class) // Oof, no latency packet check, this going to sucks.
            .deregisterPacket(ScriptCustomEventPacket.class)
            // Now let's "upgrade" the serializer.
            .updateSerializer(StartGamePacket.class, StartGameSerializer_v261.INSTANCE)
            .updateSerializer(AddPlayerPacket.class, AddPlayerSerializer_v261.INSTANCE)
            .updateSerializer(AddEntityPacket.class, AddEntitySerializer_v261.INSTANCE)
            .updateSerializer(MoveEntityAbsolutePacket.class, MoveEntityAbsoluteSerializer_v261.INSTANCE)
            .updateSerializer(ContainerOpenPacket.class, ContainerOpenSerializer_v261.INSTANCE)
            .updateSerializer(SetScorePacket.class, SetScoreSerializer_v261.INSTANCE)
            // This is correct, there is nothing here yet, this packet still exist despite that.
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
