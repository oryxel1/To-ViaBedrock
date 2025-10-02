package oxy.toviabedrock.protocols.v274to261.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityDeltaPacket;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.UserStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker_v261 extends UserStorage {
    private final Map<Long, EntityCache> entities = new ConcurrentHashMap<>();
    public EntityTracker_v261(UserSession session) {
        super(session);
    }

    public void cache(long entityId, Vector3f position, Vector3f rotation) {
        this.entities.put(entityId, new EntityCache(position, rotation));
    }

    public void remove(long entityId) {
        this.entities.remove(entityId);
    }

    public void moveRelative(long entityId, MoveEntityDeltaPacket delta) {
        final EntityCache entity = this.entities.get(entityId);
        if (entity == null) {
            return;
        }

        float posX = delta.getFlags().contains(MoveEntityDeltaPacket.Flag.HAS_X) ? entity.position.getX() : translateValue(entity.position.getX(), delta.getDeltaX());
        float posY = delta.getFlags().contains(MoveEntityDeltaPacket.Flag.HAS_Y) ? entity.position.getY() : translateValue(entity.position.getY(), delta.getDeltaY());
        float posZ = delta.getFlags().contains(MoveEntityDeltaPacket.Flag.HAS_Z) ? entity.position.getZ() : translateValue(entity.position.getZ(), delta.getDeltaZ());

        float pitch = delta.getFlags().contains(MoveEntityDeltaPacket.Flag.HAS_PITCH) ? delta.getPitch() : entity.rotation.getX();
        float yaw = delta.getFlags().contains(MoveEntityDeltaPacket.Flag.HAS_YAW) ? delta.getYaw() : entity.rotation.getY();
        float headYaw = delta.getFlags().contains(MoveEntityDeltaPacket.Flag.HAS_HEAD_YAW) ? delta.getHeadYaw() : entity.rotation.getZ();

        entity.setPosition(Vector3f.from(posX, posY, posZ));
        entity.setRotation(Vector3f.from(pitch, yaw, headYaw));
    }

    public void moveAbsolute(long entityId, Vector3f position, Vector3f rotation) {
        final EntityCache entity = this.entities.get(entityId);
        if (this.entities.get(entityId) == null) {
            return;
        }

        entity.setPosition(position);
        entity.setRotation(rotation);
    }

    public EntityCache getEntity(long entityId) {
        return this.entities.get(entityId);
    }

    private float translateValue(float value, int delta) {
        return Float.intBitsToFloat(delta + Float.floatToIntBits(Math.round(value * 100.0) / 100.0f));
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class EntityCache {
        private Vector3f position, rotation;
    }
}