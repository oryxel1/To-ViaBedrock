package oxy.toviabedrock.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import oxy.toviabedrock.session.storage.UserStorage;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public abstract class UserSession {
    @Getter
    private final int protocolVersion;
    private final Map<Class<?>, UserStorage> storages = new HashMap<>();

    public abstract void sendUpstreamPacket(BedrockPacket packet, boolean immediately);
    public abstract void sendDownstreamPacket(BedrockPacket packet, boolean immediately);

    public void put(UserStorage storage) {
        this.storages.put(storage.getClass(), storage);
    }

    @SuppressWarnings("unchecked")
    public <T extends UserStorage> T get(Class<T> klass) {
        return (T) this.storages.get(klass);
    }
}
