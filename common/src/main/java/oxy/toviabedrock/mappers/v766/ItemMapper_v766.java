package oxy.toviabedrock.mappers.v766;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemVersion;
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.BaseItemMapper;
import oxy.toviabedrock.mappers.v844.ItemMapper_v844;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemMapper_v766 extends ItemMapper_v844 {
    protected List<String> vanillaItemIdentifiers;

    public ItemMapper_v766(ProtocolToProtocol translator) {
        super(translator);
    }

    protected void loadVanillaIdentifiersFromFile(String name) {
        if (this.vanillaItemIdentifiers == null) {
            this.vanillaItemIdentifiers = new ArrayList<>();
        }
        try {
            final String jsonString = new String(Objects.requireNonNull(BaseItemMapper.class.getResourceAsStream("/items/vanilla/" + name)).readAllBytes());
            final JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
            final JsonArray items = object.getAsJsonArray("items");
            for (JsonElement identifier : items) {
                this.vanillaItemIdentifiers.add(identifier.getAsString());
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
            packet.getItems().removeIf(definition -> this.vanillaItemIdentifiers.contains(definition.getIdentifier()));
            for (int i = 0; i < packet.getItems().size(); i++) {
                final ItemDefinition definition = packet.getItems().get(i);

                packet.getItems().set(i,
                        new SimpleItemDefinition(
                                definition.getIdentifier(),
                                definition.getRuntimeId(),
                                ItemVersion.DATA_DRIVEN,
                                true,
                                NbtMap.EMPTY
                        )
                );
            }
        });
    }
}
