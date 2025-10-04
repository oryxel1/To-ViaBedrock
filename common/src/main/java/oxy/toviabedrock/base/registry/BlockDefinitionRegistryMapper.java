package oxy.toviabedrock.base.registry;

import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import oxy.toviabedrock.base.Mapper;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.BaseBlockMapper;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.base.definitions.UnknownBlockDefinition;

public record BlockDefinitionRegistryMapper(UserSession session) implements DefinitionRegistry<BlockDefinition> {
    @Override
    public BlockDefinition getDefinition(int runtimeId) {
        for (ProtocolToProtocol translator : this.session.getTranslators()) {
            for (Mapper mapper : translator.getMappers()) {
                if (!(mapper instanceof BaseBlockMapper blockMapper)) {
                    continue;
                }

                runtimeId = blockMapper.mapBlockIdOrHashedId(this.session, runtimeId);
            }
        }

        return new UnknownBlockDefinition(runtimeId);
    }

    @Override
    public boolean isRegistered(BlockDefinition definition) {
        return true;
    }
}
