package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.index.Colors;
import net.createmod.catnip.theme.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;

public class VecFormat {
    public interface Presets {
        static MutableComponent gray(Vector3dc vector3dc) {
            return vectorToFormatted(vector3dc, Colors.GRAY).withColor(Colors.DARK_GRAY.getRGB());
        }

        static MutableComponent lightGray(Vector3dc vector3dc) {
            return vectorToFormatted(vector3dc, Colors.LIGHT_GRAY).withColor(Colors.GRAY.getRGB());
        }

        static MutableComponent forceCords(Vector3dc v) {
            int outerColor = (0xff << 24) | Config.FORCE_CORDS_COLOR.getAsInt();
            return withOuterColor(v, outerColor);
        }

        static @NotNull MutableComponent withOuterColor(Vector3dc v, int outerColor) {
            int innerColor = Color.mixColors(outerColor, 0xffffffff, 0.15f);
            return vectorToFormatted(v, new Color(innerColor)).withColor(outerColor);
        }

        static MutableComponent centerOfMass(Vector3dc v) {
            return withOuterColor(v, (0xff << 24) | Config.CENTER_OF_MASS_COLOR.getAsInt());
        }

        static MutableComponent blockCenterOfMass(Vector3dc v) {
            return lightGray(v);
        }
    }

    public static @NotNull MutableComponent vectorToFormatted(Vector3dc force, Color numberColor) {
        return vectorToFormatted(force, numberColor, numberColor, numberColor);
    }

    public static @NotNull MutableComponent vectorToFormatted(Vector3dc force, Color xColor, Color yColor, Color zColor) {
        double x = force.x();
        double y = force.y();
        double z = force.z();
        var sx = StringUtil.plainDouble(x);
        var sy = StringUtil.plainDouble(y);
        var sz = StringUtil.plainDouble(z);


        return Component
            .literal("(")
            .append(Component.literal(sx).withColor(xColor.getRGB()))
            .append(", ")
            .append(Component.literal(sy).withColor(yColor.getRGB()))
            .append(", ")
            .append(Component.literal(sz).withColor(zColor.getRGB()))
            .append(")")
            ;
    }

    @Unique
    private static @NotNull String vecToString(double x, double y, double z) {
        var sx = StringUtil.plainDouble(x);
        var sy = StringUtil.plainDouble(y);
        var sz = StringUtil.plainDouble(z);

        return "(" + sx + ", " + sy + ", " + sz + ")";
    }
}
