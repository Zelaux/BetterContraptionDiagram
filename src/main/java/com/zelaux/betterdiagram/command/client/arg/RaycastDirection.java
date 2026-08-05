package com.zelaux.betterdiagram.command.client.arg;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import com.zelaux.betterdiagram.command.client.BCDRaycastCommand;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.function.Function;

public record RaycastDirection(String name,
                               Function<CommandContext<CommandSourceStack>, Iterable<Vector3dc>> direction) {
    public static final ObjectArrayList<RaycastDirection> directions = new ObjectArrayList<>();
    public static final Object2ObjectOpenHashMap<String,RaycastDirection> nameToDirection = new Object2ObjectOpenHashMap<>();

    static {
        for(Direction direction : Direction.values()) {
            Vec3i normal = direction.getNormal();
            addDirection(RaycastDirection.constant(direction.getName(), new Vector3d(
                normal.getX(), normal.getY(), normal.getZ()
            )));
        }

        addDirection(new RaycastDirection("lookat", RaycastDirection::findViewVector));
    }

    public static void addDirection(RaycastDirection direction) {
        nameToDirection.put(direction.name,direction);
    }


    public static RaycastDirection constant(String name, Vector3dc direction) {
        List<Vector3dc> list = List.of(direction);
        return new RaycastDirection(name, it -> list);
    }

    public static RaycastDirection constant(String name, Vector3dc a, Vector3dc b, Vector3dc... direction) {
        var list = ImmutableList
            .<Vector3dc>builder()
            .add(a)
            .add(b)
            .add(direction)
            .build();
        return new RaycastDirection(name, it -> list);
    }

    @SneakyThrows
    private static Iterable<Vector3dc> findViewVector(CommandContext<CommandSourceStack> ctx) {
        Entity entity = ctx.getSource().getEntityOrException();
        return List.of(JOMLConversion.toJOML(entity.getLookAngle()));
    }
}
