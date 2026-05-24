package com.zelaux.betterdiagram.util;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class StringUtil {
    public static final DecimalFormat ZERO_WITH_SOME_DIGITS = makeFormat("#.#####");
    private static final DecimalFormat ZERO_WITH_ZERO = makeFormat("#.#");
    private static final DecimalFormat[] formats = {
        null,
        makeFormat("#.####"),
        makeFormat("#.###"),
        makeFormat("#.##"),
        ZERO_WITH_ZERO,
    };

    private static @NotNull DecimalFormat makeFormat(String pattern) {
        return new DecimalFormat(pattern);
    }

    public static @NotNull String plainDouble(double value) {
        if(value == 0) return formats[4].format(0);
        int amountOfIntSym;
        int intVal = Math.abs((int) value);
        //0..10..100..INF
        if(intVal >= 100) {
            amountOfIntSym = 3;
        } else {
            if(intVal >= 10) {
                amountOfIntSym = 2;
            } else {
                amountOfIntSym = (intVal + 9) / 10;
            }
        }
        if(amountOfIntSym > 0) {
            return formats[amountOfIntSym].format(value);
        }
        double v = Math.log10(Math.abs(value));
        if(v > -5) {
            return ZERO_WITH_SOME_DIGITS.format(value);
        }
        return "~0";
    }
}
