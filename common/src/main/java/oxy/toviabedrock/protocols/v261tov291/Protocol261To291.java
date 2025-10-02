package oxy.toviabedrock.protocols.v261tov291;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.v291.Bedrock_v291;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.codec.v261.Bedrock_v261;
import oxy.toviabedrock.protocols.v261tov291.storage.EntityTracker_v261;
import oxy.toviabedrock.session.UserSession;

import java.util.Arrays;
import java.util.EnumSet;

public class Protocol261To291 extends ProtocolToProtocol {
    public Protocol261To291() {
        super(Bedrock_v291.CODEC, Bedrock_v261.CODEC);
    }

    @Override
    public void init(UserSession session) {
        session.put(new EntityTracker_v261(session));
    }

    @Override
    public void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);
        this.ignoreClientbound(PhotoTransferPacket.class);
        this.ignoreClientbound(UpdateSoftEnumPacket.class);
        this.ignoreClientbound(ScriptCustomEventPacket.class);

        this.registerClientbound(NetworkStackLatencyPacket.class, wrapped -> {
            final NetworkStackLatencyPacket packet = (NetworkStackLatencyPacket) wrapped.getPacket();

            // TODO: Figure out the magnitude..
            wrapped.session().sendDownstreamPacket(packet, false);
        });

        // Cleanup the new metadata.
        this.registerClientbound(SetEntityDataPacket.class, wrapped -> this.cleanMetadata(((SetEntityDataPacket)wrapped.getPacket()).getMetadata()));

        // Cleanup the new metadata and also cache the entity.
        this.registerClientbound(AddPaintingPacket.class, wrapped -> {
            final AddPaintingPacket packet = (AddPaintingPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getPosition(), Vector3f.ZERO);
        });
        this.registerClientbound(AddItemEntityPacket.class, wrapped -> {
            final AddItemEntityPacket packet = (AddItemEntityPacket) wrapped.getPacket();

            cleanMetadata(packet.getMetadata());
            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getPosition(), Vector3f.ZERO);
        });
        this.registerClientbound(AddPlayerPacket.class, wrapped -> {
            final AddPlayerPacket packet = (AddPlayerPacket) wrapped.getPacket();
            cleanMetadata(packet.getMetadata());

            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getPosition(), packet.getRotation());
        });
        this.registerClientbound(AddEntityPacket.class, wrapped -> {
            final AddEntityPacket packet = (AddEntityPacket) wrapped.getPacket();
            cleanMetadata(packet.getMetadata());

            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getPosition(), Vector3f.from(packet.getRotation().getX(), packet.getRotation().getY(), packet.getRotation().getY()));
        });

        // Now cache entities position.
        this.registerClientbound(MovePlayerPacket.class, wrapped -> {
            final MovePlayerPacket packet = (MovePlayerPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).moveAbsolute(packet.getRuntimeEntityId(), packet.getPosition(), packet.getRotation());
        });

        this.registerClientbound(MoveEntityAbsolutePacket.class, wrapped -> {
            final MoveEntityAbsolutePacket packet = (MoveEntityAbsolutePacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).moveAbsolute(packet.getRuntimeEntityId(), packet.getPosition(), packet.getRotation());
        });

        // And now we can translate this.
        this.registerClientbound(MoveEntityDeltaPacket.class, wrapped -> {
            final EntityTracker_v261 entityTracker = wrapped.session().get(EntityTracker_v261.class);
            final MoveEntityDeltaPacket packet = (MoveEntityDeltaPacket) wrapped.getPacket();
            entityTracker.moveRelative(packet.getRuntimeEntityId(), packet);

            final EntityTracker_v261.EntityCache entity = entityTracker.getEntity(packet.getRuntimeEntityId());
            if (entity == null) {
                return;
            }

            // Yucks no move relative pre 1.7, sound bad.
            final MoveEntityAbsolutePacket absolutePacket = new MoveEntityAbsolutePacket();
            absolutePacket.setRuntimeEntityId(packet.getRuntimeEntityId());
            absolutePacket.setPosition(entity.getPosition());
            absolutePacket.setRotation(entity.getRotation());
            absolutePacket.setOnGround(packet.getFlags().contains(MoveEntityDeltaPacket.Flag.ON_GROUND));
            absolutePacket.setTeleported(packet.getFlags().contains(MoveEntityDeltaPacket.Flag.TELEPORTING));
            absolutePacket.setForceMove(packet.getFlags().contains(MoveEntityDeltaPacket.Flag.FORCE_MOVE_LOCAL_ENTITY));

            wrapped.setPacket(absolutePacket);
        });

        // TODO: Translate this...
        this.ignoreClientbound(SetScorePacket.class);
    }

    private void cleanMetadata(EntityDataMap metadata) {
        if (metadata == null) {
            return;
        }

        if (metadata.getFlags() != null) {
            final EnumSet<EntityFlag> flags = metadata.getFlags();
            flags.remove(EntityFlag.ORPHANED);
            flags.remove(EntityFlag.IS_PREGNANT);
            flags.remove(EntityFlag.LAYING_EGG);
            flags.remove(EntityFlag.RIDER_CAN_PICK);
        }

        metadata.remove(EntityDataTypes.BOAT_BUBBLE_TIME);
        metadata.remove(EntityDataTypes.AGENT_EID);
    }
}
