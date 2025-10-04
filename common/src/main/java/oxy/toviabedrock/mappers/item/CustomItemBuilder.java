package oxy.toviabedrock.mappers.item;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

public class CustomItemBuilder {
    private String identifier;
    private int runtimeId;
    private String icon;
    private String displayName;
    private String entityPlacer;
    private String blockPlacer;
    private boolean allowOffHand;
    private boolean handEquipped;
    private int maxStackSize;
    private String creativeGroup;
    private int creativeCategory;

    public static CustomItemBuilder builder() {
        return new CustomItemBuilder();
    }

    public CustomItemBuilder identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public CustomItemBuilder runtimeId(int runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public CustomItemBuilder icon(String icon) {
        this.icon = icon;
        return this;
    }

    public CustomItemBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public CustomItemBuilder entityPlacer(String entityPlacer) {
        this.entityPlacer = entityPlacer;
        return this;
    }

    public CustomItemBuilder blockPlacer(String blockPlacer) {
        this.blockPlacer = blockPlacer;
        return this;
    }

    public CustomItemBuilder allowOffHand(boolean allowOffHand) {
        this.allowOffHand = allowOffHand;
        return this;
    }

    public CustomItemBuilder handEquipped(boolean handEquipped) {
        this.handEquipped = handEquipped;
        return this;
    }

    public CustomItemBuilder maxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        return this;
    }

    public CustomItemBuilder creativeGroup(String creativeGroup) {
        this.creativeGroup = creativeGroup;
        return this;
    }

    public CustomItemBuilder creativeCategory(int creativeCategory) {
        this.creativeCategory = creativeCategory;
        return this;
    }

    public NbtMap build() {
        NbtMapBuilder builder = NbtMap.builder();
        builder.putString("name", identifier).putInt("id", runtimeId);

        NbtMapBuilder itemProperties = NbtMap.builder();

        NbtMapBuilder componentBuilder = NbtMap.builder();

        if (this.icon != null) {
            NbtMap iconMap = NbtMap.builder()
                    .putCompound("textures", NbtMap.builder()
                            .putString("default", this.icon)
                            .build())
                    .build();
            itemProperties.putCompound("minecraft:icon", iconMap);
        }
        if (this.displayName != null) {
            componentBuilder.putCompound("minecraft:display_name", NbtMap.builder().putString("value", this.displayName).build());
        }

        if (this.entityPlacer != null) {
            componentBuilder.putCompound("minecraft:entity_placer", NbtMap.builder()
                    .putString("entity", this.entityPlacer)
                    .build());
        }

        if (this.blockPlacer != null) {
            componentBuilder.putCompound("minecraft:block_placer", NbtMap.builder().putString("block", this.blockPlacer).build());
        }

        itemProperties.putBoolean("allow_off_hand", this.allowOffHand);
        itemProperties.putBoolean("hand_equipped", this.handEquipped);
        itemProperties.putInt("max_stack_size", this.maxStackSize);

        if (this.creativeGroup != null) {
            itemProperties.putString("creative_group", "itemGroup.name.minecart");
        }

        itemProperties.putInt("creative_category", this.creativeCategory);

        componentBuilder.putCompound("item_properties", itemProperties.build());
        builder.putCompound("components", componentBuilder.build());
        return builder.build();
    }
}
