package oxy.toviabedrock.base;

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import oxy.toviabedrock.session.UserSession;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class Mapper {
    protected final ProtocolToProtocol translator;

    protected void registerProtocol() {
    }

    protected void init(UserSession session) {
    }

    protected void unmapDirectlyClientbound(BedrockPacketType type) {
        this.translator.unmapDirectlyClientbound(type);
    }

    protected void unmapDirectlyServerbound(BedrockPacketType type) {
        this.translator.unmapDirectlyServerbound(type);
    }

    protected void mapDirectlyClientbound(BedrockPacketType type) {
        this.translator.mapDirectlyClientbound(type);
    }

    protected void mapDirectlyServerbound(BedrockPacketType type) {
        this.translator.mapDirectlyServerbound(type);
    }

    protected void ignoreClientbound(Class<? extends BedrockPacket> klass) {
        this.translator.ignoreClientbound(klass);
    }

    protected void ignoreServerbound(Class<? extends BedrockPacket> klass) {
        this.translator.ignoreServerbound(klass);
    }

    protected void registerClientbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.translator.registerClientbound(klass, consumer);
    }

    protected void registerServerbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.translator.registerServerbound(klass, consumer);
    }
}
