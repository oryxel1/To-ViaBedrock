package oxy.toviabedrock.api.dimension;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BedrockDimension {
    OVERWORLD(-64, 384, 0), THE_NETHER(0, 128, 1), THE_END(0, 256, 2);

    private final int minY;
    private final int height;
    private final int bedrockId;

    public static BedrockDimension dimensionFromId(int id) {
        return id == 0 ? BedrockDimension.OVERWORLD : id == 1 ? BedrockDimension.THE_NETHER : BedrockDimension.THE_END;
    }
}
