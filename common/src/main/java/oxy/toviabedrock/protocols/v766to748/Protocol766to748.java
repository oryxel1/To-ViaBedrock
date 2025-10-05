package oxy.toviabedrock.protocols.v766to748;

import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.protocol.bedrock.codec.v748.Bedrock_v748;
import org.cloudburstmc.protocol.bedrock.codec.v766.Bedrock_v766;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.bedrock.packet.CameraAimAssistPresetsPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.v766.ItemMapper_v766;
import oxy.toviabedrock.mappers.v844.BlockMapper_v844;
import oxy.toviabedrock.utils.MathUtils;

import java.util.Arrays;

public class Protocol766to748 extends ProtocolToProtocol {
    public Protocol766to748() {
        super(Bedrock_v766.CODEC, Bedrock_v748.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                loadBlockMappingFromFile("v766to748/blockIds_v766to748.json");
                loadHashedBlockMappingFromFile("v766to748/blockIds_v766to748_hashed.json");
            }
        });
        this.mappers.add(new ItemMapper_v766(this) {
            @Override
            protected void initItemMappings() {
                loadItemMappingsFromFile("itemIdentifiers_v766to748.json");
                loadVanillaIdentifiersFromFile("vanilla_items_v748.json");
            }
        });
        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.ignoreClientbound(CameraAimAssistPresetsPacket.class);
        this.registerServerbound(PlayerAuthInputPacket.class, wrapped -> {
            final PlayerAuthInputPacket packet = (PlayerAuthInputPacket) wrapped.getPacket();
            if (packet.getAnalogMoveVector().lengthSquared() > 0) {
                packet.setRawMoveVector(packet.getAnalogMoveVector());
            } else {
                packet.setRawMoveVector(Vector2f.from(MathUtils.sign(packet.getMotion().getX()), MathUtils.sign(packet.getMotion().getY())));
            }
        });
    }
}
