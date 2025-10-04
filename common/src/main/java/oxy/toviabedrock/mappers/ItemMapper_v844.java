package oxy.toviabedrock.mappers;

import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.CreativeItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.CreativeContentPacket;
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.BaseItemMapper;
import oxy.toviabedrock.base.mappers.storage.BaseItemRemappingStorage;

public class ItemMapper_v844 extends BaseItemMapper {
    public ItemMapper_v844(ProtocolToProtocol translator) {
        super(translator);
    }

    @Override
    protected void registerProtocol() {
        super.registerProtocol();

        this.registerClientbound(ItemComponentPacket.class, wrapped -> {
            final ItemComponentPacket packet = (ItemComponentPacket) wrapped.getPacket();
            for (int i = 0; i < packet.getItems().size(); i++) {
                final ItemDefinition definition = packet.getItems().get(i);
                final String identifier = this.identifierToIdentifier.get(definition.getIdentifier());
                if (identifier != null) {
                    packet.getItems().set(i, new SimpleItemDefinition(identifier, definition.getRuntimeId(), definition.getVersion(), definition.isComponentBased(), definition.getComponentData()));
                }

                wrapped.session().get(BaseItemRemappingStorage.class).put(definition.getIdentifier(), definition);
            }
        });

        this.registerClientbound(CreativeContentPacket.class, wrapped -> {
            final CreativeContentPacket packet = (CreativeContentPacket) wrapped.getPacket();
            for (int i = 0; i < packet.getContents().size(); i++) {
                final CreativeItemData creative = packet.getContents().get(i);
                final ItemData itemData = creative.getItem();
                final ItemData mapped = this.mapItemAndApplyHash(wrapped.session(), itemData);

                if (mapped != itemData) {
                    packet.getContents().set(i, new CreativeItemData(mapped, creative.getNetId(), creative.getGroupId()));
                }
            }
        });
    }
}
