package oxy.toviabedrock.protocols.v748to729;

import org.cloudburstmc.protocol.bedrock.codec.v729.Bedrock_v729;
import org.cloudburstmc.protocol.bedrock.codec.v748.Bedrock_v748;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.v766.ItemMapper_v766;
import oxy.toviabedrock.mappers.v844.BlockMapper_v844;
import oxy.toviabedrock.utils.MathUtils;

import java.util.Arrays;

public class Protocol748to729 extends ProtocolToProtocol {
    public Protocol748to729() {
        super(Bedrock_v748.CODEC, Bedrock_v729.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                loadBlockMappingFromFile("v748to729/blockIds_v748to729.json");
                loadHashedBlockMappingFromFile("v748to729/blockIds_v748to729_hashed.json");
            }
        });
        this.mappers.add(new ItemMapper_v766(this) {
            @Override
            protected void initItemMappings() {
                loadVanillaIdentifiersFromFile("vanilla_items_v729.json");
                loadItemMappingsFromFile("itemIdentifiers_v748to729.json");
            }
        });
        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.registerServerbound(PlayerAuthInputPacket.class, wrapped -> {
            final PlayerAuthInputPacket packet = (PlayerAuthInputPacket) wrapped.getPacket();
            packet.setInteractRotation(packet.getRotation().toVector2());
            packet.setCameraOrientation(MathUtils.getCameraOrientation(packet.getRotation()));
        });
    }
}
