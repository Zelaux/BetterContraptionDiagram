package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.google.common.collect.ImmutableSet;
import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstraintTree {
    public final ConstraintTree parent;
    public ImmutableSet<SubLevel> path;
    public SubLevelGroup subLevelGroup;
    public final AbstractForceMapper transitionConstraint;
    public final ObjectArrayList<ConstraintTree> children = new ObjectArrayList<>();
    public final Object2IntOpenHashMap<AbstractForceMapper> childrenSet = new Object2IntOpenHashMap<>();

    public static ConstraintTree build(SubLevel subLevel, int maxDepth) {
        return ConstraintBuilder.buildTree(subLevel,maxDepth+10);
    }

    public boolean hasChild(AbstractForceMapper constraint){
        return childrenSet.containsKey(constraint);
    }

    public @NotNull ConstraintTree child(AbstractForceMapper transitionConstraint, SubLevelGroup subLevelGroup) {
        int index = childrenSet.computeIfAbsent(transitionConstraint, it -> children.size());
        ConstraintTree tree = new ConstraintTree(this,
            ImmutableSet.<SubLevel>builder()
                        .addAll(path)
                        .addAll(transitionConstraint.toAttachGroup())
                        .build(),
            subLevelGroup,
            transitionConstraint
        );
        if(index==children.size()){
            children.add(tree);
        }else children.set(index, tree);
        return tree;
    }

    public static ConstraintTree root(SubLevelGroup level) {
        return new ConstraintTree(
            null,
            ImmutableSet.copyOf(level.all()),
            level,
            null
        );
    }

    public boolean merge(SubLevel level, Consumer<ConstraintTree> removed) {
        if(path.contains(level))return false;
        subLevelGroup = subLevelGroup.with(level);
        path= ImmutableSet.<SubLevel>builder().addAll(path).add(level).build();
        int offset=0;
        for(var iterator = childrenSet.object2IntEntrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            int i = entry.getIntValue();
            var constraint = entry.getKey();
            if(offset!=0){
                i-=offset;
                entry.setValue(i);

            }
            if (constraint.toAttachSubLevel() != level)continue;
            iterator.remove();
            removed.accept(children.remove(i));
        }
        return true;
    }

    public boolean isValid() {
        return parent==null || parent.hasChild(this.transitionConstraint);
    }
}
