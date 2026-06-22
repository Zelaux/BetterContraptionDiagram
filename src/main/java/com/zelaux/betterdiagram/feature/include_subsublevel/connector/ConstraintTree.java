package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.google.common.collect.ImmutableSet;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstraintTree {
    public final ImmutableSet<SubLevel> path;
    public MergedSubLevel mergedSubLevel;
    public final LocalConstraint transitionConstraint;
    public final ObjectArrayList<ConstraintTree> children = new ObjectArrayList<>();
    public final Object2IntOpenHashMap<LocalConstraint> childrenSet = new Object2IntOpenHashMap<>();

    public static ConstraintTree build(SubLevel subLevel, int maxDepth) {
        return ConstraintBuilder.buildTree(subLevel,maxDepth);
    }

    public boolean hasChild(LocalConstraint constraint){
        return childrenSet.containsKey(constraint);
    }

    public ConstraintTree child(LocalConstraint transitionConstraint) {
        int index = childrenSet.computeIfAbsent(transitionConstraint, it -> children.size());
        ConstraintTree tree = new ConstraintTree(
            ImmutableSet.<SubLevel>builder()
                        .addAll(path)
                        .add(transitionConstraint.b())
                        .build(),
            MergedSubLevel.single(transitionConstraint.b()),
            transitionConstraint
        );
        if(index==children.size()){
            children.add(tree);
        }else children.set(index, tree);
        return tree;
    }

    public static ConstraintTree root(SubLevel level) {
        return new ConstraintTree(
            ImmutableSet.of(),
            MergedSubLevel.single(level),
            null
        );
    }

}
