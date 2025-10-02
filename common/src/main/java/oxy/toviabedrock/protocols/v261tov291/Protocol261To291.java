package oxy.toviabedrock.protocols.v261tov291;

import org.cloudburstmc.protocol.bedrock.codec.v291.Bedrock_v291;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.codec.v261.Bedrock_v261;

public class Protocol261To291 extends ProtocolToProtocol {
    public Protocol261To291() {
        super(Bedrock_v291.CODEC, Bedrock_v261.CODEC);
    }


}
