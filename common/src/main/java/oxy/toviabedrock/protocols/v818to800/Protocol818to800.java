package oxy.toviabedrock.protocols.v818to800;

import org.cloudburstmc.protocol.bedrock.codec.v800.Bedrock_v800;
import org.cloudburstmc.protocol.bedrock.codec.v818.Bedrock_v818;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.ExperimentData;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.BaseEntityMapper;
import oxy.toviabedrock.mappers.ItemMapper_v844;

import java.util.Arrays;
import java.util.EnumSet;

public class Protocol818to800 extends ProtocolToProtocol {
    public Protocol818to800() {
        super(Bedrock_v818.CODEC, Bedrock_v800.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new ItemMapper_v844(this) {
            @Override
            protected void initItemMappings() {
                this.itemIdentifierToMappedIdentifier.put("minecraft:music_disc_tears", "minecraft:music_disc_chirp");
            }
        });
        this.mappers.add(new BaseEntityMapper(this) {
            @Override
            protected void initEntityMappings() {
                this.identifierToMapped.put("minecraft:happy_ghast", new MappedEntity("minecraft:ghast", true));
            }
        });
        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();
            packet.setAuthoritativeMovementMode(AuthoritativeMovementMode.SERVER_WITH_REWIND);

            packet.getExperiments().add(new ExperimentData("experimental_graphics", true));
            packet.getExperiments().add(new ExperimentData("y_2025_drop_2", true));
            packet.getExperiments().add(new ExperimentData("locator_bar", true));
        });

        this.registerClientbound(ResourcePackStackPacket.class, wrapped -> {
            final ResourcePackStackPacket packet = (ResourcePackStackPacket) wrapped.getPacket();
            packet.getExperiments().add(new ExperimentData("y_2025_drop_2", true));
            packet.getExperiments().add(new ExperimentData("locator_bar", true));
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
            flags.remove(EntityFlag.BODY_ROTATION_ALWAYS_FOLLOWS_HEAD);
        }
    }
}
