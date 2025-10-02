package oxy.toviabedrock.base;

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import oxy.toviabedrock.ToViaBedrock;
import oxy.toviabedrock.session.UserSession;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ProtocolToProtocol {
    private final BedrockCodec originalCodec;
    private final BedrockCodec translatedCodec;

    private final Map<Class<? extends BedrockPacket>, Consumer<WrappedBedrockPacket>> mappedClientBounds = new HashMap<>();
    private final Map<Class<? extends BedrockPacket>, Consumer<WrappedBedrockPacket>> mappedServerBounds = new HashMap<>();

    public void init(UserSession session) {}

    public void mapClientboundDirectly(Class<? extends BedrockPacket> klass) {
        this.mappedClientBounds.put(klass, null);
    }

    public void mapServerboundDirectly(Class<? extends BedrockPacket> klass) {
        this.mappedServerBounds.put(klass, null);
    }

    public void registerClientbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.mappedClientBounds.put(klass, consumer);
    }

    public void registerServerbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.mappedServerBounds.put(klass, consumer);
    }

    public BedrockPacket passthroughClientbound(UserSession session, BedrockPacket packet) {
        final Consumer<WrappedBedrockPacket> translator = this.mappedClientBounds.get(packet.getClass());
        if (translator == null) {
            ToViaBedrock.getLogger().warning("Server sent an packet that we don't have a translator for: " + packet.getClass() + "!");
            return null;
        }

        final WrappedBedrockPacket wrapped = new WrappedBedrockPacket(session, packet, false);
        translator.accept(wrapped);
        return wrapped.isCancelled() ? null : wrapped.getPacket();
    }

    public BedrockPacket passthroughServerbound(UserSession session, BedrockPacket packet) {
        final Consumer<WrappedBedrockPacket> translator = this.mappedServerBounds.get(packet.getClass());
        if (translator == null) {
            ToViaBedrock.getLogger().warning("Client sent an packet that we don't have a translator for: " + packet.getClass() + "!");
            return null;
        }

        final WrappedBedrockPacket wrapped = new WrappedBedrockPacket(session, packet, false);
        translator.accept(wrapped);
        return wrapped.isCancelled() ? null : wrapped.getPacket();
    }
}
