package com.zelaux.betterdiagram.annotations;

import com.mojang.datafixers.util.Function3;
import com.zelaux.bcd.asm_commons.BoundMethod;
import org.apache.commons.lang3.function.TriConsumer;
import org.objectweb.asm.Type;

import java.util.function.*;

public class CompileTool {
    /**
     * Will be substituted in compile time
     *
     */
    public static String classToString(Class<?> type) {
        return type.getCanonicalName();
    }
    /**
     * Will be substituted in compile time
     *
     */
    public static String classToInternal(Class<?> type) {
        return type.getCanonicalName().replace('.','/');
    }

    /**
     * Will be substituted in compile time
     *
     */
    public static Type bakeType(Class<?> type) {
        return Type.getType(type);
    }
    static  {


        GetMethod.<String>methodOf(System.out::println);
        GetMethod.methodOf(CompileTool::proc);
    }
    static void proc(int x){}
    public interface GetMethod {

        /**
         * Will be substituted in compile time
         *
         */
        /*static <R> BoundMethod methodOf(Supplier<R> type) {
            throw new UnsupportedOperationException();
        }
        static <T> BoundMethod methodOf(Consumer<T> type) {
            throw new UnsupportedOperationException();
        }*/

        static <T1,R> BoundMethod methodOf(Function<T1, R> type) {
            throw new UnsupportedOperationException();
        }
        static <T1,T2,R> BoundMethod methodOf(BiFunction<T1,T2, R> type) {
            throw new UnsupportedOperationException();
        }
        static <T1,T2,T3,R> BoundMethod methodOf(Function3<T1,T2,T3, R> type) {
            throw new UnsupportedOperationException();
        }

        static <T1> BoundMethod methodOf(Consumer<T1> type) {
            throw new UnsupportedOperationException();
        }
        static <T1,T2> BoundMethod methodOf(BiConsumer<T1,T2> type) {
            throw new UnsupportedOperationException();
        }
        static <T1,T2,T3> BoundMethod methodOf(TriConsumer<T1,T2,T3> type) {
            throw new UnsupportedOperationException();
        }
    }
}
