package oxy.toviabedrock.session;

import lombok.Getter;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import oxy.toviabedrock.ToViaBedrock;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.WrappedBedrockPacket;
import oxy.toviabedrock.chunk.WorldTracker;
import oxy.toviabedrock.chunk.base.WorldReaderBase;
import oxy.toviabedrock.chunk.impl.WorldReader_v844;
import oxy.toviabedrock.session.storage.UserStorage;
import oxy.toviabedrock.session.storage.impl.GameSessionStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class UserSession {
    @Getter
    private final int protocolVersion;
    private final Map<Class<?>, UserStorage> storages = new HashMap<>();

    @Getter
    private final List<ProtocolToProtocol> translators;

    @Getter
    private final WorldReaderBase worldReader;

    protected UserSession(int protocolVersion, int serverVersion) {
        this.protocolVersion = protocolVersion;
        this.translators = ToViaBedrock.getTranslators(serverVersion, protocolVersion);

        // Default storages.
        this.put(new GameSessionStorage(this));
        this.put(new WorldTracker(this));

        this.worldReader = new WorldReader_v844(this);
    }

    public final BedrockPacket translateClientbound(BedrockPacket packet) {
        final WrappedBedrockPacket wrapped = new WrappedBedrockPacket(this, packet, false);
        for (ProtocolToProtocol translator : this.translators) {
            if (!translator.passthroughClientbound(wrapped)) {
                return null;
            }
        }
        return wrapped.isCancelled() ? null : wrapped.getPacket();
    }

    public final BedrockPacket translateServerbound(BedrockPacket packet) {
        final WrappedBedrockPacket wrapped = new WrappedBedrockPacket(this, packet, false);
        for (ProtocolToProtocol translator : this.translators) {
            if (!translator.passthroughServerbound(wrapped)) {
                return null;
            }
        }
        return wrapped.isCancelled() ? null : wrapped.getPacket();
    }

    public abstract void sendUpstreamPacket(BedrockPacket packet, boolean immediately);
    public abstract void sendDownstreamPacket(BedrockPacket packet, boolean immediately);

    public final void put(UserStorage storage) {
        this.storages.put(storage.getClass(), storage);
    }

    @SuppressWarnings("unchecked")
    public final <T extends UserStorage> T get(Class<T> klass) {
        return (T) this.storages.get(klass);
    }
}
