package oxy.toviabedrock.base;

import lombok.Getter;
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
    @Getter
    private final BedrockCodec originalCodec;
    @Getter
    private final BedrockCodec translatedCodec;

    private final List<Class<? extends BedrockPacket>> ignoredClientBounds = new ArrayList<>();
    private final List<Class<? extends BedrockPacket>> ignoredServerBounds = new ArrayList<>();
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
    protected void registerProtocol() {}

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
        this.ignoredClientBounds.add(klass);
    }

    public void ignoreServerbound(Class<? extends BedrockPacket> klass) {
        this.ignoredServerBounds.add(klass);
    }

    public void registerClientbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        if (this.mappedClientBounds.containsKey(klass)) {
            this.mappedClientBounds.put(klass, this.mappedClientBounds.get(klass).andThen(consumer));
            return;
        }

        this.mappedClientBounds.put(klass, consumer);
    }

    public void registerServerbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        if (this.mappedServerBounds.containsKey(klass)) {
            this.mappedServerBounds.put(klass, this.mappedServerBounds.get(klass).andThen(consumer));
            return;
        }

        this.mappedServerBounds.put(klass, consumer);
    }

    public boolean passthroughClientbound(final WrappedBedrockPacket wrapped) {
        final Consumer<WrappedBedrockPacket> translator = this.mappedClientBounds.get(wrapped.getPacket().getClass());
        if (translator == null) {
            if (!this.ignoredClientBounds.contains(wrapped.getPacket().getClass())) {
                if (this.directPassthroughClientBounds.contains(wrapped.getPacket().getPacketType())) {
                    return true;
                }

                ToViaBedrock.getLogger().warning("Server sent an packet that we don't have a translator for: " + wrapped.getPacket().getClass() + "!");
            }
            return false;
        }

        translator.accept(wrapped);
        return true;
    }

    public boolean passthroughServerbound(final WrappedBedrockPacket wrapped) {
        final Consumer<WrappedBedrockPacket> translator = this.mappedServerBounds.get(wrapped.getPacket().getClass());
        if (translator == null) {
            if (!this.ignoredServerBounds.contains(wrapped.getPacket().getClass())) {
                if (this.directPassthroughServerBounds.contains(wrapped.getPacket().getPacketType())) {
                    return true;
                }

                ToViaBedrock.getLogger().warning("Client sent an packet that we don't have a translator for: " + wrapped.getPacket().getClass() + "!");
            }
            return false;
        }

        translator.accept(wrapped);
        return true;
    }
}
