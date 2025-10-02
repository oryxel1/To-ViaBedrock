package oxy.toviabedrock;

import org.cloudburstmc.protocol.bedrock.codec.v291.Bedrock_v291;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.codec.v261.Bedrock_v261;
import oxy.toviabedrock.codec.v274.Bedrock_v274;
import oxy.toviabedrock.protocols.v274to261.Protocol274to261;
import oxy.toviabedrock.protocols.v282to274.Protocol282to274;
import oxy.toviabedrock.protocols.v291to282.Protocol291to282;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ToViaBedrock {
    private static final Logger LOGGER = Logger.getLogger("To-ViaBedrock");
    private static final Map<Integer, Class<? extends ProtocolToProtocol>> SUPPORTED_PROTOCOLS = new HashMap<>();

    public void init() {
        SUPPORTED_PROTOCOLS.put(Bedrock_v261.CODEC.getProtocolVersion(), Protocol274to261.class);
        SUPPORTED_PROTOCOLS.put(Bedrock_v274.CODEC.getProtocolVersion(), Protocol282to274.class);
        SUPPORTED_PROTOCOLS.put(Bedrock_v291.CODEC.getProtocolVersion(), Protocol291to282.class);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
