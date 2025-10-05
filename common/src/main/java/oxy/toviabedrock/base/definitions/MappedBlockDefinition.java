package oxy.toviabedrock.base.definitions;

import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;

public record MappedBlockDefinition(int oldId, int newId) implements BlockDefinition {
    @Override
    public int getRuntimeId() {
        return this.newId;
    }
}
