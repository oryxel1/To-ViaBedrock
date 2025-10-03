package oxy.toviabedrock.session.storage;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.data.auth.AuthType;
import org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockClientInitializer;
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestNetworkSettingsPacket;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;
import org.jose4j.json.internal.json_simple.JSONObject;
import oxy.toviabedrock.ToViaBedrockProxy;
import oxy.toviabedrock.handler.DownstreamPacketHandler;
import oxy.toviabedrock.session.ProxyUserSession;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.utils.ForgeryUtils;
import oxy.toviabedrock.utils.registry.UnknownBlockDefinitionRegistry;
import oxy.toviabedrock.utils.registry.UnknownItemDefinitionRegistry;

import java.security.KeyPair;
import java.util.Collections;
import java.util.function.Consumer;

public class DownstreamStorage extends UserStorage {
    @Getter

    private final BedrockCodec codec;
    @Getter
    private KeyPair keyPair;
    @Getter
    private LoginPacket cachedLoginPacket;

    public DownstreamStorage(UserSession session) {
        super(session);
        this.codec = ToViaBedrockProxy.getProxyRun().version();
    }

    public void startDownstreamOffline(JSONObject extraData, JSONObject skinData) {
        this.initDownstream(session -> {
            session.setCodec(this.codec);
            ((ProxyUserSession) this.session).setDownstreamSession(session);

            this.keyPair = EncryptionUtils.createKeyPair();
            String newAuthData = ForgeryUtils.forgeAuthData(this.keyPair, extraData);
            String newSkinData = ForgeryUtils.forgeSkinData(this.keyPair, skinData);

            LoginPacket login = new LoginPacket();
            login.setAuthPayload(new CertificateChainPayload(Collections.singletonList(newAuthData), AuthType.SELF_SIGNED));
            login.setClientJwt(newSkinData);
            login.setProtocolVersion(this.codec.getProtocolVersion());
            this.cachedLoginPacket = login;

            session.setPacketHandler(new DownstreamPacketHandler(session, (ProxyUserSession) this.session));

            RequestNetworkSettingsPacket packet = new RequestNetworkSettingsPacket();
            packet.setProtocolVersion(codec.getProtocolVersion());
            session.sendPacketImmediately(packet);

            session.getPeer().getCodecHelper().setBlockDefinitions(new UnknownBlockDefinitionRegistry());
            session.getPeer().getCodecHelper().setItemDefinitions(new UnknownItemDefinitionRegistry());

            ((ProxyUserSession) this.session).getUpstreamPeer().getCodecHelper().setBlockDefinitions(new UnknownBlockDefinitionRegistry());
            ((ProxyUserSession) this.session).getUpstreamPeer().getCodecHelper().setItemDefinitions(new UnknownItemDefinitionRegistry());
        });
    }

    private void initDownstream(Consumer<BedrockClientSession> consumer) {
        new Thread(() -> new Bootstrap()
                .channelFactory(RakChannelFactory.client(NioDatagramChannel.class))
                .option(RakChannelOption.RAK_PROTOCOL_VERSION, ToViaBedrockProxy.getProxyRun().version().getRaknetProtocolVersion())
                .option(RakChannelOption.RAK_COMPATIBILITY_MODE, true)
                .option(RakChannelOption.RAK_IP_DONT_FRAGMENT, true)
                .option(RakChannelOption.RAK_MTU_SIZES, new Integer[]{1492, 1200, 576})
                .option(RakChannelOption.RAK_CLIENT_INTERNAL_ADDRESSES, 20)
                .option(RakChannelOption.RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS, 500)
                .group(new NioEventLoopGroup())
                .handler(new BedrockClientInitializer() {
                    @Override
                    protected void initSession(BedrockClientSession session) {
                        consumer.accept(session);
                    }
                })
                .connect(ToViaBedrockProxy.getProxyRun().target())
                .awaitUninterruptibly()).start();
    }
}
