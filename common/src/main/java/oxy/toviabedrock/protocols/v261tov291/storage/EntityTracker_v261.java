package oxy.toviabedrock.protocols.v261tov291.storage;

import org.cloudburstmc.math.vector.Vector3f;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;

public class EntityTracker_v261 extends UserStorage {
    private final Map<Long, Vector3f> entities = new HashMap<>();
    public EntityTracker_v261(UserSession session) {
        super(session);
    }


}
