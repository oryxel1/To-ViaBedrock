package oxy.toviabedrock.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import oxy.toviabedrock.session.UserSession;

@AllArgsConstructor
@Getter @Setter
public class WrappedBedrockPacket {
    private final UserSession session;
    private BedrockPacket packet;
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }
    public UserSession session() {
        return this.session;
    }
}
