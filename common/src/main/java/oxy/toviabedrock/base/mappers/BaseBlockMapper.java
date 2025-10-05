package oxy.toviabedrock.base.mappers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import oxy.toviabedrock.base.Mapper;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.protocols.v844to827.Protocol844to827;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.impl.GameSessionStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaseBlockMapper extends Mapper {
    protected final Map<Integer, Integer> mappedBlockIds = new HashMap<>();
    protected final Map<Integer, Integer> mappedHashedBlockIds = new HashMap<>();

    public BaseBlockMapper(ProtocolToProtocol translator) {
        super(translator);

        this.initBlockMappings();
    }

    protected void initBlockMappings() {
    }

    protected final void loadHashedBlockMappingFromFile(String path) {
        try {
            final String jsonString = new String(Objects.requireNonNull(Protocol844to827.class.getResourceAsStream("/blocks/" + path)).readAllBytes());
            final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
            for (String key : object.keySet()) {
                this.mappedHashedBlockIds.put(Integer.valueOf(key), object.get(key).getAsInt());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final void loadBlockMappingFromFile(String path) {
        try {
            final String jsonString = new String(Objects.requireNonNull(Protocol844to827.class.getResourceAsStream("/blocks/" + path)).readAllBytes());
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
        // Bypass BDS block registry checksum, also no idea how this is calculated anyway.
        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();
            packet.setBlockRegistryChecksum(0);
        });
    }

    public int mapBlockIdOrHashedId(UserSession user, int id) {
        if (user.get(GameSessionStorage.class).isBlockNetworkIdsHashed()) {
            return this.mappedHashedBlockIds.getOrDefault(id, id);
        } else {
            return this.mappedBlockIds.getOrDefault(id, id);
        }
    }
}
