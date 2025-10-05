package oxy.toviabedrock.chunk.base;

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import oxy.toviabedrock.session.UserSession;

@RequiredArgsConstructor
public class WorldReaderBase {
    protected final UserSession session;

    public void onClientbound(BedrockPacket packet) {}
    public void onServerbound(BedrockPacket packet) {}
}
