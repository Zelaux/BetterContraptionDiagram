package com.zelaux.betterdiagram.feature.include_subsublevel.connector;

import com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper.AbstractForceMapper;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConstraintBuilder {

    public static final Function<SubLevel, Ref<SubLevelGroup>> DEFAULT_REF = Ref.with(SubLevelGroup::single);

    public static ConstraintTree buildTree(final SubLevel origin, int maxDepth) {
        var subLevel2Group = collectFixedGroups(origin, maxDepth);
        var frontier = new ObjectOpenHashSet<ConstraintTree>();
        var nextFrontier = new ObjectOpenHashSet<ConstraintTree>();

        ConstraintTree root = ConstraintTree.root(subLevel2Group.get(origin));
        frontier.add(root);
        int depth = 0;
        var mergableMappers = new ObjectArrayList<AbstractForceMapper>();
        while(!frontier.isEmpty() && depth < maxDepth) {
            depth++;
            for(var tree : frontier) {
                if(!tree.isValid()) continue;
                var currentMerged = tree.subLevelGroup;
                mergableMappers.clear();
                for(SubLevel current : currentMerged) {
                    for(final BlockEntitySubLevelActor actor : current.getPlot().getBlockEntityActors()) {
                        var constraints = ActorHandler.findFor(actor).mapToConstraint(current, subLevel2Group);
                        if(constraints == null) continue;
                        constraintsLoop:
                        for(final var constraint : constraints) {
                            if(constraint.originSubLevel() != current) continue;

                            SubLevel target = constraint.toAttachSubLevel();
                            if(!tree.path.contains(target) && !tree.hasChild(constraint)) {
                                if(!constraint.fixed()) {
                                    if(constraint.isMergable()){
                                        for(int i = 0; i < mergableMappers.size(); i++) {
                                            AbstractForceMapper createdMapper = mergableMappers.get(i);
                                            AbstractForceMapper abstractForceMapper = createdMapper.tryMergeWith(constraint);
                                            if(abstractForceMapper != null) {
                                                mergableMappers.set(i, abstractForceMapper);
                                                continue constraintsLoop;
                                            }
                                        }
                                        mergableMappers.add(constraint);
                                    }else{
                                        nextFrontier.add(tree.child(constraint, subLevel2Group.get(target)));
                                    }
                                }
                            }
                        }
                    }
                }
                for(AbstractForceMapper constraint : mergableMappers) {
                    nextFrontier.add(tree.child(constraint, subLevel2Group.get(constraint.toAttachSubLevel())));
                }
            }
            frontier.clear();

            var tmp = nextFrontier;
            nextFrontier = frontier;
            frontier = tmp;
        }

        return root;
    }

    private static Object2ObjectOpenHashMap<SubLevel, SubLevelGroup> collectFixedGroups(SubLevel origin, int maxDepth) {
        final var subLevel2Merged = new Object2ObjectOpenHashMap<SubLevel, Ref<SubLevelGroup>>();
        final var visited = new ObjectOpenHashSet<SubLevel>();
        var frontier = new ObjectOpenHashSet<SubLevel>();
        var nextFrontier = new ObjectOpenHashSet<SubLevel>();


        frontier.add(origin);
        int depth = 0;
        while(!frontier.isEmpty() && depth < maxDepth) {
            depth++;
            for(var current : frontier) {
                Ref<SubLevelGroup> currentMergedRef = subLevel2Merged.computeIfAbsent(current, DEFAULT_REF);
                for(final BlockEntitySubLevelActor actor : current.getPlot().getBlockEntityActors()) {
                    var mappers = ActorHandler.findFor(actor).mapToConstraint(current, Map.of());
                    if(mappers == null) continue;
                    for(final var mapper : mappers) {
                        if(mapper.originSubLevel() != current) continue;
                        SubLevel b = mapper.toAttachSubLevel();
                        if(mapper.fixed()) {
                            Ref<SubLevelGroup> bMergedRef = subLevel2Merged.computeIfAbsent(b, DEFAULT_REF);
                            var cMerged = currentMergedRef.value;
                            var bMerged = bMergedRef.value;
                            SubLevelGroup mergedMerged = cMerged.with(bMerged);
                            bMergedRef.setRef(currentMergedRef);
                            bMergedRef.setValue(mergedMerged);
                        }
                        if(visited.add(b)) {
                            nextFrontier.add(b);
                        }
                    }
                }
            }
            frontier.clear();


            var tmp = nextFrontier;
            nextFrontier = frontier;
            frontier = tmp;
        }
        for(Ref<SubLevelGroup> ref : subLevel2Merged.values()) {
            SubLevelGroup value = ref.value();
            List<SubLevel> all = value.all();
            int index = all.indexOf(origin);
            if(index >= 0) {
                if(all.getFirst() != origin) {
                    ArrayList<SubLevel> subLevels = new ArrayList<>(all);
                    subLevels.set(index, subLevels.get(0));
                    subLevels.set(0, origin);

                    ref.setValue(new SubLevelGroup.Many(subLevels));
                }
                break;
            }
        }
        return unpackRef(subLevel2Merged);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private static @NotNull Object2ObjectOpenHashMap<SubLevel, SubLevelGroup> unpackRef(Object2ObjectOpenHashMap<SubLevel, Ref<SubLevelGroup>> subLevel2Merged) {
        for(Object2ObjectMap.Entry entry : subLevel2Merged.object2ObjectEntrySet()) {
            entry.setValue(((Ref) entry.getValue()).value());
        }
        return (Object2ObjectOpenHashMap) subLevel2Merged;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    static class Ref<T> {
        Ref<T> wrapped;
        private T value;

        public static <T> Ref<T> with(T single) {
            Ref<T> ref = new Ref<>();
            ref.value = single;
            return ref;
        }

        public static <A, T> Function<A, Ref<T>> with(Function<A, T> single) {
            return a -> with(single.apply(a));
        }

        public Ref<T> setRef(Ref<T> ref) {
            Ref<T> selfRef = asRef();
            ref = ref.asRef();
            if(ref == selfRef) {
                if(selfRef != this) wrapped = selfRef;
                return this;
            }
            selfRef.wrapped = ref;
            wrapped = ref;
            return this;
        }

        public T value() {
            return asRef().value;
        }

        public Ref<T> asRef() {
            if(wrapped == null) return this;
            Ref<T> wrapped = this.wrapped;
            Ref<T> wrappedRef = wrapped.wrapped == null ? wrapped : wrapped.asRef();
            if(wrappedRef != wrapped) this.wrapped = wrappedRef;
            return wrappedRef;
        }

        public void setValue(T value) {
            asRef().value = value;
        }
    }
}
