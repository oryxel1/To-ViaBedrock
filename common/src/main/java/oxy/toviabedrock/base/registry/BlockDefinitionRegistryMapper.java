package oxy.toviabedrock.base.registry;

import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.UpdateBlockSerializer_v291;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import oxy.toviabedrock.base.Mapper;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.definitions.MappedBlockDefinition;
import oxy.toviabedrock.base.mappers.BaseBlockMapper;
import oxy.toviabedrock.session.UserSession;

public record BlockDefinitionRegistryMapper(UserSession session) implements DefinitionRegistry<BlockDefinition> {
    @Override
    public BlockDefinition getDefinition(int runtimeId) {
        int mappedId = runtimeId;
        for (ProtocolToProtocol translator : this.session.getTranslators()) {
            for (Mapper mapper : translator.getMappers()) {
                if (!(mapper instanceof BaseBlockMapper blockMapper)) {
                    continue;
                }

                mappedId = blockMapper.mapBlockIdOrHashedId(this.session, mappedId);
            }
        }

        return new MappedBlockDefinition(runtimeId, mappedId);
    }

    @Override
    public boolean isRegistered(BlockDefinition definition) {
        return true;
    }
}
