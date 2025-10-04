package oxy.toviabedrock.base.mappers;

import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import oxy.toviabedrock.base.Mapper;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.impl.GameSessionStorage;

import java.util.HashMap;
import java.util.Map;

public class BaseBlockMapper extends Mapper {
    protected final Map<Integer, Integer> mappedBlockIds = new HashMap<>();
    protected final Map<Integer, Integer> mappedHashedBlockIds = new HashMap<>();

    public BaseBlockMapper(ProtocolToProtocol translator) {
        super(translator);

        this.initBlockMappings();
    }

    protected void initBlockMappings() {
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
