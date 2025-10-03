package oxy.toviabedrock.utils.definition;

import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;

public record UnknownItemDefinition(int runtimeId) implements ItemDefinition {
    @Override
    public boolean isComponentBased() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "";
    }

    @Override
    public int getRuntimeId() {
        return runtimeId;
    }
}
