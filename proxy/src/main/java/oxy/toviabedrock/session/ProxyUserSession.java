package oxy.toviabedrock.session;

import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import oxy.toviabedrock.ToViaBedrock;
import oxy.toviabedrock.base.ProtocolToProtocol;

import java.util.List;


public class ProxyUserSession extends UserSession {
    private final BedrockServerSession upstreamSession;
    @Setter
    private BedrockClientSession downstreamSession;

    @Getter
    private final List<ProtocolToProtocol> translators;

    public ProxyUserSession(int protocolVersion, int targetVersion, BedrockServerSession upstreamSession) {
        super(protocolVersion);
        this.upstreamSession = upstreamSession;

        this.translators = ToViaBedrock.getTranslators(targetVersion, protocolVersion);
    }

    @Override
    public void sendUpstreamPacket(BedrockPacket packet, boolean immediately) {
        if (immediately) {
            this.upstreamSession.sendPacketImmediately(packet);
        } else {
            this.upstreamSession.sendPacket(packet);
        }
    }

    @Override
    public void sendDownstreamPacket(BedrockPacket packet, boolean immediately) {
        if (immediately) {
            this.downstreamSession.sendPacketImmediately(packet);
        } else {
            this.downstreamSession.sendPacket(packet);
        }
    }

    public BedrockPeer getUpstreamPeer() {
        return this.upstreamSession.getPeer();
    }

    public void disconnect(CharSequence reason) {
        this.downstreamSession.disconnect(reason);
    }
}
