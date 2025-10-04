package oxy.toviabedrock.utils.definition;

import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;

public record UnknownItemDefinition(String identifier, int runtimeId, boolean componentBased) implements ItemDefinition {
    @Override
    public boolean isComponentBased() {
        return componentBased;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getRuntimeId() {
        return runtimeId;
    }
}
