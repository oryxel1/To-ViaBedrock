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

    public void init(UserSession session) {
    }

    public void unmapDirectlyClientbound(BedrockPacketType type) {
        this.translator.unmapDirectlyClientbound(type);
    }

    public void unmapDirectlyServerbound(BedrockPacketType type) {
        this.translator.unmapDirectlyServerbound(type);
    }

    public void mapDirectlyClientbound(BedrockPacketType type) {
        this.translator.mapDirectlyClientbound(type);
    }

    public void mapDirectlyServerbound(BedrockPacketType type) {
        this.translator.mapDirectlyServerbound(type);
    }

    public void ignoreClientbound(Class<? extends BedrockPacket> klass) {
        this.translator.ignoreClientbound(klass);
    }

    public void ignoreServerbound(Class<? extends BedrockPacket> klass) {
        this.translator.ignoreServerbound(klass);
    }

    public void registerClientbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.translator.registerClientbound(klass, consumer);
    }

    public void registerServerbound(Class<? extends BedrockPacket> klass, Consumer<WrappedBedrockPacket> consumer) {
        this.translator.registerServerbound(klass, consumer);
    }
}
