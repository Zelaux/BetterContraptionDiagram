package com.zelaux.betterdiagram.func;

import org.jetbrains.annotations.Nullable;

public interface FloatConsumer {
    @Nullable FloatConsumer DUMMY = it->{};

    void consume(float value);
}
