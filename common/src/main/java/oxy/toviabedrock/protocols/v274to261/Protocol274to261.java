package oxy.toviabedrock.protocols.v274to261;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.codec.v261.Bedrock_v261;
import oxy.toviabedrock.codec.v274.Bedrock_v274;
import oxy.toviabedrock.protocols.v274to261.storage.EntityTracker_v261;
import oxy.toviabedrock.session.UserSession;

import java.util.Arrays;

public class Protocol274to261 extends ProtocolToProtocol {
    public Protocol274to261() {
        super(Bedrock_v274.CODEC, Bedrock_v261.CODEC);
    }

    @Override
    public void init(UserSession session) {
        session.put(new EntityTracker_v261(session));
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);
        this.ignoreClientbound(UpdateBlockSyncedPacket.class);

        this.registerClientbound(RemoveEntityPacket.class, wrapped -> {
            final RemoveEntityPacket packet = (RemoveEntityPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).remove(packet.getUniqueEntityId());
        });
        this.registerClientbound(AddPaintingPacket.class, wrapped -> {
            final AddPaintingPacket packet = (AddPaintingPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getUniqueEntityId(), packet.getPosition(), Vector3f.ZERO);
        });
        this.registerClientbound(AddItemEntityPacket.class, wrapped -> {
            final AddItemEntityPacket packet = (AddItemEntityPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getUniqueEntityId(), packet.getPosition(), Vector3f.ZERO);
        });
        this.registerClientbound(AddPlayerPacket.class, wrapped -> {
            final AddPlayerPacket packet = (AddPlayerPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getUniqueEntityId(), packet.getPosition(), packet.getRotation());
        });
        this.registerClientbound(AddEntityPacket.class, wrapped -> {
            final AddEntityPacket packet = (AddEntityPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).cache(packet.getRuntimeEntityId(), packet.getUniqueEntityId(), packet.getPosition(), Vector3f.from(packet.getRotation().getX(), packet.getRotation().getY(), packet.getRotation().getY()));
        });
        this.registerClientbound(MovePlayerPacket.class, wrapped -> {
            final MovePlayerPacket packet = (MovePlayerPacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).moveAbsolute(packet.getRuntimeEntityId(), packet.getPosition(), packet.getRotation());
        });
        this.registerClientbound(MoveEntityAbsolutePacket.class, wrapped -> {
            final MoveEntityAbsolutePacket packet = (MoveEntityAbsolutePacket) wrapped.getPacket();
            wrapped.session().get(EntityTracker_v261.class).moveAbsolute(packet.getRuntimeEntityId(), packet.getPosition(), packet.getRotation());
        });

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
    }
}
