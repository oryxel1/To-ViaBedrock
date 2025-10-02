import org.cloudburstmc.protocol.bedrock.codec.v291.Bedrock_v291;
import oxy.toviabedrock.ToViaBedrockProxy;

import java.net.InetSocketAddress;

public class LocalTest {
    public static void main(String[] args) {
        ToViaBedrockProxy.launchProxy(new InetSocketAddress("127.0.0.1", 19132), new InetSocketAddress("127.0.0.1", 19133), Bedrock_v291.CODEC);
    }
}
