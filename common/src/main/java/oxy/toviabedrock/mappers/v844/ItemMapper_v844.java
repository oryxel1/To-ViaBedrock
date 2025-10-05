package oxy.toviabedrock.mappers.v844;

import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.CreativeItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.crafting.CraftingDataType;
import org.cloudburstmc.protocol.bedrock.data.inventory.crafting.recipe.RecipeData;
import org.cloudburstmc.protocol.bedrock.data.inventory.crafting.recipe.ShapedRecipeData;
import org.cloudburstmc.protocol.bedrock.data.inventory.crafting.recipe.ShapelessRecipeData;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.DefaultDescriptor;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.ItemDescriptorWithCount;
import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;
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

        this.registerClientbound(CraftingDataPacket.class, wrapped -> {
            final CraftingDataPacket packet = (CraftingDataPacket) wrapped.getPacket();
            for (final RecipeData data : packet.getCraftingData()) {
                if (data.getType() != CraftingDataType.SHAPED && data.getType() != CraftingDataType.SHAPELESS) {
                    continue;
                }

                if (data instanceof ShapedRecipeData shaped) {
                    shaped.getResults().replaceAll(itemData -> this.mapItemAndApplyHash(wrapped.session(), itemData));
                    for (int i = 0; i < shaped.getIngredients().size(); i++) {
                        final ItemDescriptorWithCount ingredient = shaped.getIngredients().get(i);
                        if (!(ingredient.getDescriptor() instanceof DefaultDescriptor descriptor)) {
                            continue;
                        }
                        if (descriptor.getItemId() == ItemData.AIR.getDefinition()) {
                            continue;
                        }

                        final ItemDefinition mapped = this.mapItemDefinitionWithOldIdentifier(wrapped.session(), descriptor.getItemId());
                        if (mapped != null) {
                            shaped.getIngredients().set(i, new ItemDescriptorWithCount(new DefaultDescriptor(mapped, descriptor.getAuxValue()), ingredient.getCount()));
                        }
                    }

                    shaped.getResults().replaceAll(itemData -> this.mapItemAndApplyHash(wrapped.session(), itemData));
                } else if (data instanceof ShapelessRecipeData shapeless) {
                    shapeless.getResults().replaceAll(itemData -> this.mapItemAndApplyHash(wrapped.session(), itemData));

                    for (int i = 0; i < shapeless.getIngredients().size(); i++) {
                        final ItemDescriptorWithCount ingredient = shapeless.getIngredients().get(i);
                        if (!(ingredient.getDescriptor() instanceof DefaultDescriptor descriptor)) {
                            continue;
                        }
                        if (descriptor.getItemId() == ItemData.AIR.getDefinition()) {
                            continue;
                        }

                        final ItemDefinition mapped = this.mapItemDefinitionWithOldIdentifier(wrapped.session(), descriptor.getItemId());
                        if (mapped != null) {
                            shapeless.getIngredients().set(i, new ItemDescriptorWithCount(new DefaultDescriptor(mapped, descriptor.getAuxValue()), ingredient.getCount()));
                        }
                    }
                }
            }
        });
    }
}
