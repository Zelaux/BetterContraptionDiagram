package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.annotations.DebugOnly;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class StringUtil {
    public static final DecimalFormat ZERO_WITH_SOME_DIGITS = makeFormat("#.#####");
    public static final DecimalFormat PARSABLE_FORMAT_5 = makeFormat("#.#####", true);
    private static final DecimalFormat ZERO_WITH_ZERO = makeFormat("#.#");
    private static final DecimalFormat[] formats = {
        null,
        makeFormat("#.####"),
        makeFormat("#.###"),
        makeFormat("#.##"),
        ZERO_WITH_ZERO,
    };

    public static @NotNull DecimalFormat makeFormat(String pattern) {
        return makeFormat(pattern, false);
    }
    public static @NotNull DecimalFormat makeFormat(String pattern, boolean parsable) {

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern(pattern);
        if(!parsable){
            decimalFormat.setGroupingSize(3);
            decimalFormat.setGroupingUsed(true);
        }else{
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        }
        return decimalFormat;
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
