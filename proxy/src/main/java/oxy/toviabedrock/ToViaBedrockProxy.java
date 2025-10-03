package oxy.toviabedrock;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Builder;
import lombok.Getter;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockServerInitializer;
import oxy.toviabedrock.handler.UpstreamPacketHandler;

import java.net.InetSocketAddress;

public class ToViaBedrockProxy {
    @Getter
    private static ProxyRun proxyRun;
    private static boolean init;

    public static void start(ProxyRun proxy, String motd) {
        if (!init) {
            ToViaBedrock.init();
            init = true;
        }

        proxyRun = proxy;

        final BedrockPong pong = new BedrockPong()
                .edition("MCPE")
                .motd(motd)
                .gameType("Survival")
                .protocolVersion(proxy.version.getProtocolVersion());

        new ServerBootstrap()
                .channelFactory(RakChannelFactory.server(NioDatagramChannel.class))
                .option(RakChannelOption.RAK_ADVERTISEMENT, pong.toByteBuf())
                .group(new NioEventLoopGroup())
                .childHandler(new BedrockServerInitializer() {
                    @Override
                    protected void initSession(BedrockServerSession session) {
                        session.setPacketHandler(new UpstreamPacketHandler(session));
                    }
                })
                .bind(proxy.proxy)
                .awaitUninterruptibly();
    }

    public record ProxyRun(InetSocketAddress target, InetSocketAddress proxy, BedrockCodec version) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private InetSocketAddress target;
            private InetSocketAddress proxy;
            private BedrockCodec version;

            public Builder target(InetSocketAddress target) {
                this.target = target;
                return this;
            }

            public Builder proxy(InetSocketAddress proxy) {
                this.proxy = proxy;
                return this;
            }

            public Builder version(BedrockCodec version) {
                final BedrockCodecHelper helper = version.createHelper();
                helper.setEncodingSettings(EncodingSettings.builder()
                        .maxListSize(Integer.MAX_VALUE)
                        .maxByteArraySize(Integer.MAX_VALUE)
                        .maxNetworkNBTSize(Integer.MAX_VALUE)
                        .maxItemNBTSize(Integer.MAX_VALUE)
                        .maxStringLength(Integer.MAX_VALUE)
                        .build());
                this.version = version.toBuilder().helper(() -> helper).build();
                return this;
            }

            public ProxyRun build() {
                return new ProxyRun(target, proxy, version);
            }
        }
    }
}
