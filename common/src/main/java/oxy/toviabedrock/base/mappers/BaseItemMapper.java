package oxy.toviabedrock.base.mappers;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData;
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import oxy.toviabedrock.base.Mapper;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.storage.BaseItemRemappingStorage;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.utils.HashMapWithHashed;
import oxy.toviabedrock.utils.definition.TOVBItemData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseItemMapper extends Mapper {
    // These are to map identifier that changed identifier, eg: minecraft:chain -> minecraft:iron_chain or vice versa.
    protected final Map<String, String> identifierToIdentifier = new HashMap<>();
    // These are to map new identifier to old one if it's not supported, eg: minecraft:copper_chain -> minecraft:iron_chain.
    protected final HashMapWithHashed<String, String> itemIdentifierToMappedIdentifier = new HashMapWithHashed<>();

    public BaseItemMapper(ProtocolToProtocol translator) {
        super(translator);

        this.initItemMappings();
    }

    protected void initItemMappings() {
    }

    @Override
    protected void registerProtocol() {
        this.registerServerbound(InventoryTransactionPacket.class, wrapped -> {
            final InventoryTransactionPacket packet = (InventoryTransactionPacket) wrapped.getPacket();
            packet.setItemInHand(this.reverseItemMapFromHash(wrapped.session(), packet.getItemInHand()));

            for (int i = 0; i < packet.getActions().size(); i++) {
                final InventoryActionData action = packet.getActions().get(i);

                packet.getActions().set(i, new InventoryActionData(
                        action.getSource(), action.getSlot(),
                        this.reverseItemMapFromHash(wrapped.session(), action.getFromItem()),
                        this.reverseItemMapFromHash(wrapped.session(), action.getToItem()),
                        action.getStackNetworkId()
                ));
            }
        });

        this.registerClientbound(AddItemEntityPacket.class, wrapped -> {
            AddItemEntityPacket packet = (AddItemEntityPacket) wrapped.getPacket();
            packet.setItemInHand(this.mapItemAndApplyHash(wrapped.session(), packet.getItemInHand()));
        });

        this.registerClientbound(InventoryContentPacket.class, wrapped -> {
            final InventoryContentPacket packet = (InventoryContentPacket) wrapped.getPacket();
            for (int i = 0; i < packet.getContents().size(); i++) {
                final ItemData itemData = packet.getContents().get(i);
                final ItemData mapped = this.mapItemAndApplyHash(wrapped.session(), itemData);

                if (mapped != itemData) {
                    packet.getContents().set(i, mapped);
                }
            }
        });

        this.registerClientbound(InventorySlotPacket.class, wrapped -> {
            final InventorySlotPacket packet = (InventorySlotPacket) wrapped.getPacket();
            packet.setItem(this.mapItemAndApplyHash(wrapped.session(), packet.getItem()));
        });
    }

    public ItemDefinition mapItemDefinitionWithOldIdentifier(UserSession session, ItemDefinition definition) {
        if (definition == null) {
            return null;
        }

        final String identifier = this.itemIdentifierToMappedIdentifier.get(definition.getIdentifier());
        if (identifier == null) {
            return null;
        }
        final ItemDefinition mapped = session.get(BaseItemRemappingStorage.class).getDefinition(identifier);
        if (mapped == null) {
            return null;
        }

        String oldIdentifier = this.identifierToIdentifier.get(definition.getIdentifier());
        if (oldIdentifier != null) {
            return new SimpleItemDefinition(oldIdentifier, mapped.getRuntimeId(), mapped.getVersion(), mapped.isComponentBased(), mapped.getComponentData());
        }

        return mapped;
    }

    protected ItemData mapItemAndApplyHash(UserSession session, ItemData data) {
        if (data == null) {
            return null;
        }

        final ItemDefinition mapped = mapItemDefinitionWithOldIdentifier(session, data.getDefinition());
        if (mapped == null) {
            return data;
        }

        final NbtMap originalTag = data.getTag() == null ? NbtMap.builder().build() : data.getTag();
        final NbtMapBuilder nbtBuilder = originalTag.toBuilder();

        NbtMapBuilder display = originalTag.containsKey("display") ? originalTag.getCompound("display").toBuilder() : NbtMap.builder();

        int hashed = data.getDefinition().getIdentifier().hashCode();
        if (display.build().containsKey("Lore")) {
            List<String> lore = new ArrayList<>(display.build().getList("lore", NbtType.STRING));
            lore.add("§r§7Item mapped from: " + data.getDefinition().getIdentifier() + " (" + hashed + ").");
            display.putList("Lore", NbtType.STRING, lore);
        } else {
            display.putList("Lore", NbtType.STRING, List.of("§r§7Item mapped from: " + data.getDefinition().getIdentifier() + " (" + hashed + ")."));
        }
        nbtBuilder.put("display", display.build());
        nbtBuilder.put("TOVBHash", hashed);

        return new TOVBItemData(mapped, data.getDamage(),
                data.getCount(), nbtBuilder.build(), data.getCanPlace(),
                data.getCanBreak(), data.getBlockingTicks(), data.getBlockDefinition(), data.isUsingNetId(), data.getNetId());
    }

    // I know this is a bad idea since we should properly track the item instead but oh well.
    // It works so who cares! Only BDS is *this* strict with item tag anyway!
    protected ItemData reverseItemMapFromHash(UserSession session, ItemData data) {
        if (data == null) {
            return null;
        }

        if (data.getTag() == null || data.getTag().isEmpty() || !data.getTag().containsKey("TOVBHash")) {
            return data;
        }

        final String identifier = this.itemIdentifierToMappedIdentifier.getHashed().get(data.getTag().getInt("TOVBHash"));
        if (identifier == null) {
            return data;
        }
        final ItemDefinition mapped = session.get(BaseItemRemappingStorage.class).getDefinition(identifier);
        if (mapped == null || mapped == data.getDefinition()) {
            return data;
        }

        NbtMapBuilder tagBuilder = data.getTag().toBuilder();
        if (data.getTag().containsKey("display")) {
            final NbtMap display = data.getTag().getCompound("display");
            final NbtMapBuilder displayNbt = display.toBuilder();

            if (display.containsKey("Lore")) {
                final List<String> lore = new ArrayList<>(display.getList("Lore", NbtType.STRING));
                if (lore.size() == 1) {
                    displayNbt.remove("Lore");
                } else {
                    final String lore1 = "§r§7Item mapped from: " + mapped.getIdentifier() + " (" + data.getTag().getInt("TOVBHash") + ").";
                    lore.remove(lore1);
                    displayNbt.putList("Lore", NbtType.STRING, lore);
                }
            }

            if (!displayNbt.isEmpty()) {
                tagBuilder.put("display", displayNbt.build());
            } else {
                tagBuilder.remove("display");
            }
        }
        tagBuilder.remove("TOVBHash");

        return new TOVBItemData(mapped, data.getDamage(),
                data.getCount(), tagBuilder.build(), data.getCanPlace(),
                data.getCanBreak(), data.getBlockingTicks(), data.getBlockDefinition(), data.isUsingNetId(), data.getNetId());
    }
}
