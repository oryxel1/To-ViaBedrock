package oxy.toviabedrock.handler;

import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;
import org.cloudburstmc.protocol.bedrock.util.JsonUtils;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;
import org.jose4j.json.JsonUtil;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;
import oxy.toviabedrock.ToViaBedrock;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.WrappedBedrockPacket;
import oxy.toviabedrock.base.registry.BlockDefinitionRegistryMapper;
import oxy.toviabedrock.base.registry.UnknownBlockDefinitionRegistry;
import oxy.toviabedrock.session.ProxyUserSession;
import oxy.toviabedrock.session.storage.DownstreamStorage;
import oxy.toviabedrock.session.storage.impl.GameSessionStorage;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@RequiredArgsConstructor
public class DownstreamPacketHandler implements BedrockPacketHandler {
    private final BedrockClientSession session;
    private final ProxyUserSession user;

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        PacketSignal signal = packet.handle(this);
        if (signal == PacketSignal.HANDLED) {
            return PacketSignal.HANDLED; // We already handled this, don't pass it to the translator.
        }
        packet = this.user.translateClientbound(packet);

        if (packet != null) {
//            System.out.println("Passthrough clientbound: " + packet.getPacketType());
            this.user.sendUpstreamPacket(packet, false);
        }
        return PacketSignal.HANDLED;
    }

    // Direct this to the client, so the client and also login in.
    @Override
    public PacketSignal handle(PlayStatusPacket packet) {
        this.user.sendUpstreamPacket(packet, false);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(StartGamePacket packet) {
        final GameSessionStorage gameSession = this.user.get(GameSessionStorage.class);
        gameSession.setBlockNetworkIdsHashed(packet.isBlockNetworkIdsHashed());

        if (this.session.getCodec().getProtocolVersion() < 776) {
            SimpleDefinitionRegistry<ItemDefinition> itemDefinitions = SimpleDefinitionRegistry.<ItemDefinition>builder()
                    .addAll(packet.getItemDefinitions())
                    .add(new SimpleItemDefinition("minecraft:empty", 0, false))
                    .build();

            this.session.getPeer().getCodecHelper().setItemDefinitions(itemDefinitions);
            this.user.getUpstreamPeer().getCodecHelper().setItemDefinitions(itemDefinitions);
        }

        // We have to set this up so we can translate the block ids....
        session.getPeer().getCodecHelper().setBlockDefinitions(new BlockDefinitionRegistryMapper(this.user));
        this.user.getUpstreamPeer().getCodecHelper().setBlockDefinitions(new UnknownBlockDefinitionRegistry());

        return PacketSignal.UNHANDLED;
    }

    @Override
    public PacketSignal handle(ItemComponentPacket packet) {
        if (this.session.getCodec().getProtocolVersion() >= 776) {
            SimpleDefinitionRegistry.Builder<ItemDefinition> builder = SimpleDefinitionRegistry.<ItemDefinition>builder()
                    .add(new SimpleItemDefinition("minecraft:empty", 0, false));

            for (ItemDefinition definition : packet.getItems()) {
                builder.add(new SimpleItemDefinition(definition.getIdentifier(), definition.getRuntimeId(), definition.isComponentBased()));
            }

            SimpleDefinitionRegistry<ItemDefinition> itemDefinitions = builder.build();
            this.session.getPeer().getCodecHelper().setItemDefinitions(itemDefinitions);
            this.user.getUpstreamPeer().getCodecHelper().setItemDefinitions(itemDefinitions);
        }

        return PacketSignal.UNHANDLED;
    }

    @Override
    public PacketSignal handle(NetworkSettingsPacket packet) {
        this.session.setCompression(packet.getCompressionAlgorithm());
        ToViaBedrock.getLogger().info("Compression algorithm picked " + packet.getCompressionAlgorithm());

        this.session.sendPacketImmediately(this.user.get(DownstreamStorage.class).getCachedLoginPacket());
        return PacketSignal.HANDLED;
    }

    // Handle this if the server send this... Not all servers sent this and Bedrock seems to don't care.
    @Override
    public PacketSignal handle(ServerToClientHandshakePacket packet) {
        try {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(packet.getJwt());
            JSONObject saltJwt = new JSONObject(JsonUtil.parseJson(jws.getUnverifiedPayload()));
            String x5u = jws.getHeader(HeaderParameterNames.X509_URL);
            ECPublicKey serverKey = EncryptionUtils.parseKey(x5u);
            SecretKey key = EncryptionUtils.getSecretKey(this.user.get(DownstreamStorage.class).getKeyPair().getPrivate(), serverKey,
                    Base64.getDecoder().decode(JsonUtils.childAsType(saltJwt, "salt", String.class)));
            session.enableEncryption(key);
        } catch (JoseException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        ClientToServerHandshakePacket clientToServerHandshake = new ClientToServerHandshakePacket();
        session.sendPacketImmediately(clientToServerHandshake);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(DisconnectPacket packet) {
        this.session.disconnect();
        this.user.sendUpstreamPacket(packet, false);
        return PacketSignal.HANDLED;
    }
}
