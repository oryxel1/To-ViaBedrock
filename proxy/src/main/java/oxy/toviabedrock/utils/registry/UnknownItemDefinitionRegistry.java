package oxy.toviabedrock.utils.registry;

import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import oxy.toviabedrock.utils.definition.UnknownItemDefinition;

public class UnknownItemDefinitionRegistry implements DefinitionRegistry<ItemDefinition> {
    @Override
    public ItemDefinition getDefinition(int runtimeId) {
        return new UnknownItemDefinition("", runtimeId, true);
    }

    @Override
    public boolean isRegistered(ItemDefinition definition) {
        return true;
    }
}
