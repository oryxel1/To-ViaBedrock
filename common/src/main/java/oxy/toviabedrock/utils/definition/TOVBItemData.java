package oxy.toviabedrock.utils.definition;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import java.util.Arrays;
import java.util.Objects;

@ToString
@EqualsAndHashCode
public final class TOVBItemData implements ItemData {
    static final String[] EMPTY_ARRAY = new String[0];
    private ItemDefinition definition;
    private final int damage;
    private final int count;
    private final NbtMap tag;
    private final String[] canPlace;
    private final String[] canBreak;
    private final long blockingTicks;
    private final BlockDefinition blockDefinition;
    private boolean usingNetId;
    private int netId;

    public TOVBItemData(ItemDefinition definition, int damage, int count, NbtMap tag, String[] canPlace, String[] canBreak, long blockingTicks, BlockDefinition blockDefinition, boolean hasNetId, int netId) {
        this.definition = definition;
        this.damage = damage;
        this.count = count;
        this.tag = tag;
        this.canPlace = canPlace == null ? EMPTY_ARRAY : canPlace;
        this.canBreak = canBreak == null ? EMPTY_ARRAY : canBreak;
        this.blockingTicks = blockingTicks;
        this.blockDefinition = blockDefinition;
        this.netId = netId;
        this.usingNetId = hasNetId;
    }

    @Override
    public ItemDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public int getDamage() {
        return this.damage;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public NbtMap getTag() {
        return this.tag;
    }

    @Override
    public String[] getCanPlace() {
        return this.canPlace;
    }

    @Override
    public String[] getCanBreak() {
        return this.canBreak;
    }

    @Override
    public long getBlockingTicks() {
        return this.blockingTicks;
    }

    @Override
    public BlockDefinition getBlockDefinition() {
        return this.blockDefinition;
    }

    @Override
    public boolean isUsingNetId() {
        return this.usingNetId;
    }

    @Override
    public int getNetId() {
        return this.netId;
    }

    @Override
    public void setNetId(int netId) {
        this.netId = netId;
    }

    public boolean isValid() {
        return !isNull() && definition != null && definition != ItemDefinition.AIR;
    }

    public boolean isNull() {
        return count <= 0;
    }

    public boolean equals(ItemData other, boolean checkAmount, boolean checkMetadata, boolean checkUserdata) {
        return definition == other.getDefinition() &&
                (!checkAmount || count == other.getCount()) &&
                (!checkMetadata || (damage == other.getDamage() && blockingTicks == other.getBlockingTicks())) &&
                (!checkUserdata || (Objects.equals(tag, other.getTag()) && Arrays.equals(canPlace, other.getCanPlace()) && Arrays.equals(canBreak, other.getCanBreak())));
    }
}
