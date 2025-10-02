package oxy.toviabedrock;

import oxy.toviabedrock.base.ProtocolToProtocol;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ToViaBedrock {
    private static final Logger LOGGER = Logger.getLogger("To-ViaBedrock");
    private static final Map<Integer, Class<? extends ProtocolToProtocol>> SUPPORTED_PROTOCOLS = new HashMap<>();

    public void init() {
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
