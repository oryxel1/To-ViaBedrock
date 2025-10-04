package oxy.toviabedrock.base.mappers.storage;

import oxy.toviabedrock.base.mappers.BaseEntityMapper;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;

public class BaseEntityRemappingStorage extends UserStorage {
    private final Map<Long, Long> uniqueIdToRuntimeId = new HashMap<>();
    private final Map<Long, BaseEntityMapper.MappedEntity> runtimeIdToRealIdentifier = new HashMap<>();

    public BaseEntityRemappingStorage(UserSession session) {
        super(session);
    }

    public void remove(long id) {
        this.runtimeIdToRealIdentifier.remove(this.uniqueIdToRuntimeId.remove(id));
    }

    public void add(long runtimeId, long uniqueId, String identifier, boolean show) {
        this.runtimeIdToRealIdentifier.put(runtimeId, new BaseEntityMapper.MappedEntity(identifier, show));
        this.uniqueIdToRuntimeId.put(uniqueId, runtimeId);
    }

    public BaseEntityMapper.MappedEntity getIdentifier(long id) {
        return this.runtimeIdToRealIdentifier.get(id);
    }
}
