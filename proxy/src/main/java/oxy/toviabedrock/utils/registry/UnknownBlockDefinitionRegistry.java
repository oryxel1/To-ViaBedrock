package oxy.toviabedrock.utils.registry;

import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import oxy.toviabedrock.utils.definition.UnknownBlockDefinition;

public class UnknownBlockDefinitionRegistry implements DefinitionRegistry<BlockDefinition> {
    @Override
    public BlockDefinition getDefinition(int runtimeId) {
        return new UnknownBlockDefinition(runtimeId);
    }

    @Override
    public boolean isRegistered(BlockDefinition blockDefinition) {
        return true;
    }
}
