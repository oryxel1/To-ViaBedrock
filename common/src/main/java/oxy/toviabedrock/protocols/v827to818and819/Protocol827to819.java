package oxy.toviabedrock.protocols.v827to818and819;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v818.Bedrock_v818;
import org.cloudburstmc.protocol.bedrock.codec.v819.Bedrock_v819;
import org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import oxy.toviabedrock.mappers.BlockAndItemMapper_v844;
import oxy.toviabedrock.protocols.v844to827.Protocol844to827;

import java.util.Arrays;
import java.util.Objects;

public class Protocol827to819 extends BlockAndItemMapper_v844 {
    public Protocol827to819(BedrockCodec translatedCodec) {
        super(Bedrock_v827.CODEC, translatedCodec);
    }

    @Override
    protected void mapBlock() {
        if (this.getTranslatedCodec() == Bedrock_v819.CODEC) {
            try {
                {
                    final String jsonString = new String(Objects.requireNonNull(Protocol827to819.class.getResourceAsStream("/blocks/v827to819/blockIds_v827to819.json")).readAllBytes());
                    final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
                    for (String key : object.keySet()) {
                        this.mappedBlockIds.put(Integer.valueOf(key), object.get(key).getAsInt());
                    }
                }

                {
                    final String jsonString = new String(Objects.requireNonNull(Protocol827to819.class.getResourceAsStream("/blocks/v827to819/blockIds_v827to819_hashed.json")).readAllBytes());
                    final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
                    for (String key : object.keySet()) {
                        this.mappedHashedBlockIds.put(Integer.valueOf(key), object.get(key).getAsInt());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void mapItem() {
        if (this.getTranslatedCodec() == Bedrock_v818.CODEC) {
            this.itemIdentifierToMapped.put("minecraft:music_disc_lava_chicken", "minecraft:music_disc_chirp");
        } else {
            try {
                {
                    final String jsonString = new String(Objects.requireNonNull(Protocol827to819.class.getResourceAsStream("/items/itemIdentifiers_v827to819.json")).readAllBytes());
                    final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
                    for (String key : object.keySet()) {
                        this.itemIdentifierToMapped.put(key, object.get(key).getAsString());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);
        super.registerProtocol();
    }
}
