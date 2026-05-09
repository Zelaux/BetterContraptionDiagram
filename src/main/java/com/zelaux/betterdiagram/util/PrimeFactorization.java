package com.zelaux.betterdiagram.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

import java.security.SecureRandom;

import static com.google.common.math.LongMath.isPrime;

public class PrimeFactorization {

    public static LongArrayList getDivisors(long n,LongArrayList result) {
        LongArrayList big=new LongArrayList();

        for (int i = 1; i * i <= n; i++) {
            if(n % i != 0) continue;
            result.add(i); // Добавляем сам делитель

            // Если делители разные (например, для 100: 4 и 25),
            // добавляем парный делитель
            if ((long) i * i != n) {
                big.add(n / i);
            }
        }
        result.addAll(big);
        return result;
    }
}
