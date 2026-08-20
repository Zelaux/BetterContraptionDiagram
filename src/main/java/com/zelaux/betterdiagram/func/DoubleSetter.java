package com.zelaux.betterdiagram.func;

public interface DoubleSetter<T> {
    DoubleSetter<?> _none_ = (a, b) -> {};

    static <T> DoubleSetter<T> NONE() {
        return (DoubleSetter<T>) _none_;
    }

    void set(T t, double value);
}
