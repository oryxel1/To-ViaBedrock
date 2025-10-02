package oxy.toviabedrock.protocols.v291to282;

import org.cloudburstmc.protocol.bedrock.codec.v291.Bedrock_v291;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.codec.v282.Bedrock_v282;

import java.util.Arrays;
import java.util.EnumSet;

public class Protocol291to282 extends ProtocolToProtocol {
    public Protocol291to282() {
        super(Bedrock_v291.CODEC, Bedrock_v282.CODEC);
    }

    @Override
    public void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.registerClientbound(SetEntityDataPacket.class, wrapped -> this.cleanMetadata(((SetEntityDataPacket)wrapped.getPacket()).getMetadata()));
        this.registerClientbound(AddItemEntityPacket.class, wrapped -> this.cleanMetadata(((AddItemEntityPacket)wrapped.getPacket()).getMetadata()));
        this.registerClientbound(AddEntityPacket.class, wrapped -> this.cleanMetadata(((AddPlayerPacket)wrapped.getPacket()).getMetadata()));
        this.registerClientbound(AddPlayerPacket.class, wrapped -> this.cleanMetadata(((AddPlayerPacket)wrapped.getPacket()).getMetadata()));

        // TODO: Fix items and blocks.
    }

    private void cleanMetadata(EntityDataMap metadata) {
        if (metadata == null) {
            return;
        }

        if (metadata.getFlags() != null) {
            final EnumSet<EntityFlag> flags = metadata.getFlags();
            flags.remove(EntityFlag.ORPHANED);
            flags.remove(EntityFlag.IS_PREGNANT);
            flags.remove(EntityFlag.LAYING_EGG);
            flags.remove(EntityFlag.RIDER_CAN_PICK);
        }

        metadata.remove(EntityDataTypes.BOAT_BUBBLE_TIME);
        metadata.remove(EntityDataTypes.AGENT_EID);
    }
}
