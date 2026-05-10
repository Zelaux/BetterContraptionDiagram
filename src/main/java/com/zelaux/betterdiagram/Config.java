package com.zelaux.betterdiagram;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CENTER_OF_MASS_COLOR = BUILDER
        .comment("color of center of mass")
        .defineInRange("centerOfMassColor",0x76614D,0,Integer.MAX_VALUE)
        ;
    public static final ModConfigSpec.IntValue EXPECTED_CENTER_OF_MASS_COLOR = BUILDER
        .comment("color of target center of mass")
        .defineInRange("expectedCenterOfMassColor",0x764d4d,0,Integer.MAX_VALUE)
        ;
    public static final ModConfigSpec.IntValue FORCE_CORDS_COLOR = BUILDER
        .comment("color of force cords")
        .defineInRange("forceCordsColor",ChatFormatting.DARK_GRAY.getColor(),0,Integer.MAX_VALUE)
        ;

    public static final ModConfigSpec.DoubleValue AXIS_OFFSET = BUILDER
            .comment("axis offset from (0,0,0)")
            .defineInRange("axisOffset", 0.2, 0, 1);

    public static final ModConfigSpec.DoubleValue AXIS_SCALE = BUILDER
            .comment("axis scale")
            .defineInRange("axisScale", 0.5, 0.25, 2);
    public static final ModConfigSpec.IntValue MAX_ITERATION = BUILDER
            .comment("max mass stack distance")
            .defineInRange("maxMassStackIteration", 20, 1, 1000);
    public static final ModConfigSpec.IntValue MAX_FIX_DISTANCE = BUILDER
            .comment("max mass stack distance")
            .defineInRange("maxFixDistance", 1000, 1, 100_000);

    static final ModConfigSpec SPEC = BUILDER.build();

}
