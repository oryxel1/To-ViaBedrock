package oxy.toviabedrock.protocols.v786to776;

import org.cloudburstmc.protocol.bedrock.codec.v776.Bedrock_v776;
import org.cloudburstmc.protocol.bedrock.codec.v786.Bedrock_v786;
import org.cloudburstmc.protocol.bedrock.data.ExperimentData;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.BlockMapper_v844;

import java.util.Arrays;
import java.util.EnumSet;

public class Protocol786to776 extends ProtocolToProtocol {
    public Protocol786to776() {
        super(Bedrock_v786.CODEC, Bedrock_v776.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                loadBlockMappingFromFile("v786to776/blockIds_v786to776.json");
                loadHashedBlockMappingFromFile("v786to776/blockIds_v786to776_hashed.json");
            }
        });
        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.ignoreClientbound(UpdateClientOptionsPacket.class);
        this.ignoreClientbound(PlayerVideoCapturePacket.class);

        this.ignoreServerbound(LevelSoundEvent1Packet.class);
        this.ignoreServerbound(LevelSoundEvent2Packet.class);

        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();

            packet.getExperiments().clear();
            packet.getExperiments().add(new ExperimentData("y_2025_drop_1", true));
        });

        this.registerClientbound(ResourcePackStackPacket.class, wrapped -> {
            final ResourcePackStackPacket packet = (ResourcePackStackPacket) wrapped.getPacket();
            packet.getExperiments().clear();
            packet.getExperiments().add(new ExperimentData("y_2025_drop_1", true));
        });

        this.registerClientbound(AddEntityPacket.class, wrapped -> cleanMetadata(((AddEntityPacket)wrapped.getPacket()).getMetadata()));
        this.registerClientbound(SetEntityDataPacket.class, wrapped -> cleanMetadata(((SetEntityDataPacket)wrapped.getPacket()).getMetadata()));
    }

    private void cleanMetadata(final EntityDataMap metadata) {
        if (metadata == null) {
            return;
        }

        EnumSet<EntityFlag> flags = metadata.getFlags();
        if (flags != null) {
            flags.remove(EntityFlag.BODY_ROTATION_AXIS_ALIGNED);
            flags.remove(EntityFlag.COLLIDABLE);
            flags.remove(EntityFlag.WASD_AIR_CONTROLLED);
        }
    }
}
