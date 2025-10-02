package oxy.toviabedrock;

import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.codec.v261.Bedrock_v261;
import oxy.toviabedrock.protocols.v261tov291.Protocol261To291;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ToViaBedrock {
    private static final Logger LOGGER = Logger.getLogger("To-ViaBedrock");
    private static final Map<Integer, Class<? extends ProtocolToProtocol>> SUPPORTED_PROTOCOLS = new HashMap<>();

    public void init() {
        SUPPORTED_PROTOCOLS.put(Bedrock_v261.CODEC.getProtocolVersion(), Protocol261To291.class);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
