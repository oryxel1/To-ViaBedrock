package oxy.toviabedrock.mappers.storage;

import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;

public class ItemRemappingStorage_v844 extends UserStorage {
    private final Map<String, ItemDefinition> identifierToDefinition = new HashMap<>();

    public ItemRemappingStorage_v844(UserSession session) {
        super(session);
    }

    public void put(String identifier, ItemDefinition definition) {
        this.identifierToDefinition.put(identifier, definition);
    }

    public ItemDefinition getDefinition(String identifier) {
        return this.identifierToDefinition.get(identifier);
    }
}
