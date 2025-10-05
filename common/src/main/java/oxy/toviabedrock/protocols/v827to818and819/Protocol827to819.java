package oxy.toviabedrock.protocols.v827to818and819;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v818.Bedrock_v818;
import org.cloudburstmc.protocol.bedrock.codec.v819.Bedrock_v819;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import oxy.toviabedrock.base.ProtocolToProtocol;
import oxy.toviabedrock.base.mappers.BaseEntityMapper;
import oxy.toviabedrock.mappers.v844.BlockMapper_v844;
import oxy.toviabedrock.mappers.v844.ItemMapper_v844;

import java.util.Arrays;

public class Protocol827to819 extends ProtocolToProtocol {
    public Protocol827to819(BedrockCodec originalCodec, BedrockCodec translatedCodec) {
        super(originalCodec, translatedCodec);
    }

    @Override
    public void initMappers() {
        this.mappers.add(new BlockMapper_v844(this) {
            @Override
            protected void initBlockMappings() {
                if (getTranslatedCodec() != Bedrock_v819.CODEC) {
                    return;
                }

                loadBlockMappingFromFile("v827to819/blockIds_v827to819.json");
                loadHashedBlockMappingFromFile("v827to819/blockIds_v827to819_hashed.json");
            }
        });

        this.mappers.add(new ItemMapper_v844(this) {
            @Override
            protected void initItemMappings() {
                if (getTranslatedCodec() == Bedrock_v818.CODEC) {
                    this.itemIdentifierToMappedIdentifier.put("minecraft:music_disc_lava_chicken", "minecraft:music_disc_chirp");
                } else {
                    this.loadItemMappingsFromFile("itemIdentifiers_v827to819.json");
                }
            }
        });

        if (getTranslatedCodec() == Bedrock_v819.CODEC) {
            this.mappers.add(new BaseEntityMapper(this) {
                @Override
                protected void initEntityMappings() {
                    this.identifierToMapped.put("minecraft:copper_golem", new MappedEntity("minecraft:frog", true));
                }
            });
        }

        super.initMappers();
    }

    @Override
    protected void registerProtocol() {
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyServerbound);
        Arrays.stream(BedrockPacketType.values()).forEach(this::mapDirectlyClientbound);
    }
}
