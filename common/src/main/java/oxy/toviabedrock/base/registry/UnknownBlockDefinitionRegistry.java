package oxy.toviabedrock.base.registry;

import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import oxy.toviabedrock.base.definitions.UnknownBlockDefinition;

public record UnknownBlockDefinitionRegistry() implements DefinitionRegistry<BlockDefinition> {
    @Override
    public BlockDefinition getDefinition(int runtimeId) {
        return new UnknownBlockDefinition(runtimeId);
    }

    @Override
    public boolean isRegistered(BlockDefinition definition) {
        return true;
    }
}
