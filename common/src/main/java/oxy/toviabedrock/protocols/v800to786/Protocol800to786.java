package oxy.toviabedrock.protocols.v800to786;

import org.cloudburstmc.protocol.bedrock.codec.v786.Bedrock_v786;
import org.cloudburstmc.protocol.bedrock.codec.v800.Bedrock_v800;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.BlockMapper_v844;
import oxy.toviabedrock.mappers.ItemMapper_v844;

import java.util.Arrays;
import java.util.EnumSet;

public class Protocol800to786 extends ProtocolToProtocol {
    public Protocol800to786() {
        super(Bedrock_v800.CODEC, Bedrock_v786.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                loadBlockMappingFromFile("v800to786/blockIds_v800to786.json");
                loadHashedBlockMappingFromFile("v800to786/blockIds_v800to786_hashed.json");
            }
        });

        this.mappers.add(new ItemMapper_v844(this) {
            @Override
            protected void initItemMappings() {
                this.loadItemMappingsFromFile("itemIdentifiers_v800to786.json");
            }
        });

        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.ignoreClientbound(PlayerLocationPacket.class);
        this.ignoreClientbound(ClientboundControlSchemeSetPacket.class);

        this.registerClientbound(AddEntityPacket.class, wrapped -> cleanMetadata(((AddEntityPacket)wrapped.getPacket()).getMetadata()));
        this.registerClientbound(SetEntityDataPacket.class, wrapped -> cleanMetadata(((SetEntityDataPacket)wrapped.getPacket()).getMetadata()));
    }

    private void cleanMetadata(final EntityDataMap metadata) {
        if (metadata == null) {
            return;
        }

        metadata.remove(EntityDataTypes.SEAT_THIRD_PERSON_CAMERA_RADIUS);
        metadata.remove(EntityDataTypes.SEAT_CAMERA_RELAX_DISTANCE_SMOOTHING);

        EnumSet<EntityFlag> flags = metadata.getFlags();
        if (flags != null) {
            flags.remove(EntityFlag.DOES_SERVER_AUTH_ONLY_DISMOUNT);
        }
    }
}
