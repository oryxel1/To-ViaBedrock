package oxy.toviabedrock.mappers;

import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.AvailableEntityIdentifiersPacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;
import oxy.toviabedrock.base.Mapper;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.mappers.storage.EntityRemappingStorage_v844;
import oxy.toviabedrock.session.UserSession;

import java.util.HashMap;
import java.util.Map;

public class EntityMapper_v844 extends Mapper {
    protected final Map<String, MappedEntity> identifierToMapped = new HashMap<>();

    public EntityMapper_v844(ProtocolToProtocol translator) {
        super(translator);

        this.mapEntity();
    }

    protected void mapEntity() {
    }

    @Override
    public void init(UserSession session) {
        session.put(new EntityRemappingStorage_v844(session));
    }

    @Override
    protected void registerProtocol() {
        this.registerClientbound(AvailableEntityIdentifiersPacket.class, wrapped -> {
            final AvailableEntityIdentifiersPacket packet = (AvailableEntityIdentifiersPacket) wrapped.getPacket();
        });

        this.registerClientbound(RemoveEntityPacket.class, wrapped -> {
            final RemoveEntityPacket packet = (RemoveEntityPacket) wrapped.getPacket();
            wrapped.session().get(EntityRemappingStorage_v844.class).remove(packet.getUniqueEntityId());
        });

        this.registerClientbound(AddEntityPacket.class, wrapped -> {
            final AddEntityPacket packet = (AddEntityPacket) wrapped.getPacket();
            final MappedEntity mappedEntity = this.identifierToMapped.get(packet.getIdentifier());
            final EntityRemappingStorage_v844 storage = wrapped.session().get(EntityRemappingStorage_v844.class);

            storage.remove(packet.getUniqueEntityId());
            if (mappedEntity != null) {
                storage.add(packet.getRuntimeEntityId(), packet.getUniqueEntityId(), packet.getIdentifier(), mappedEntity.showRealName);

                if (mappedEntity.showRealName) {
                    packet.getMetadata().put(EntityDataTypes.NAMETAG_ALWAYS_SHOW, (byte) 1);
                    final CharSequence name = packet.getMetadata().get(EntityDataTypes.NAME);
                    if (name == null || name.isEmpty()) {
                        packet.getMetadata().put(EntityDataTypes.NAME, packet.getIdentifier());
                    }
                }
                packet.setIdentifier(mappedEntity.identifier);
            } else {
                storage.remove(packet.getUniqueEntityId());
            }
        });

        this.registerClientbound(SetEntityDataPacket.class, wrapped -> {
            final SetEntityDataPacket packet = (SetEntityDataPacket) wrapped.getPacket();
            final MappedEntity reverseMapped = wrapped.session().get(EntityRemappingStorage_v844.class).getIdentifier(packet.getRuntimeEntityId());
            if (reverseMapped == null) {
                return;
            }

            if (reverseMapped.showRealName) {
                packet.getMetadata().put(EntityDataTypes.NAMETAG_ALWAYS_SHOW, (byte) 1);
                final CharSequence name = packet.getMetadata().get(EntityDataTypes.NAME);

                if (name == null || name.isEmpty()) {
                    packet.getMetadata().put(EntityDataTypes.NAME, reverseMapped.identifier);
                }
            }
        });
    }

    public record MappedEntity(String identifier, boolean showRealName) {
    }
}
