package oxy.toviabedrock.protocols.v844to827;

import org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827;
import org.cloudburstmc.protocol.bedrock.codec.v844.Bedrock_v844;
import org.cloudburstmc.protocol.bedrock.data.ExperimentData;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackStackPacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.BlockMapper_v844;
import oxy.toviabedrock.mappers.ItemMapper_v844;

import java.util.Arrays;

public class Protocol844to827 extends ProtocolToProtocol {
    public Protocol844to827() {
        super(Bedrock_v844.CODEC, Bedrock_v827.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                loadBlockMappingFromFile("v844to827/blockIds_v844to827.json");
                loadHashedBlockMappingFromFile("v844to827/blockIds_v844to827_hashed.json");
            }
        });

        this.mappers.add(new ItemMapper_v844(this) {
            @Override
            protected void initItemMappings() {
                this.identifierToIdentifier.put("minecraft:iron_chain", "minecraft:chain");

                this.loadItemMappingsFromFile("itemIdentifiers_v844to827.json");
            }
        });
        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.registerClientbound(ResourcePackStackPacket.class, wrapped -> {
            final ResourcePackStackPacket packet = (ResourcePackStackPacket) wrapped.getPacket();

            // We want to add support for some of the new blocks.
            packet.getExperiments().add(new ExperimentData("y_2025_drop_3", true));
        });

        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();

            // We want to add support for some of the new items.
            packet.getExperiments().add(new ExperimentData("y_2025_drop_3", true));
        });
    }
}
