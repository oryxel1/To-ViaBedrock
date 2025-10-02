package oxy.toviabedrock.base;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import oxy.toviabedrock.ToViaBedrock;
import oxy.toviabedrock.session.UserSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProtocolToProtocol {
    private final BedrockCodec originalCodec;
    private final BedrockCodec translatedCodec;

    private final List<BedrockPacketType> directPassthroughClientBounds = new ArrayList<>();
    private final List<BedrockPacketType> directPassthroughServerBounds = new ArrayList<>();
    private final Map<Class<? extends BedrockPacket>, Consumer<WrappedBedrockPacket>> mappedClientBounds = new HashMap<>();
    private final Map<Class<? extends BedrockPacket>, Consumer<WrappedBedrockPacket>> mappedServerBounds = new HashMap<>();

    public ProtocolToProtocol(BedrockCodec originalCodec, BedrockCodec translatedCodec) {
        this.originalCodec = originalCodec;
        this.translatedCodec = translatedCodec;

        this.registerProtocol();
    }

    public void init(UserSession session) {}
    public void registerProtocol() {}

    public void unmapDirectlyClientbound(BedrockPacketType type) {
        this.directPassthroughClientBounds.remove(type);
    }

    public void unmapDirectlyServerbound(BedrockPacketType type) {
        this.directPassthroughServerBounds.remove(type);
    }

    public void mapDirectlyClientbound(BedrockPacketType type) {
        this.directPassthroughClientBounds.add(type);
    }

    public void mapDirectlyServerbound(BedrockPacketType type) {
        this.directPassthroughServerBounds.add(type);
    }

    public void ignoreClientbound(Class<? extends BedrockPacket> klass) {
        this.mappedClientBounds.put(klass, null);
    }

    public void ignoreServerbound(Class<? extends BedrockPacket> klass) {
        this.mappedServerBounds.put(klass, null);
    }

    public void registerClientbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.mappedClientBounds.put(klass, consumer);
    }

    public void registerServerbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.mappedServerBounds.put(klass, consumer);
    }

    public BedrockPacket passthroughClientbound(UserSession session, BedrockPacket packet) {
        final boolean exist = this.mappedServerBounds.containsKey(packet.getClass());
        final Consumer<WrappedBedrockPacket> translator = this.mappedClientBounds.get(packet.getClass());
        if (translator == null) {
            if (!exist) {
                if (this.directPassthroughClientBounds.contains(packet.getPacketType())) {
                    return packet;
                }

                ToViaBedrock.getLogger().warning("Server sent an packet that we don't have a translator for: " + packet.getClass() + "!");
            }
            return null;
        }

        final WrappedBedrockPacket wrapped = new WrappedBedrockPacket(session, packet, false);
        translator.accept(wrapped);
        return wrapped.isCancelled() ? null : wrapped.getPacket();
    }

    public BedrockPacket passthroughServerbound(UserSession session, BedrockPacket packet) {
        final boolean exist = this.mappedServerBounds.containsKey(packet.getClass());
        final Consumer<WrappedBedrockPacket> translator = this.mappedServerBounds.get(packet.getClass());
        if (translator == null) {
            if (!exist) {
                if (this.directPassthroughServerBounds.contains(packet.getPacketType())) {
                    return packet;
                }

                ToViaBedrock.getLogger().warning("Client sent an packet that we don't have a translator for: " + packet.getClass() + "!");
            }
            return null;
        }

        final WrappedBedrockPacket wrapped = new WrappedBedrockPacket(session, packet, false);
        translator.accept(wrapped);
        return wrapped.isCancelled() ? null : wrapped.getPacket();
    }
}
