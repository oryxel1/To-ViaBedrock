package oxy.toviabedrock.session;

import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

public class ProxyUserSession extends UserSession {
    private final BedrockServerSession upstreamSession;
    @Setter
    private BedrockClientSession downstreamSession;

    public ProxyUserSession(int protocolVersion, int targetVersion, BedrockServerSession upstreamSession) {
        super(protocolVersion, targetVersion);
        this.upstreamSession = upstreamSession;
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
