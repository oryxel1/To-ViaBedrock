package oxy.toviabedrock.handler;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.bedrock.util.ChainValidationResult;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;
import org.cloudburstmc.protocol.bedrock.util.JsonUtils;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.jose4j.json.JsonUtil;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import oxy.toviabedrock.ToViaBedrock;
import oxy.toviabedrock.ToViaBedrockProxy;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.WrappedBedrockPacket;
import oxy.toviabedrock.session.ProxyUserSession;
import oxy.toviabedrock.session.storage.DownstreamStorage;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.Map;

public class UpstreamPacketHandler implements BedrockPacketHandler {
    public static final ObjectMapper JSON_MAPPER;
    static {
        JSON_MAPPER = new ObjectMapper().registerModule(new SimpleModule("To-ViaBedrock", Version.unknownVersion())).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private final BedrockServerSession session;
    private ProxyUserSession user;

    private boolean hasReceivedNetworkSettings = false;

    public UpstreamPacketHandler(BedrockServerSession session) {
        this.session = session;
    }

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        PacketSignal signal = packet.handle(this);
        if (signal == PacketSignal.HANDLED) {
            return PacketSignal.HANDLED; // We already handled this, don't pass it to the translator.
        }
        final WrappedBedrockPacket wrapped = new WrappedBedrockPacket(this.user, packet, false);
        for (ProtocolToProtocol translator : this.user.getTranslators()) {
            if (!translator.passthroughServerbound(wrapped)) {
                return PacketSignal.HANDLED;
            }
        }
        packet = wrapped.getPacket();

        if (!wrapped.isCancelled()) {
//            System.out.println("Passthrough serverbound: " + packet.getPacketType());
            this.user.sendDownstreamPacket(packet, false);
        }
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(LoginPacket packet) {
        // Player have to send a request network settings packet first to login since 1.19.30.
        if (!this.hasReceivedNetworkSettings && packet.getProtocolVersion() >= 554) {
            session.disconnect("Failed to validate login.");
            return PacketSignal.HANDLED;
        }

        // On older version, we need to cache the user and check protocol version here.
        if (!this.hasReceivedNetworkSettings && !validateVersionAndCacheUser(packet.getProtocolVersion())) {
            return PacketSignal.HANDLED;
        }

        final JSONObject extraData, skinData;
        // Let's validate the player login, even if the target server allow offline mode.
        try {
            ChainValidationResult result = EncryptionUtils.validatePayload(packet.getAuthPayload());
            if (!result.signed()) {
                session.disconnect("Please login into Xbox to join this server!");
            }

            JsonNode payload = JSON_MAPPER.valueToTree(result.rawIdentityClaims());

            if (payload.get("extraData").getNodeType() != JsonNodeType.OBJECT) {
                throw new RuntimeException("AuthData was not found!");
            }

            extraData = new JSONObject(JsonUtils.childAsType(result.rawIdentityClaims(), "extraData", Map.class));

            if (payload.get("identityPublicKey").getNodeType() != JsonNodeType.STRING) {
                throw new RuntimeException("Identity Public Key was not found!");
            }
            ECPublicKey identityPublicKey = EncryptionUtils.parseKey(payload.get("identityPublicKey").textValue());

            String clientJwt = packet.getClientJwt();
            verifyJwt(clientJwt, identityPublicKey);
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(clientJwt);

            skinData = new JSONObject(JsonUtil.parseJson(jws.getUnverifiedPayload()));
        } catch (Exception e) {
            session.disconnect("Failed to validate login.");
            return PacketSignal.HANDLED;
        }

        // Player finished authentication! Now let's allow the player to join the server.
        this.user.get(DownstreamStorage.class).startDownstreamOffline(extraData, skinData);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(RequestNetworkSettingsPacket packet) {
        if (!validateVersionAndCacheUser(packet.getProtocolVersion())) {
            return PacketSignal.HANDLED;
        }

        // Response with a network settings packet.
        NetworkSettingsPacket networkSettingsPacket = new NetworkSettingsPacket();
        networkSettingsPacket.setCompressionThreshold(0);
        networkSettingsPacket.setCompressionAlgorithm(PacketCompressionAlgorithm.ZLIB);
        session.sendPacketImmediately(networkSettingsPacket);
        session.setCompression(PacketCompressionAlgorithm.ZLIB);

        this.hasReceivedNetworkSettings = true;

        return PacketSignal.HANDLED;
    }

    @Override
    public void onDisconnect(CharSequence reason) {
        this.user.disconnect(reason);
    }

    @SuppressWarnings("ALL")
    private boolean validateVersionAndCacheUser(int protocolVersion) {
        if (!ToViaBedrock.isSupported(protocolVersion)) {
            session.disconnect("Your version is not supported? PV=" + protocolVersion + "! (To-VB)");
            return false;
        }

        session.setCodec(ToViaBedrock.getCodec(protocolVersion));
        this.user = new ProxyUserSession(protocolVersion, ToViaBedrockProxy.getProxyRun().version().getProtocolVersion(), this.session);
        this.user.put(new DownstreamStorage(this.user));
        this.user.getTranslators().forEach(translator -> translator.init(this.user));
        return true;
    }

    @SuppressWarnings("ALL")
    private static boolean verifyJwt(String jwt, PublicKey key) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setKey(key);
        jws.setCompactSerialization(jwt);
        return jws.verifySignature();
    }
}
