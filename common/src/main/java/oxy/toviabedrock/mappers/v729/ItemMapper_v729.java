package oxy.toviabedrock.mappers.v729;

import com.google.gson.*;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemVersion;
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.BaseItemMapper;
import oxy.toviabedrock.mappers.v766.ItemMapper_v766;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class ItemMapper_v729 extends ItemMapper_v766 {
    protected Map<String, Integer> identifierToRuntimeId;
    private int maxItemId;

    public ItemMapper_v729(ProtocolToProtocol translator) {
        super(translator);
    }

    protected void loadItemRuntimeStatesFromFile(String name) {
        if (this.identifierToRuntimeId == null) {
            this.identifierToRuntimeId = new HashMap<>();
        }
        if (this.vanillaItemIdentifiers == null) {
            this.vanillaItemIdentifiers = new HashSet<>();
        }
        try {
            final String jsonString = new String(Objects.requireNonNull(BaseItemMapper.class.getResourceAsStream("/items/runtimes/" + name)).readAllBytes());
            final JsonArray array = JsonParser.parseString(jsonString).getAsJsonArray();

            for (JsonElement element : array) {
                final JsonObject item = element.getAsJsonObject();
                final String identifier = item.get("name").getAsString();
                final int id = item.get("id").getAsInt();
                this.vanillaItemIdentifiers.add(identifier);
                this.identifierToRuntimeId.put(identifier, id);
                this.maxItemId = id;
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void registerProtocol() {
        super.registerProtocol();
        this.registerClientbound(ItemComponentPacket.class, wrapped -> {
            final ItemComponentPacket packet = (ItemComponentPacket) wrapped.getPacket();
            // This is NOT a typo, older versions don't need you to send the vanilla item definition.
            packet.getItems().removeIf(definition -> this.vanillaItemIdentifiers.contains(definition.getIdentifier()));

            int id = this.maxItemId;
            for (int i = 0; i < packet.getItems().size(); i++) {
                final ItemDefinition definition = packet.getItems().get(i);

                id++; // Prevent duplicate ids.
                packet.getItems().set(i,
                        new SimpleItemDefinition(
                                definition.getIdentifier(),
                                id,
                                ItemVersion.DATA_DRIVEN,
                                true,
                                NbtMap.EMPTY
                        )
                );
            }
        });
    }

    @Override
    protected void loadVanillaIdentifiersFromFile(String name) {
        throw new RuntimeException("From protocol v729 and below, please use loadItemRuntimeStatesFromFile instead!");
    }

    @Override
    protected int mapRuntimeId(ItemDefinition definition) {
        return this.identifierToRuntimeId.getOrDefault(definition.getIdentifier(), definition.getRuntimeId());
    }
}
