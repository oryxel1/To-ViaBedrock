package oxy.toviabedrock.mappers.storage;

import oxy.toviabedrock.mappers.EntityMapper_v844;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;

public class EntityRemappingStorage_v844 extends UserStorage {
    private final Map<Long, Long> uniqueIdToRuntimeId = new HashMap<>();
    private final Map<Long, EntityMapper_v844.MappedEntity> runtimeIdToRealIdentifier = new HashMap<>();

    public EntityRemappingStorage_v844(UserSession session) {
        super(session);
    }

    public void remove(long id) {
        this.runtimeIdToRealIdentifier.remove(this.uniqueIdToRuntimeId.remove(id));
    }

    public void add(long runtimeId, long uniqueId, String identifier, boolean show) {
        this.runtimeIdToRealIdentifier.put(runtimeId, new EntityMapper_v844.MappedEntity(identifier, show));
        this.uniqueIdToRuntimeId.put(uniqueId, runtimeId);
    }

    public EntityMapper_v844.MappedEntity getIdentifier(long id) {
        return this.runtimeIdToRealIdentifier.get(id);
    }
}
