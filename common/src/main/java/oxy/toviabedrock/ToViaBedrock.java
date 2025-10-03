package oxy.toviabedrock;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.protocols.v844to827.Protocol844to827;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ToViaBedrock {
    private static final Logger LOGGER = Logger.getLogger("To-ViaBedrock");
    private static final Map<Integer, ProtocolToProtocol> SUPPORTED_PROTOCOLS = new HashMap<>();

    // WARNING: THESE HAVE TO BE ADDED IN ORDER, FROM LATEST TO OLDEST.
    public static void init() {
        // TODO: Finish these. Since I kinda want to see how this works when join latest version.
//        SUPPORTED_PROTOCOLS.put(Bedrock_v261.CODEC.getProtocolVersion(), new Protocol274to261());
//        SUPPORTED_PROTOCOLS.put(Bedrock_v274.CODEC.getProtocolVersion(), new Protocol282to274());
//        SUPPORTED_PROTOCOLS.put(Bedrock_v291.CODEC.getProtocolVersion(), new Protocol291to282());

        SUPPORTED_PROTOCOLS.put(Bedrock_v827.CODEC.getProtocolVersion(), new Protocol844to827());
    }

    public static List<ProtocolToProtocol> getTranslators(int target, int client) {
        if (target == client) {
            return List.of();
        }

        final List<ProtocolToProtocol> translators = new ArrayList<>();
        for (Map.Entry<Integer, ProtocolToProtocol> protocol : SUPPORTED_PROTOCOLS.entrySet()) {
            final int protocolVersion = protocol.getKey();
            if (protocolVersion < client) {
                break;
            }

            translators.add(protocol.getValue());
        }

        return translators;
    }

    public static BedrockCodec getCodec(int protocolVersion) {
        return SUPPORTED_PROTOCOLS.get(protocolVersion).getTranslatedCodec();
    }

    public static boolean isSupported(int protocolVersion) {
        return SUPPORTED_PROTOCOLS.containsKey(protocolVersion);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
