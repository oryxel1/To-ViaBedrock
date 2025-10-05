package oxy.toviabedrock.protocols.v776to766;

import org.cloudburstmc.protocol.bedrock.codec.v766.Bedrock_v766;
import org.cloudburstmc.protocol.bedrock.codec.v776.Bedrock_v776;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.v766.ItemMapper_v766;
import oxy.toviabedrock.mappers.v844.BlockMapper_v844;

import java.util.Arrays;
import java.util.EnumSet;

public class Protocol776to766 extends ProtocolToProtocol {
    public Protocol776to766() {
        super(Bedrock_v776.CODEC, Bedrock_v766.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                loadBlockMappingFromFile("v776to766/blockIds_v776to766.json");
                loadHashedBlockMappingFromFile("v776to766/blockIds_v776to766_hashed.json");
            }
        });
        this.mappers.add(new ItemMapper_v766(this) {
            @Override
            protected void initItemMappings() {
                loadItemMappingsFromFile("itemIdentifiers_v776to766.json");
                loadVanillaIdentifiersFromFile("vanilla_items_v766.json");
            }
        });
        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.ignoreClientbound(CameraAimAssistInstructionPacket.class);
        this.ignoreClientbound(MovementPredictionSyncPacket.class);

        this.registerClientbound(AddEntityPacket.class, wrapped -> cleanMetadata(((AddEntityPacket)wrapped.getPacket()).getMetadata()));
        this.registerClientbound(SetEntityDataPacket.class, wrapped -> cleanMetadata(((SetEntityDataPacket)wrapped.getPacket()).getMetadata()));

        this.registerClientbound(UpdateAbilitiesPacket.class, wrapped -> {
            final UpdateAbilitiesPacket packet = (UpdateAbilitiesPacket) wrapped.getPacket();
            for (AbilityLayer layer : packet.getAbilityLayers()) {
                layer.getAbilitiesSet().remove(Ability.VERTICAL_FLY_SPEED);
                layer.getAbilityValues().remove(Ability.VERTICAL_FLY_SPEED);
            }
        });
    }

    private void cleanMetadata(final EntityDataMap metadata) {
        if (metadata == null) {
            return;
        }

        metadata.remove(EntityDataTypes.FILTERED_NAME);
        metadata.remove(EntityDataTypes.BED_ENTER_POSITION);
        EnumSet<EntityFlag> flags = metadata.getFlags();
        if (flags != null) {
            flags.remove(EntityFlag.RENDER_WHEN_INVISIBLE);
        }
    }
}
