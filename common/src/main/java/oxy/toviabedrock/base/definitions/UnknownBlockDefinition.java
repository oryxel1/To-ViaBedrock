package oxy.toviabedrock.base.definitions;

import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;

public record UnknownBlockDefinition(int runtimeId) implements BlockDefinition {
    @Override
    public int getRuntimeId() {
        return runtimeId;
    }
}