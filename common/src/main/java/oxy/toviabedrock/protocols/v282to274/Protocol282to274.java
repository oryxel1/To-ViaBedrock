package oxy.toviabedrock.protocols.v282to274;

import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.codec.v274.Bedrock_v274;
import oxy.toviabedrock.codec.v282.Bedrock_v282;

import java.util.Arrays;

public class Protocol282to274 extends ProtocolToProtocol {
    public Protocol282to274() {
        super(Bedrock_v282.CODEC, Bedrock_v274.CODEC);
    }

    @Override
    public void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);
        
        this.ignoreClientbound(PhotoTransferPacket.class);
        this.ignoreClientbound(ShowProfilePacket.class);
        this.ignoreClientbound(SetDefaultGameTypePacket.class);
        this.ignoreClientbound(RemoveObjectivePacket.class);
        this.ignoreClientbound(SetDisplayObjectivePacket.class);
        this.ignoreClientbound(SetScorePacket.class);
        this.ignoreClientbound(LabTablePacket.class);
        this.ignoreClientbound(SetScoreboardIdentityPacket.class);
        this.ignoreClientbound(UpdateSoftEnumPacket.class);

        this.registerClientbound(NetworkStackLatencyPacket.class, wrapped -> {
            // TODO: Figure out the magnitude and translate this.
        });
    }
}
