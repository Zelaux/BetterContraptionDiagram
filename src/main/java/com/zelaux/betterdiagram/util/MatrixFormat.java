package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.index.Colors;
import net.createmod.catnip.theme.Color;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Matrix3dc;
import org.joml.Vector3d;

import java.util.function.Consumer;

public class MatrixFormat {
    public interface Presets {
        static void inertia(Matrix3dc matrix3dc, Consumer<MutableComponent> consumer) {
            toFormatted(matrix3dc, Colors.LIGHT_GRAY, x -> consumer.accept(x.withColor(Colors.GRAY.getRGB())));
        }
    }


    public static void toFormatted(Matrix3dc matrix, Color color, Consumer<MutableComponent> consumer) {
        Vector3d vec = new Vector3d();
        consumer.accept(VecFormat.vectorToFormatted(matrix.getRow(0, vec), color, color, color));
        consumer.accept(VecFormat.vectorToFormatted(matrix.getRow(1, vec), color, color, color));
        consumer.accept(VecFormat.vectorToFormatted(matrix.getRow(2, vec), color, color, color));
    }
}
