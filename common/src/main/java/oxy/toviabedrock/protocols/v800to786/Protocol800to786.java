package oxy.toviabedrock.protocols.v800to786;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.protocol.bedrock.codec.v786.Bedrock_v786;
import org.cloudburstmc.protocol.bedrock.codec.v800.Bedrock_v800;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.BlockMapper_v844;
import oxy.toviabedrock.mappers.ItemMapper_v844;
import oxy.toviabedrock.protocols.v844to827.Protocol844to827;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;

public class Protocol800to786 extends ProtocolToProtocol {
    public Protocol800to786() {
        super(Bedrock_v800.CODEC, Bedrock_v786.CODEC);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                try {
                    {
                        final String jsonString = new String(Objects.requireNonNull(Protocol844to827.class.getResourceAsStream("/blocks/v800to786/blockIds_v800to786.json")).readAllBytes());
                        final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
                        for (String key : object.keySet()) {
                            this.mappedBlockIds.put(Integer.valueOf(key), object.get(key).getAsInt());
                        }
                    }

                    {
                        final String jsonString = new String(Objects.requireNonNull(Protocol844to827.class.getResourceAsStream("/blocks/v800to786/blockIds_v800to786_hashed.json")).readAllBytes());
                        final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
                        for (String key : object.keySet()) {
                            this.mappedHashedBlockIds.put(Integer.valueOf(key), object.get(key).getAsInt());
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        this.mappers.add(new ItemMapper_v844(this) {
            @Override
            protected void initItemMappings() {
                try {
                    {
                        final String jsonString = new String(Objects.requireNonNull(Protocol844to827.class.getResourceAsStream("/items/itemIdentifiers_v800to786.json")).readAllBytes());
                        final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
                        for (String key : object.keySet()) {
                            this.itemIdentifierToMappedIdentifier.put(key, object.get(key).getAsString());
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);

        this.registerClientbound(StartGamePacket.class, wrapped -> {
            final StartGamePacket packet = (StartGamePacket) wrapped.getPacket();
            packet.setAuthoritativeMovementMode(AuthoritativeMovementMode.SERVER_WITH_REWIND);
        });

        this.registerClientbound(AddEntityPacket.class, wrapped -> cleanMetadata(((AddEntityPacket)wrapped.getPacket()).getMetadata()));
        this.registerClientbound(SetEntityDataPacket.class, wrapped -> cleanMetadata(((SetEntityDataPacket)wrapped.getPacket()).getMetadata()));
    }

    private void cleanMetadata(final EntityDataMap metadata) {
        if (metadata == null) {
            return;
        }

        metadata.remove(EntityDataTypes.SEAT_THIRD_PERSON_CAMERA_RADIUS);
        metadata.remove(EntityDataTypes.SEAT_CAMERA_RELAX_DISTANCE_SMOOTHING);

        EnumSet<EntityFlag> flags = metadata.getFlags();
        if (flags != null) {
            flags.remove(EntityFlag.DOES_SERVER_AUTH_ONLY_DISMOUNT);
        }
    }
}
