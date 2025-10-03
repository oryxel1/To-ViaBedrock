package oxy.toviabedrock.protocols.v844to827;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827;
import org.cloudburstmc.protocol.bedrock.codec.v844.Bedrock_v844;
import org.cloudburstmc.protocol.bedrock.data.ExperimentData;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import oxy.toviabedrock.mappers.BlockAndItemMapper_v844;

import java.util.Arrays;
import java.util.Objects;

public class Protocol844to827 extends BlockAndItemMapper_v844 {
    public Protocol844to827() {
        super(Bedrock_v844.CODEC, Bedrock_v827.CODEC);
    }

    @Override
    protected void mapBlock() {
        try {
            final String jsonString = new String(Objects.requireNonNull(Protocol844to827.class.getResourceAsStream("/blocks/blockIds_v844to827.json")).readAllBytes());
            final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
            for (String key : object.keySet()) {
                this.mappedBlockIds.put(Integer.valueOf(key), object.get(key).getAsInt());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();
            packet.setServerEngine(Bedrock_v827.CODEC.getMinecraftVersion());

            // We want to add support for some of the new blocks :).
            packet.getExperiments().add(new ExperimentData("y_2025_drop_3", true));
        });

        super.registerProtocol();
    }
}
