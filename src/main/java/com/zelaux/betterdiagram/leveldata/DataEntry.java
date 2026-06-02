package com.zelaux.betterdiagram.leveldata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.data.BCDData;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.spongepowered.asm.mixin.*;

@FieldDefaults(level = AccessLevel.PUBLIC)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DataEntry {
    @NonNull
    BCDData data;
    @Setter
    private long creationTick;

    public DataEntry set(@NonNull BCDData data, long updateTick) {
        this.data = data;
        this.creationTick = updateTick;
        return this;
    }

    public static final Codec<DataEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            BCDData.NON_NULL_CODEC.fieldOf("data").forGetter(DataEntry::data),
            Codec.LONG.fieldOf("creationTick").forGetter(DataEntry::creationTick)
        ).apply(i, DataEntry::new)
    );
}
