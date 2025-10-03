import org.cloudburstmc.protocol.bedrock.codec.v844.Bedrock_v844;
import oxy.toviabedrock.ToViaBedrockProxy;

import java.net.InetSocketAddress;

public class LocalTest {
    public static void main(String[] args) {
        final ToViaBedrockProxy.ProxyRun run = ToViaBedrockProxy.ProxyRun.builder()
                .proxy(new InetSocketAddress("127.0.0.1", 19133))
                .target(new InetSocketAddress("127.0.0.1", 19132))
                .version(Bedrock_v844.CODEC).build();
        ToViaBedrockProxy.start(run, "Test Translation Layer.");
    }
}
