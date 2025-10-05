package oxy.toviabedrock;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v748.Bedrock_v748;
import org.cloudburstmc.protocol.bedrock.codec.v766.Bedrock_v766;
import org.cloudburstmc.protocol.bedrock.codec.v776.Bedrock_v776;
import org.cloudburstmc.protocol.bedrock.codec.v786.Bedrock_v786;
import org.cloudburstmc.protocol.bedrock.codec.v800.Bedrock_v800;
import org.cloudburstmc.protocol.bedrock.codec.v818.Bedrock_v818;
import org.cloudburstmc.protocol.bedrock.codec.v819.Bedrock_v819;
import org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.protocols.v766to748.Protocol766to748;
import oxy.toviabedrock.protocols.v776to766.Protocol776to766;
import oxy.toviabedrock.protocols.v786to776.Protocol786to776;
import oxy.toviabedrock.protocols.v800to786.Protocol800to786;
import oxy.toviabedrock.protocols.v818to800.Protocol818to800;
import oxy.toviabedrock.protocols.v827to818and819.Protocol827to819;
import oxy.toviabedrock.protocols.v844to827.Protocol844to827;

import java.util.*;
import java.util.logging.Logger;

public class ToViaBedrock {
    private static final Logger LOGGER = Logger.getLogger("To-ViaBedrock");
    private static final TreeMap<Integer, ProtocolToProtocol> SUPPORTED_PROTOCOLS = new TreeMap<>();

    public static void init() {
        // TODO: Finish these. Since I kinda want to see how this works when join latest version.
//        SUPPORTED_PROTOCOLS.put(Bedrock_v261.CODEC.getProtocolVersion(), new Protocol274to261());
//        SUPPORTED_PROTOCOLS.put(Bedrock_v274.CODEC.getProtocolVersion(), new Protocol282to274());
//        SUPPORTED_PROTOCOLS.put(Bedrock_v291.CODEC.getProtocolVersion(), new Protocol291to282());

        SUPPORTED_PROTOCOLS.put(Bedrock_v827.CODEC.getProtocolVersion(), new Protocol844to827());
        SUPPORTED_PROTOCOLS.put(Bedrock_v819.CODEC.getProtocolVersion(), new Protocol827to819(Bedrock_v827.CODEC, Bedrock_v819.CODEC));
        SUPPORTED_PROTOCOLS.put(Bedrock_v818.CODEC.getProtocolVersion(), new Protocol827to819(Bedrock_v819.CODEC, Bedrock_v818.CODEC));
        SUPPORTED_PROTOCOLS.put(Bedrock_v800.CODEC.getProtocolVersion(), new Protocol818to800());
        SUPPORTED_PROTOCOLS.put(Bedrock_v786.CODEC.getProtocolVersion(), new Protocol800to786());
        SUPPORTED_PROTOCOLS.put(Bedrock_v776.CODEC.getProtocolVersion(), new Protocol786to776());
        SUPPORTED_PROTOCOLS.put(Bedrock_v766.CODEC.getProtocolVersion(), new Protocol776to766());
        SUPPORTED_PROTOCOLS.put(Bedrock_v748.CODEC.getProtocolVersion(), new Protocol766to748());
    }

    public static List<ProtocolToProtocol> getTranslators(int target, int client) {
        if (target == client) {
            return List.of();
        }

        final List<ProtocolToProtocol> translators = new ArrayList<>();
        for (Map.Entry<Integer, ProtocolToProtocol> protocol : SUPPORTED_PROTOCOLS.descendingMap().entrySet()) {
            final int protocolVersion = protocol.getKey();
            if (protocolVersion < client) {
                break;
            }

            translators.add(protocol.getValue());
        }

        return translators;
    }

    public static BedrockCodec getCodec(int protocolVersion) {
        final BedrockCodec CODEC = SUPPORTED_PROTOCOLS.get(protocolVersion).getTranslatedCodec();
        final BedrockCodecHelper helper = CODEC.createHelper();
        helper.setEncodingSettings(EncodingSettings.builder()
                .maxListSize(Integer.MAX_VALUE)
                .maxByteArraySize(Integer.MAX_VALUE)
                .maxNetworkNBTSize(Integer.MAX_VALUE)
                .maxItemNBTSize(Integer.MAX_VALUE)
                .maxStringLength(Integer.MAX_VALUE)
                .build());

        return CODEC.toBuilder().helper(() -> helper).build();
    }

    public static boolean isSupported(int protocolVersion) {
        return SUPPORTED_PROTOCOLS.containsKey(protocolVersion);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
