package com.zelaux.betterdiagram.data.balance_force_filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import foundry.veil.api.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.EqualsAndHashCode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Objects;
import java.util.Optional;

@EqualsAndHashCode
public class BalanceForceFilterSubTree {
    public static final Codec<BalanceForceFilterSubTree> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("forceGroup").forGetter(x -> x.forceGroup),
            CodecUtil.VECTOR3DC_CODEC.listOf().fieldOf("rawVectors").forGetter(x -> {
                var list = new ObjectArrayList<Vector3dc>();
                for(Vector3dc dc : x.set) {
                    list.push(Objects.requireNonNullElse(dc, VecUtil.NAN_3));
                }
                return list;
            })
        ).apply(i, (forceGroup, rawVectors) -> {
            var tree = new BalanceForceFilterSubTree(forceGroup);
            tree.set.addAll(rawVectors);
            if(tree.set.remove(VecUtil.NAN_3)) {
                tree.set.add(null);
            }
            return tree;
        })
    );
    @NotNull
    public final ResourceLocation forceGroup;
    final ObjectSet<Vector3dc> set = new ObjectOpenCustomHashSet<>(VecUtil.STRATEGY);

    public BalanceForceFilterSubTree(@Nullable ResourceLocation forceGroup) {
        this.forceGroup = Objects.requireNonNullElse(forceGroup, BalanceForceFilterTree.NULL_LOCATION);
    }


    public MatchResult match(QueuedForceGroup.PointForce force) {
        if(set.contains(null)) return MatchResult.MATCHED;
        if(set.contains(force.point())) return MatchResult.MATCHED;
        return MatchResult.NOT_MATCHED;
    }

    void add(Optional<? extends Vector3dc> location) {
        set.add(location.map(Vector3d::new).orElse(null));
    }

    public boolean contains(Optional<Vector3dc> location) {
        return set.contains(location.orElse(null));
    }

    public void remove(Optional<Vector3dc> location) {
        set.remove(location.orElse(null));
    }
}
