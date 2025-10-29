package oxy.toviabedrock.base.mappers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData;
import org.cloudburstmc.protocol.bedrock.packet.*;
import oxy.toviabedrock.base.Mapper;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.storage.BaseItemRemappingStorage;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.utils.HashMapWithHashed;
import oxy.toviabedrock.utils.definition.TOVBItemData;

import java.util.*;

public class BaseItemMapper extends Mapper {
    // These are to map identifier that changed identifier, eg: minecraft:chain -> minecraft:iron_chain or vice versa.
    protected final Map<String, String> identifierToIdentifier = new HashMap<>();
    // These are to map new identifier to old one if it's not supported, eg: minecraft:copper_chain -> minecraft:iron_chain.
    protected final HashMapWithHashed<String, String> itemIdentifierToMappedIdentifier = new HashMapWithHashed<>();
    // There are cases where the item data needed to be process directly instead of just the item definition.
    protected final List<ItemMapper> itemDataMapper = new ArrayList<>();
    public interface ItemMapper {
        TOVBItemData process(TOVBItemData item);
    }

    public BaseItemMapper(ProtocolToProtocol translator) {
        super(translator);

        this.initItemMappings();
    }

    @Override
    protected void init(UserSession session) {
        if (session.get(BaseItemRemappingStorage.class) == null) {
            session.put(new BaseItemRemappingStorage(session));
        }
    }

    protected void initItemMappings() {
    }

    protected final void loadItemMappingsFromFile(String name) {
        try {
            final String jsonString = new String(Objects.requireNonNull(BaseItemMapper.class.getResourceAsStream("/items/" + name)).readAllBytes());
            final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
            for (String key : object.keySet()) {
                this.itemIdentifierToMappedIdentifier.put(key, object.get(key).getAsString());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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

        final NbtMapBuilder mapBuilder = NbtMap.builder();
        mapBuilder.put("TOVBHash", definition.getIdentifier().hashCode());
        final String oldIdentifier = this.identifierToIdentifier.get(definition.getIdentifier());
        return new SimpleItemDefinition(oldIdentifier != null ? oldIdentifier : mapped.getIdentifier(), mapRuntimeId(mapped), mapped.getVersion(), mapped.isComponentBased(), mapBuilder.build());
    }

    protected ItemData mapItemAndApplyHash(UserSession session, ItemData data) {
        if (data == null) {
            return null;
        }

        final ItemDefinition mapped = mapItemDefinitionWithOldIdentifier(session, data.getDefinition());
        if (mapped == null) {
            int newId = mapRuntimeId(data.getDefinition());
            if (newId != data.getDefinition().getRuntimeId()) {
                final ItemDefinition old = data.getDefinition();
                final SimpleItemDefinition definition = new SimpleItemDefinition(old.getIdentifier(), newId, old.getVersion(), old.isComponentBased(), old.getComponentData());
                return new TOVBItemData(definition, data.getDamage(), data.getCount(), data.getTag(), data.getCanPlace(), data.getCanBreak(), data.getBlockingTicks(), data.getBlockDefinition(), data.isUsingNetId(), data.getNetId());
            }

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

        TOVBItemData itemData = new TOVBItemData(mapped, data.getDamage(), data.getCount(), nbtBuilder.build(), data.getCanPlace(), data.getCanBreak(), data.getBlockingTicks(), data.getBlockDefinition(), data.isUsingNetId(), data.getNetId());
        for (ItemMapper mapper : this.itemDataMapper) {
            itemData = mapper.process(itemData);
        }

        return itemData;
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
                    // Hardcoded code blah, blah, blah, yeah I don't give a fuck :D.
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

    protected int mapRuntimeId(ItemDefinition definition) {
        return definition.getRuntimeId();
    }
}
