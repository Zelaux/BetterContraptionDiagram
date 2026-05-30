package com.zelaux.betterdiagram.util;

import com.mojang.datafixers.util.Either;
import com.zelaux.betterdiagram.BetterContraptionDiagram;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class AsyncComputeHolder<T, Part> {

    @NotNull
    private volatile StateValue<T, Part> state = StateValue.unset();

    public AsyncComputeHolder() {
    }

    /**
     * Invalidates the cache, causing the supplier to be called again on the next access.
     */
    public synchronized void invalidate() {
            StateValue<T, Part> state = this.state;
            if(state.calc ) {
                state.pair.future.cancel(true);
            }
            this.state = StateValue.unset();
    }

    public Either<T, Pair<T, Part>> getAsync(Function<ConcurrentLinkedQueue<Part>, T> calculator, Executor executor) {
        var ret = state;
        if(ret.calc) return Either.right(ret.pair);
        if(ret.set) return Either.left(ret.value);
        synchronized(this) {
            ret = state;
            if(ret.calc) return Either.right(ret.pair);
            if(ret.set) return Either.left(ret.value);
            var linkedQueue = new ConcurrentLinkedQueue<Part>();

            var future = new CompletableFuture<T>();
            var pair = new Pair<>(future, linkedQueue);

            // Сначала обновляем состояние, чтобы фоновый поток мог его увидеть
            this.state =StateValue.calc(pair);

            executor.execute(() -> {
                try {
                    T result = calculator.apply(linkedQueue);
                    synchronized (this) {
                        // Сравниваем по ссылке на pair, это надежнее и быстрее
                        if (this.state.pair == pair) {
                            this.state = StateValue.set(result);
                        }
                    }
                    future.complete(result);
                } catch (Exception e) {
                    BetterContraptionDiagram.LOGGER.error("AsyncComputeHolder error: ", e);
                    synchronized (this) {
                        if (this.state.pair == pair) this.state = StateValue.unset();
                    }
                    future.completeExceptionally(e);
                }
            });
            return Either.right(pair);
        }

    }

    public void put(T value) {
        synchronized(this){
            invalidate();
            state=StateValue.set(value);
        }
    }

    private record StateValue<T, Part>(boolean set, boolean calc, @Nullable T value, Pair<T, Part> pair) {
        private static final StateValue<?, ?> UNSET = new StateValue<>(false, false, null, null);

        private static <T, Part> @NotNull StateValue<T, Part> calc(@NonNull Pair<T,Part> pair) {return new StateValue<>(false,true,null,pair);}
        private static <T, Part> @NotNull StateValue<T, Part> set(T value) {return new StateValue<>(true,false,value,null);}
        @SuppressWarnings("unchecked")
        private static <T, Part> @NotNull StateValue<T, Part> unset() {return (StateValue<T, Part>) UNSET;}
    }

    public record Pair<T, Part>(CompletableFuture<T> future, ConcurrentLinkedQueue<Part> parts) {}
}
